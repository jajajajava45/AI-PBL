param([string]$Base = 'http://localhost:8081')
$ErrorActionPreference = 'Continue'
$script:pass = 0
$script:fail = 0
$script:logs = New-Object System.Collections.Generic.List[string]

function Req([string]$method, [string]$path, [string]$token, $body) {
    $params = @{ Uri = "$Base$path"; Method = $method; UseBasicParsing = $true; TimeoutSec = 20 }
    if ($token) { $params.Headers = @{ Authorization = "Bearer $token" } }
    if ($null -ne $body) {
        $json = $body | ConvertTo-Json -Depth 8
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
        $params.ContentType = 'application/json; charset=utf-8'
    }
    try {
        $r = Invoke-WebRequest @params
        return ($r.Content | ConvertFrom-Json)
    } catch {
        return [pscustomobject]@{ code = -1; message = $_.Exception.Message }
    }
}

function Check([string]$name, $cond, [string]$detail = '') {
    if ($cond) { $script:pass++; Write-Host ("[PASS] " + $name) }
    else { $script:fail++; Write-Host ("[FAIL] " + $name + "  " + $detail) -ForegroundColor Red; $script:logs.Add("$name :: $detail") }
}

function Login([string]$u, [string]$p) {
    $r = Req 'POST' '/api/auth/login' $null @{ username = $u; password = $p }
    if ($r.code -eq 0 -and $r.data.token) { return $r.data.token }
    Write-Host ("[WARN] login failed $u : " + $r.message) -ForegroundColor Yellow
    return $null
}

function Advance([string]$token, [int]$roundId) {
    return Req 'POST' "/api/admin/flow/rounds/$roundId/next-stage" $token @{}
}

function SubmitChoice([string]$token, [long[]]$ids, [bool]$internal, [bool]$cross) {
    return Req 'POST' '/api/student/choices' $token @{ teacherIds = $ids; allowInternal = $internal; allowCrossRoom = $cross }
}

function Schedule([string]$tk, [long]$sid, [string]$time) {
    return Req 'POST' '/api/teacher/interviews' $tk @{ studentId = $sid; interviewTime = $time; location = 'A-101' }
}

Write-Host '===== 1. 鉴权 ====='
$r = Req 'GET' '/api/public/flow' $null $null
Check '匿名访问业务接口被拒绝(401)' ($r.code -eq 401) ($r | ConvertTo-Json -Compress)
$r = Req 'POST' '/api/auth/login' $null @{ username = 'admin'; password = 'wrong' }
Check '错误密码被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)

$admin = Login 'admin' '123456'
Check '管理员登录' ($null -ne $admin)
$r = Req 'GET' '/api/auth/me' $admin $null
Check 'auth/me 返回管理员信息' ($r.code -eq 0 -and $r.data.role -eq 3 -and $r.data.username -eq 'admin') ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/admin/teachers' (Login 'S-A01' '123456') $null
Check '学生访问管理端被拒绝(403)' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)

Write-Host '===== 2. 公共接口 ====='
$r = Req 'GET' '/api/public/flow' $admin $null
Check 'public/flow 可访问' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/public/rooms' $admin $null
Check 'public/rooms 含 teacherCount' ($r.code -eq 0 -and $r.data.Count -eq 6 -and $null -ne $r.data[0].teacherCount) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/public/teachers' $admin $null
Check 'public/teachers 含 remaining' ($r.code -eq 0 -and $r.data.Count -eq 12 -and $null -ne $r.data[0].remaining) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/public/teachers?roomId=1' $admin $null
Check 'public/teachers 按实训室过滤' ($r.code -eq 0 -and $r.data.Count -eq 2) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/public/config' (Login 'S-A01' '123456') $null
Check 'public/config 登录学生可读' ($r.code -eq 0 -and $r.data.maxChoicesPerRound -eq 3) ($r | ConvertTo-Json -Depth 3 -Compress)

Write-Host '===== 3. 管理端基础数据 ====='
$r = Req 'GET' '/api/admin/teachers?page=1&size=50' $admin $null
Check '老师分页列表含 userId' ($r.code -eq 0 -and $r.data.total -eq 12 -and $null -ne $r.data.records[0].userId) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/admin/students?page=1&size=50' $admin $null
Check '学生分页列表含 userId' ($r.code -eq 0 -and $r.data.total -eq 20 -and $null -ne $r.data.records[0].userId) ($r | ConvertTo-Json -Depth 3 -Compress)

Write-Host '===== 4. 第一轮：志愿填报 ====='
$r = Req 'POST' '/api/admin/flow/rounds/1/start' $admin @{}
Check '启动第一轮' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/public/flow' $admin $null
Check '流程进入 CHOICE_OPEN' ($r.code -eq 0 -and $r.data.currentStage.stageName -eq 'CHOICE_OPEN') ($r | ConvertTo-Json -Depth 4 -Compress)

$tokens = @{}
foreach ($s in @('S-A01','S-A02','S-A03','S-B01','S-B02','S-B03','S-C01','S-C02','S-D01','S-D02')) {
    $tokens[$s] = Login $s '123456'
}
Check '10 个学生登录成功' (($tokens.Values | Where-Object { $_ }).Count -eq 10)

$r = SubmitChoice $tokens['S-A01'] @(1,2,3) $true $true
Check 'S-A01 提交三个志愿' ($r.code -eq 0) ($r.message)
$r = SubmitChoice $tokens['S-A01'] @(1,2,3,4) $true $true
Check '超过 3 个志愿被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = SubmitChoice $tokens['S-A01'] @(1,1) $true $true
Check '重复志愿被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = SubmitChoice $tokens['S-A01'] @(1,2,3) $true $false
Check '覆盖提交志愿成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/student/choices' $tokens['S-A01'] $null
Check '覆盖后仍为 3 条志愿' ($r.code -eq 0 -and $r.data.Count -eq 3) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = SubmitChoice $tokens['S-A01'] @(1,2) $false $true
Check '缩小志愿范围覆盖成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/student/choices' $tokens['S-A01'] $null
Check '覆盖后志愿为 2 条' ($r.code -eq 0 -and $r.data.Count -eq 2) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = SubmitChoice $tokens['S-A01'] @(1,2,3) $true $false
SubmitChoice $tokens['S-A02'] @(1,2) $true $false | Out-Null
SubmitChoice $tokens['S-A03'] @(5,6) $false $false | Out-Null
SubmitChoice $tokens['S-B01'] @(5) $true $true | Out-Null
SubmitChoice $tokens['S-B02'] @(1,2) $false $true | Out-Null
SubmitChoice $tokens['S-B03'] @(1) $true $false | Out-Null
SubmitChoice $tokens['S-C01'] @(7,8) $false $false | Out-Null
SubmitChoice $tokens['S-C02'] @(7,8) $false $false | Out-Null
SubmitChoice $tokens['S-D01'] @(7) $false $false | Out-Null
SubmitChoice $tokens['S-D02'] @(7) $true $true | Out-Null
$r = Req 'GET' '/api/student/status' $tokens['S-A01'] $null
Check '学生状态为已提交志愿' ($r.code -eq 0 -and $r.data.status.code -eq 'CHOICE_SUBMITTED') ($r | ConvertTo-Json -Depth 4 -Compress)

Write-Host '===== 5. 阶段推进与面试 ====='
$r = Advance $admin 1
Check 'CHOICE_OPEN -> CHOICE_CLOSED' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = SubmitChoice $tokens['S-A01'] @(1) $true $true
Check '非填报阶段提交志愿被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = Advance $admin 1
Check 'CHOICE_CLOSED -> INTERVIEW' ($r.code -eq 0) ($r.message)

$t1 = Login 'T-SD01' '123456'
$t2 = Login 'T-SD02' '123456'
$t5 = Login 'T-AI01' '123456'
$t7 = Login 'T-RB01' '123456'
Check '老师登录成功' (($null -ne $t1) -and ($null -ne $t2) -and ($null -ne $t5) -and ($null -ne $t7))
$r = Req 'GET' '/api/teacher/profile' $t1 $null
Check '老师档案含 realName/roomName' ($r.code -eq 0 -and $r.data.realName -and $r.data.roomName) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/auth/me' $t1 $null
Check 'auth/me 老师含 roomName/remaining' ($r.code -eq 0 -and $r.data.roomName -and $null -ne $r.data.remaining) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/teacher/candidates' $t1 $null
Check 'T-SD01 候选学生 4 人' ($r.code -eq 0 -and $r.data.Count -eq 4) ($r | ConvertTo-Json -Depth 3 -Compress)

$r = Schedule $t1 1 '2026-11-20 09:00:00'
Check 'T-SD01 安排 S-A01 面试' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
Schedule $t1 2 '2026-11-20 10:00:00' | Out-Null
Schedule $t1 5 '2026-11-20 11:00:00' | Out-Null
Schedule $t2 5 '2026-11-20 14:00:00' | Out-Null
Schedule $t5 3 '2026-11-20 09:30:00' | Out-Null
Schedule $t5 4 '2026-11-20 10:30:00' | Out-Null
$r = Schedule $t1 999 '2026-11-20 09:00:00'
Check '给非候选学生安排面试被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/teacher/interviews' $t1 $null
Check 'T-SD01 面试列表 3 条' ($r.code -eq 0 -and $r.data.Count -eq 3) ($r | ConvertTo-Json -Depth 3 -Compress)

$t1Interviews = (Req 'GET' '/api/teacher/interviews' $t1 $null).data
$r = Req 'PUT' ("/api/teacher/interviews/" + $t1Interviews[0].id) $t1 @{ score = 88; feedback = '表现良好'; status = 2 }
Check '录入面试成绩成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
foreach ($pair in @(@(1,92), @(2,80))) {
    $iid = $t1Interviews[$pair[0]].id
    Req 'PUT' "/api/teacher/interviews/$iid" $t1 @{ score = $pair[1]; feedback = 'OK'; status = 2 } | Out-Null
}
$t2Interviews = (Req 'GET' '/api/teacher/interviews' $t2 $null).data
Req 'PUT' ("/api/teacher/interviews/" + $t2Interviews[0].id) $t2 @{ score = 90; feedback = 'OK'; status = 2 } | Out-Null
$t5Interviews = (Req 'GET' '/api/teacher/interviews' $t5 $null).data
Req 'PUT' ("/api/teacher/interviews/" + $t5Interviews[0].id) $t5 @{ score = 85; feedback = 'OK'; status = 2 } | Out-Null
Req 'PUT' ("/api/teacher/interviews/" + $t5Interviews[1].id) $t5 @{ score = 75; feedback = 'OK'; status = 2 } | Out-Null
$r = Req 'GET' '/api/student/interviews' $tokens['S-A01'] $null
Check '学生面试含成绩 88' ($r.code -eq 0 -and $r.data.Count -eq 1 -and $r.data[0].score -eq 88) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = Req 'GET' '/api/teacher/candidates' $t1 $null
Check '候选列表含面试均分' ($r.code -eq 0 -and $null -ne $r.data[0].avgScore) ($r | ConvertTo-Json -Depth 3 -Compress)

$r = Advance $admin 1
Check 'INTERVIEW -> TEACHER_PICK' ($r.code -eq 0) ($r.message)

Write-Host '===== 6. 老师选人 ====='
$r = Req 'POST' '/api/teacher/picks' $t1 @{ studentId = 1 }
Check 'T-SD01 选 S-A01' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
Req 'POST' '/api/teacher/picks' $t1 @{ studentId = 2 } | Out-Null
Req 'POST' '/api/teacher/picks' $t1 @{ studentId = 5 } | Out-Null
$r = Req 'POST' '/api/teacher/picks' $t1 @{ studentId = 3 }
Check '选非本人候选学生被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
Req 'POST' '/api/teacher/picks' $t2 @{ studentId = 5 } | Out-Null
Req 'POST' '/api/teacher/picks' $t5 @{ studentId = 3 } | Out-Null
Req 'POST' '/api/teacher/picks' $t7 @{ studentId = 7 } | Out-Null
Req 'POST' '/api/teacher/picks' $t7 @{ studentId = 8 } | Out-Null
$r = Req 'POST' '/api/teacher/picks' $t7 @{ studentId = 9 }
Check '超出名额选人被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = Req 'DELETE' '/api/teacher/picks/8' $t7 $null
Check '撤销选择成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
Req 'POST' '/api/teacher/picks' $t7 @{ studentId = 9 } | Out-Null
$r = Req 'GET' '/api/teacher/picks' $t7 $null
Check 'T-RB01 已选 2 人' ($r.code -eq 0 -and $r.data.Count -eq 2) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/student/pickers' $tokens['S-B02'] $null
Check 'S-B02 被 2 位老师选中' ($r.code -eq 0 -and $r.data.pickers.Count -eq 2) ($r | ConvertTo-Json -Depth 4 -Compress)

$r = Advance $admin 1
Check 'TEACHER_PICK -> STUDENT_FINAL' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = Req 'GET' '/api/student/result' $tokens['S-A01'] $null
Check '仅 1 位选中的学生进入终选阶段自动匹配' ($r.code -eq 0 -and $r.data.matched.teacherId -eq 1) ($r | ConvertTo-Json -Depth 5 -Compress)
$r = Req 'POST' '/api/student/final-choice' $tokens['S-A01'] @{ teacherId = 1 }
Check '已自动匹配的学生终选被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = Req 'POST' '/api/student/final-choice' $tokens['S-B02'] @{ teacherId = 1 }
Check 'S-B02 终选面试分较低的老师成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'POST' '/api/student/final-choice' $tokens['S-B02'] @{ teacherId = 2 }
Check '重复终选被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
$r = Req 'POST' '/api/student/final-choice' $tokens['S-B03'] @{ teacherId = 1 }
Check '未被选中的学生终选被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)

$r = Advance $admin 1
Check 'STUDENT_FINAL -> DIVERSION' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 5 -Compress)
$r = Req 'GET' '/api/student/result' $tokens['S-B02'] $null
Check '终选结果被尊重（T-SD01）' ($r.code -eq 0 -and $r.data.matched.teacherId -eq 1 -and $r.data.matched.matchSource -eq 1) ($r | ConvertTo-Json -Depth 5 -Compress)

Write-Host '===== 7. 分流阶段 ====='
$r = Req 'GET' '/api/teacher/diversion/candidates' $t1 $null
$ids = @(); foreach ($c in $r.data) { $ids += $c.studentId }
Check 'T-SD01 调剂候选含实训室内学生 S-B03' ($r.code -eq 0 -and $ids -contains 6) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'POST' '/api/teacher/diversion/picks' $t1 @{ studentId = 6 }
Check 'T-SD01 调剂录取 S-B03' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/student/result' $tokens['S-B03'] $null
Check '调剂录取来源为 2' ($r.code -eq 0 -and $r.data.matched.matchSource -eq 2) ($r | ConvertTo-Json -Depth 5 -Compress)
$r = Req 'GET' '/api/teacher/diversion/candidates' $t5 $null
$ids = @(); foreach ($c in $r.data) { $ids += $c.studentId }
Check 'T-AI01 调剂候选含 S-B01' ($r.code -eq 0 -and $ids -contains 4) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'POST' '/api/teacher/diversion/picks' $t5 @{ studentId = 4 }
Check 'T-AI01 调剂录取 S-B01' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)

$r = Req 'GET' '/api/admin/flow/rounds/1/diversion/candidates' $admin $null
Check '管理端分流候选含 scopeText/statusText' ($r.code -eq 0 -and $r.data.Count -gt 0 -and $r.data[0].scopeText -and $r.data[0].statusText) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = Req 'POST' '/api/admin/flow/rounds/1/diversion/system-place' $admin @{}
Check '系统统筹执行成功' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 5 -Compress)
$r = Req 'GET' '/api/admin/flow/rounds/1/diversion/records' $admin $null
Check '分流记录可查询' ($r.code -eq 0 -and $r.data.Count -ge 1) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/student/result' $tokens['S-D02'] $null
Check 'S-D02 系统统筹安置来源为 3' ($r.code -eq 0 -and $r.data.matched.matchSource -eq 3) ($r | ConvertTo-Json -Depth 5 -Compress)
$r = Req 'GET' '/api/student/result' $tokens['S-D02'] $null
Check '结果 diversionHistory 按轮次分组' ($r.code -eq 0 -and $r.data.diversionHistory.Count -ge 1 -and $null -ne $r.data.diversionHistory[0].roundNo -and $null -ne $r.data.diversionHistory[0].records) ($r | ConvertTo-Json -Depth 5 -Compress)

$r = Advance $admin 1
Check 'DIVERSION -> COMPLETED' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = Req 'GET' '/api/public/flow' $admin $null
Check '第一轮已结束，无进行中轮次' ($r.code -eq 0 -and $null -eq $r.data.currentRound) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = Advance $admin 1
Check 'COMPLETED 之后不可再推进' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)

Write-Host '===== 8. 第二轮 ====='
$r = Req 'POST' '/api/admin/flow/rounds/2/start' $admin @{}
Check '启动第二轮' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/public/flow' $admin $null
Check '第二轮回到 CHOICE_OPEN' ($r.code -eq 0 -and $r.data.currentStage.stageName -eq 'CHOICE_OPEN' -and $r.data.currentRound.roundNo -eq 2) ($r | ConvertTo-Json -Depth 4 -Compress)
$tkD01 = Login 'S-D01' '123456'
$tkE01 = Login 'S-E01' '123456'
SubmitChoice $tkD01 @(1) $false $false | Out-Null
SubmitChoice $tkE01 @(1,2) $true $true | Out-Null
Advance $admin 2 | Out-Null
Advance $admin 2 | Out-Null
$t1 = Login 'T-SD01' '123456'
Req 'POST' '/api/teacher/interviews' $t1 @{ studentId = 9; interviewTime = '2026-11-25 09:00:00'; location = 'B-202' } | Out-Null
$iv = (Req 'GET' '/api/teacher/interviews' $t1 $null).data
Req 'PUT' ("/api/teacher/interviews/" + $iv[0].id) $t1 @{ score = 86; feedback = 'OK'; status = 2 } | Out-Null
Advance $admin 2 | Out-Null
$r = Req 'POST' '/api/teacher/picks' $t1 @{ studentId = 9 }
Check '第二轮老师选人成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
Advance $admin 2 | Out-Null
$r = Req 'GET' '/api/student/result' $tkD01 $null
Check '第二轮学生自动匹配' ($r.code -eq 0 -and $r.data.matched.teacherId -eq 1) ($r | ConvertTo-Json -Depth 5 -Compress)
Advance $admin 2 | Out-Null
Req 'POST' '/api/admin/flow/rounds/2/diversion/system-place' $admin @{} | Out-Null
Advance $admin 2 | Out-Null
$r = Req 'GET' '/api/public/flow' $admin $null
Check '第二轮结束' ($r.code -eq 0 -and $null -eq $r.data.currentRound) ($r | ConvertTo-Json -Depth 4 -Compress)

Write-Host '===== 9. 第三轮（空轮） ====='
$r = Req 'POST' '/api/admin/flow/rounds/3/start' $admin @{}
Check '启动第三轮' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
Advance $admin 3 | Out-Null
Advance $admin 3 | Out-Null
$r = Advance $admin 3
Check '第三轮空轮可继续推进至老师选人' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 4 -Compress)
Advance $admin 3 | Out-Null
$r = Advance $admin 3
Check '第三轮推进到分流阶段' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 4 -Compress)
Req 'POST' '/api/admin/flow/rounds/3/diversion/system-place' $admin @{} | Out-Null
$r = Advance $admin 3
Check '第三轮结束' ($r.code -eq 0) ($r | ConvertTo-Json -Depth 4 -Compress)
$r = Req 'GET' '/api/public/flow' $admin $null
Check '三轮结束后无进行中轮次' ($r.code -eq 0 -and $null -eq $r.data.currentRound) ($r | ConvertTo-Json -Depth 4 -Compress)

Write-Host '===== 10. 待选池 ====='
$r = Req 'GET' '/api/admin/wait-pool' $admin $null
Check '最后一轮未安置学生进入待选池' ($r.code -eq 0 -and $r.data.Count -ge 1) ($r | ConvertTo-Json -Depth 3 -Compress)
$wpStudent = if ($r.data.Count -gt 0) { $r.data[0].studentId } else { 0 }
$r = Req 'PUT' "/api/admin/wait-pool/$wpStudent/interview" $admin @{ interviewAt = '2026-12-01 10:00:00' }
Check '待选池登记约谈时间' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/admin/wait-pool?status=1' $admin $null
$found = $false; foreach ($w in $r.data) { if ($w.studentId -eq $wpStudent) { $found = $true } }
Check '待选池按状态筛选（已约谈）' ($r.code -eq 0 -and $found) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'POST' "/api/admin/wait-pool/$wpStudent/settle" $admin @{ teacherId = 9; remark = '人工安置测试' }
Check '人工安置成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/admin/wait-pool?status=2' $admin $null
$found = $false; foreach ($w in $r.data) { if ($w.studentId -eq $wpStudent) { $found = $true } }
Check '待选池状态变为已安置' ($r.code -eq 0 -and $found) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/teacher/students' (Login 'T-UI01' '123456') $null
$hasWp = $false; foreach ($m in $r.data) { if ($m.studentId -eq $wpStudent) { $hasWp = $true; $src = $m.matchSource } }
Check '人工安置来源为 4' ($r.code -eq 0 -and $hasWp -and $src -eq 4) ($r | ConvertTo-Json -Depth 3 -Compress)

Write-Host '===== 11. 统计 / 配置 / 基础数据 ====='
$r = Req 'GET' '/api/admin/stats/summary' $admin $null
Check '统计汇总可访问' ($r.code -eq 0 -and $r.data.totalStudents -eq 20 -and $r.data.matchedCount -ge 7) ($r | ConvertTo-Json -Depth 3 -Compress)
$r = Req 'GET' '/api/admin/stats/by-room' $admin $null
Check '按实训室统计' ($r.code -eq 0 -and $r.data.Count -eq 6) ($r | ConvertTo-Json -Depth 2 -Compress)
$r = Req 'GET' '/api/admin/stats/by-teacher' $admin $null
Check '按老师统计' ($r.code -eq 0 -and $r.data.Count -ge 12) ($r | ConvertTo-Json -Depth 2 -Compress)
$r = Req 'GET' '/api/admin/stats/by-round' $admin $null
Check '按轮次统计' ($r.code -eq 0 -and $r.data.Count -eq 3) ($r | ConvertTo-Json -Depth 2 -Compress)
try {
    $csv = Invoke-WebRequest -Uri "$Base/api/admin/stats/export" -Headers @{ Authorization = "Bearer $admin" } -UseBasicParsing -TimeoutSec 20
    Check '导出 CSV 正常' ($csv.StatusCode -eq 200 -and $csv.Content.Length -gt 20) $csv.StatusCode
} catch { Check '导出 CSV 正常' $false $_.Exception.Message }

$r = Req 'GET' '/api/admin/config' $admin $null
Check '配置列表可访问' ($r.code -eq 0 -and $r.data.Count -ge 1) ($r | ConvertTo-Json -Depth 3 -Compress)
$cfgKey = $r.data[0].configKey
$cfgOld = $r.data[0].configValue
$r = Req 'PUT' "/api/admin/config/$cfgKey" $admin @{ value = $cfgOld }
Check '修改配置成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)

$r = Req 'PUT' '/api/admin/teachers/1' $admin @{ quota = 10 }
Check '修改老师名额成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'GET' '/api/admin/teachers/1/quota-logs' $admin $null
Check '名额变更写入日志' ($r.code -eq 0 -and $r.data.Count -ge 1) ($r | ConvertTo-Json -Depth 3 -Compress)

$r = Req 'PUT' '/api/admin/users/102/enabled' $admin @{ enabled = $false }
Check '禁用老师账号' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'POST' '/api/auth/login' $null @{ username = 'T-SD02'; password = '123456' }
Check '禁用后登录被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)
Req 'PUT' '/api/admin/users/102/enabled' $admin @{ enabled = $true } | Out-Null
$r = Login 'T-SD02' '123456'
Check '启用后可正常登录' ($null -ne $r)
$r = Req 'PUT' '/api/admin/users/102/reset-password' $admin @{ newPassword = 'abc12345' }
Check '重置老师密码成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Login 'T-SD02' 'abc12345'
Check '新密码可登录' ($null -ne $r)

$r = Req 'POST' '/api/admin/teachers' $admin @{ username = 'T-NEW01'; realName = 'NewTeacher'; roomId = 1; quota = 2; title = '讲师' }
Check '新增老师成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Login 'T-NEW01' '123456'
Check '新老师默认密码登录' ($null -ne $r)
$r = Req 'POST' '/api/admin/students' $admin @{ username = 'S-NEW01'; realName = 'NewStudent'; studentNo = '20269999'; major = '软件工程'; grade = '2026'; className = '软工1班' }
Check '新增学生成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Login 'S-NEW01' '123456'
Check '新学生默认密码登录' ($null -ne $r)
$r = Req 'PUT' '/api/admin/students/21' $admin @{ className = '软工2班' }
Check '修改学生信息成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)

$r = Req 'POST' '/api/admin/rooms' $admin @{ name = '测试实训室'; code = 'TEST-01'; description = '自动化测试'; status = 1 }
Check '新增实训室成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'PUT' '/api/admin/rooms/7' $admin @{ description = '自动化测试已修改'; status = 0 }
Check '修改实训室成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Req 'POST' '/api/admin/teachers' $admin @{ username = 'T-SD01'; realName = '重复账号'; roomId = 1 }
Check '重复用户名新增老师被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)

$r = Req 'POST' '/api/auth/change-password' (Login 'S-NEW01' '123456') @{ oldPassword = '123456'; newPassword = 'xyz12345' }
Check '学生修改密码成功' ($r.code -eq 0) ($r | ConvertTo-Json -Compress)
$r = Login 'S-NEW01' 'xyz12345'
Check '修改后新密码登录成功' ($null -ne $r)
$r = Req 'POST' '/api/auth/change-password' (Login 'S-NEW01' 'xyz12345') @{ oldPassword = 'wrong'; newPassword = 'aaa11111' }
Check '旧密码错误修改被拒绝' ($r.code -ne 0) ($r | ConvertTo-Json -Compress)

Write-Host ''
Write-Host ("===== 结果：通过 " + $script:pass + " / 失败 " + $script:fail + " =====")
if ($script:fail -gt 0) { Write-Host '失败项：'; $script:logs | ForEach-Object { Write-Host $_ -ForegroundColor Red } }
exit $script:fail
