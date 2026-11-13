# AI+PBL 师生双向选择系统 — API 接口文档

> 版本：v1.0
> 日期：2026-11-13
> 说明：前后端协作契约，前端可按本文档并行开发；后端实现与本文档保持一致
> 后端：Spring Boot 3 + MyBatis-Plus，启动后 Swagger 地址：`http://localhost:8080/swagger-ui.html`

---

## 一、全局约定

### 1.1 基础信息
- **Base URL**：`http://localhost:8080/api`
- **认证方式**：JWT，登录后所有请求携带请求头 `Authorization: Bearer <token>`
- **内容类型**：`application/json; charset=UTF-8`

### 1.2 统一响应结构
```json
{ "code": 0, "message": "success", "data": { ... } }
```
HTTP 状态码统一返回 200，**以 body 中的 code 为准**：

| code | 含义 |
|------|------|
| 0 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 业务冲突（重复提交 / 状态不允许 / 名额不足等） |
| 500 | 系统异常 |

### 1.3 时间格式
`yyyy-MM-dd HH:mm:ss`（东八区）

### 1.4 角色码
| role | 角色 | 权限前缀 |
|------|------|----------|
| 1 | 学生 | `/api/student/**` |
| 2 | 老师 | `/api/teacher/**` |
| 3 | 管理员 | `/api/admin/**` |

`/api/auth/**` 与 `/api/public/**` 所有已登录用户可访问。

### 1.5 枚举字典（响应中均返回 code + 代码里可转义文本）

| 枚举 | 取值 |
|------|------|
| 轮次类型 roundType | 1=初选，2=补录 |
| 轮次状态 roundStatus | 0=未开始，1=进行中，2=已结束 |
| 阶段 stageName | CHOICE_OPEN → CHOICE_CLOSED → INTERVIEW → TEACHER_PICK → STUDENT_FINAL → DIVERSION → COMPLETED（顺序 1~7） |
| 阶段状态 stageStatus | 0=未开始，1=进行中，2=已结束 |
| 选人来源 pickSource | 1=学生志愿填报，2=调剂范围内选择 |
| 匹配来源 matchSource | 1=正常匹配，2=调剂老师选择，3=系统统筹，4=待选池人工安置 |
| 面试状态 interviewStatus | 0=待安排，1=已安排，2=已完成，3=已取消 |
| 待选池状态 waitPoolStatus | 0=待约谈，1=已约谈，2=已安置 |
| 分流步骤 diversionStep | 1=调剂范围内老师选择，2=系统统筹-实训室内，3=系统统筹-全校 |
| 分流结果 diversionResult | 1=成功安置，2=未安置流转下一步，3=彻底失败 |
| 调剂范围 scopeType | 1=实训室内，2=校内 |

### 1.6 学生综合状态码（`GET /student/status` 返回 `status.code`，实时推导）

| code | text | 出现时机 |
|------|------|----------|
| NOT_CHOSEN | 未填报志愿 | 当前阶段为志愿填报且未提交 |
| CHOICE_SUBMITTED | 已提交志愿 | 已提交，等待面试 |
| INTERVIEWING | 面试中 | 面试阶段 |
| WAIT_TEACHER_PICK | 等待老师选择 | 选人阶段且未被任何老师选中 |
| PICKED | 已被老师选中 | 被老师选中（终选阶段前） |
| WAIT_FINAL | 待终选 | ≥2 位老师选中我，需终选 |
| MATCHED | 已匹配 | 已写入匹配结果 |
| DIVERSION_TEACHER_SELECT | 分流中-等待调剂老师选择 | 分流阶段未录取且服从调剂 |
| DIVERSION_SYSTEM | 分流中-等待系统统筹 | 分流阶段老师未选且服从调剂 |
| TO_NEXT_ROUND | 流转下一轮补录 | 上轮分流后未安置，等待下一轮 |
| WAIT_POOL | 待选池 | 进入待选池（未安置） |
| SETTLED | 已安置 | 待选池人工安置完成 |

---

## 二、认证模块 `/api/auth`

### 2.1 登录
`POST /api/auth/login` — 无需 token

请求：
```json
{ "username": "S-A01", "password": "123456" }
```

响应 data：
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { "id": 201, "username": "S-A01", "realName": "张一", "role": 1 }
}
```

错误：401 用户名或密码错误；403 账号被禁用（以 code 409 message 提示）。

### 2.2 当前用户信息
`GET /api/auth/me` → `data`
```json
{ "id": 201, "username": "S-A01", "realName": "张一", "role": 1, "phone": "139...", "email": "..." }
```
（学生/老师会额外带扩展字段：学生含 `studentNo/major/grade/className`，老师含 `teacherId/roomId/roomName/title/researchDirection/quota/remaining`）

### 2.3 修改密码
`POST /api/auth/change-password`
```json
{ "oldPassword": "123456", "newPassword": "654321" }
```

---

## 三、公共模块 `/api/public`（已登录用户）

### 3.1 实训室列表
`GET /api/public/rooms` → `data`
```json
[ { "id": 1, "name": "智能软件开发实训室", "code": "ROOM-SD", "description": "...", "teacherCount": 2 } ]
```

### 3.2 老师列表（含实时剩余名额）
`GET /api/public/teachers?roomId=1`（roomId 可选，不传返回全部）

`data`（TeacherCardVO）：
```json
[
  {
    "id": 1, "teacherNo": "TSD01", "realName": "林工", "title": "副教授",
    "researchDirection": "Java 后端与微服务", "bio": "...",
    "roomId": 1, "roomName": "智能软件开发实训室",
    "quota": 15, "remaining": 13, "status": 1
  }
]
```
> `remaining` = 名额上限 − 已录取数 − 本轮暂选未落地人数；学生据此查看剩余名额（US-02/US-12）。

### 3.3 流程总览
`GET /api/public/flow` → `data`
```json
{
  "currentRound": { "id": 1, "roundNo": 1, "roundType": 1, "name": "第一轮初选", "roundStatus": 1 },
  "currentStage": { "id": 11, "stageName": "CHOICE_OPEN", "stageText": "志愿填报", "stageStatus": 1 },
  "rounds": [
    { "id": 1, "roundNo": 1, "roundType": 1, "name": "第一轮初选", "roundStatus": 1,
      "stages": [ { "id": 11, "stageName": "CHOICE_OPEN", "stageText": "志愿填报", "stageOrder": 1, "stageStatus": 1, "startAt": "...", "endAt": null } ] }
  ]
}
```
> 全部轮次结束时 `currentRound/currentStage` 为 `null`。

---

## 四、学生端 `/api/student`

### 4.1 综合状态（学生端首页聚合接口）
`GET /api/student/status` → `data`
```json
{
  "student": { "id": 1, "studentNo": "20260001", "realName": "张一", "major": "软件工程", "grade": "2024", "className": "软工1班" },
  "status": { "code": "MATCHED", "text": "已匹配" },
  "round": { "id": 1, "roundNo": 1, "roundType": 1, "name": "第一轮初选" },
  "stage": { "stageName": "TEACHER_PICK", "stageText": "老师选学生", "stageStatus": 1 },
  "matched": {
    "id": 1, "teacherId": 1, "teacherName": "林工", "roomName": "智能软件开发实训室",
    "roundNo": 1, "roundName": "第一轮初选", "matchSource": 1, "createdAt": "..."
  },
  "waitPool": null,
  "currentRound": {
    "choices": [ { "teacherId": 1, "teacherName": "林工", "roomName": "SD", "choiceRank": 1 } ],
    "wish": { "allowInternal": 0, "allowCrossRoom": 0 },
    "interviews": [ { "id": 1, "teacherId": 1, "teacherName": "林工", "interviewTime": "...", "location": "SD-A301", "interviewStatus": 1, "score": null, "feedback": null } ],
    "pickers": [ { "teacherId": 1, "teacherName": "林工", "roomName": "SD" } ],
    "finalChoice": { "teacherId": 1, "teacherName": "林工" },
    "diversionRecords": [ { "step": 1, "stepText": "调剂范围内老师选择", "teacherName": "林工", "scopeType": 2, "result": 1 } ]
  }
}
```
字段说明：
- `matched`：已匹配时返回；未匹配为 `null`
- `waitPool`：进入待选池时返回 `{status, statusText, interviewAt, settleTeacherName, settleRemark}`
- `currentRound`：无进行中的轮次时为 `null`
- `pickers`：仅本轮阶段 ≥ TEACHER_PICK 时返回（学生终选依据）

### 4.2 本轮我的志愿
`GET /api/student/choices` → `[ChoiceVO]`（同 4.1 choices 结构，含 id/choiceRank/title/researchDirection）

### 4.3 提交 / 修改志愿（含调剂意愿）
`POST /api/student/choices` — 全量提交（覆盖式）

请求：
```json
{
  "teacherIds": [1, 2, 3],
  "allowInternal": true,
  "allowCrossRoom": false
}
```
- `teacherIds`：1~3 个老师 id（平行志愿不分先后，服务端按提交顺序编 rank 仅作展示）
- `allowInternal` / `allowCrossRoom`：调剂意愿，两项可多选/单选/不选（都不选 = 不服从）

前置校验（违反返回 409）：
- 当前阶段必须是 `CHOICE_OPEN`
- 学生未被录取（无匹配结果）且不在待选池
- 老师存在、状态为活跃、实训室启用
- 志愿不重复；数量 ≤ 配置 `max_choices_per_round`（默认 3）

> 阶段关闭前可重复提交覆盖（US-06），同上校验。

### 4.4 本轮我的面试
`GET /api/student/interviews` → `[InterviewVO]`（含 teacherName、interviewTime、location、interviewStatus、score、feedback — US-07/US-08）

### 4.5 选中我的老师（终选依据）
`GET /api/student/pickers` → `data`
```json
{ "pickers": [ { "teacherId": 5, "teacherName": "李大模型", "roomName": "人工智能模型实训室" } ],
  "finalChoice": null,
  "needFinal": true }
```
> `needFinal=true` 表示 ≥2 位选中且未提交终选，前端弹终选框。

### 4.6 学生终选
`POST /api/student/final-choice`
```json
{ "teacherId": 5 }
```
前置校验（409）：阶段为 `STUDENT_FINAL`；≥2 位老师选中我；teacherId 在选中列表内；未提交过终选；该老师剩余名额 > 0。
成功后立即写入匹配结果（`matchSource=1`），学生状态变为已匹配。

### 4.7 我的最终结果
`GET /api/student/result` → `data`
```json
{ "matched": { ...同 4.1 matched... },
  "diversionHistory": [ { "roundNo": 1, "roundName": "第一轮初选", "records": [ { "step": 1, "teacherName": "...", "scopeType": 2, "result": 1 } ] } ] }
```

---

## 五、老师端 `/api/teacher`

### 5.1 我的档案与名额
`GET /api/teacher/profile` → `data`
```json
{ "teacherId": 1, "teacherNo": "TSD01", "realName": "林工", "roomId": 1, "roomName": "SD",
  "title": "副教授", "researchDirection": "...", "quota": 15, "used": 2, "remaining": 13 }
```

### 5.2 选我做志愿的学生（本轮 / 指定轮）
`GET /api/teacher/candidates?roundId=1`（roundId 可选，默认当前轮）→ `[PickVO]`
```json
[ { "studentId": 1, "studentNo": "20260001", "studentName": "张一", "major": "软件工程", "className": "软工1班",
    "pickSource": 1, "avgScore": 82.5, "createdAt": "..." } ]
```
> 仅返回本轮选了我做志愿的学生（US-20/21）；`pickSource=1` 的记录即"我已勾选"的暂选记录，前端以此回显勾选状态。

### 5.3 本轮我的面试记录
`GET /api/teacher/interviews?roundId=1` → `[InterviewVO]`（含学生信息）

### 5.4 安排面试
`POST /api/teacher/interviews`
```json
{ "studentId": 1, "interviewTime": "2026-11-15 09:00:00", "location": "SD 实训室 A301" }
```
前置：该生本轮选了我做志愿；interview 记录不存在时创建（status=1 已安排），已存在则更新时间地点。

### 5.5 录入面试成绩 / 评价
`PUT /api/teacher/interviews/{id}`
```json
{ "score": 85.5, "feedback": "基本功扎实", "status": 2 }
```
> score 可选；status 可省略（传 2 标记已完成）。成绩用于系统统筹排序（需求待确认项 #6 的实现）。

### 5.6 我本轮已选学生
`GET /api/teacher/picks?roundId=1` → `[PickVO]`

### 5.7 选择学生（阶段③，暂选）
`POST /api/teacher/picks`
```json
{ "studentId": 1 }
```
前置校验（409）：阶段为 `TEACHER_PICK`；该生本轮选了我当志愿；该生未被录取；剩余名额 > 0；未重复选择。
> 暂选不产生录取结果，等待学生终选/阶段结束自动匹配；阶段关闭前可撤销。

### 5.8 撤销选择
`DELETE /api/teacher/picks/{studentId}` — 仅 `TEACHER_PICK` 阶段可撤销。

### 5.9 调剂候选学生（分流阶段，US-27/28）
`GET /api/teacher/diversion/candidates` → `[DiversionCandidateVO]`
```json
[ { "studentId": 7, "studentNo": "20260007", "studentName": "吴七", "major": "数据科学", "className": "数科1班",
    "allowInternal": 1, "allowCrossRoom": 1, "avgScore": 79.3 } ]
```
> 返回：本轮未录取、服从调剂、且**调剂范围包含我**的学生（服从实训室内 → 我所在实训室的志愿学生；服从校内 → 全校）；名额已满时前端可提示。

### 5.10 调剂选择学生（立即录取）
`POST /api/teacher/diversion/picks`
```json
{ "studentId": 7 }
```
前置校验（409）：阶段为 `DIVERSION`；学生在我的调剂范围内；剩余名额 > 0。
成功即写入：`teacher_pick(source=2)` + `diversion_record(step=1, result=1)` + `match_result(source=2)`，学生立即被录取。

### 5.11 我的最终学生名单（US-29）
`GET /api/teacher/students` → `[MatchVO]`（含调剂来的学生，带 matchSource 区分来源）

---

## 六、管理端 `/api/admin`

### 6.1 基础数据管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/rooms` | 实训室列表（全部） |
| POST | `/admin/rooms` | 新建 `{name, code, description}` |
| PUT | `/admin/rooms/{id}` | 更新（含 status 启停） |
| GET | `/admin/teachers?page=&size=&keyword=&roomId=` | 老师分页 `{records, total, page, size}`，keyword 匹配姓名/工号 |
| POST | `/admin/teachers` | 新建老师 `{username, password, realName, phone, email, roomId, teacherNo, title, researchDirection, bio, quota}` |
| PUT | `/admin/teachers/{id}` | 更新老师（可改 quota/status/roomId 等） |
| GET | `/admin/students?page=&size=&keyword=` | 学生分页 |
| POST | `/admin/students` | 新建学生 `{username, password, realName, phone, email, studentNo, major, grade, className}` |
| PUT | `/admin/students/{id}` | 更新学生 |
| PUT | `/admin/users/{id}/enabled` | `{enabled: false}` 启用/禁用账号 |
| PUT | `/admin/users/{id}/reset-password` | `{newPassword}` 重置密码 |
| GET | `/admin/teachers/{id}/quota-logs` | 名额变动日志 |

### 6.2 流程阶段控制（US-33/34）

#### 流程总览
`GET /admin/flow/overview` → 结构同 `GET /api/public/flow`（含全部轮次与阶段）

#### 启动轮次
`POST /admin/flow/rounds/{roundId}/start`
- 前置：该轮未开始；前序轮次已全部结束
- 行为：创建 7 个阶段记录（CHOICE_OPEN~COMPLETED），开放 `CHOICE_OPEN`，轮次置为进行中

#### 进入下一阶段
`POST /admin/flow/rounds/{roundId}/next-stage`
- 按顺序推进：CHOICE_OPEN → CHOICE_CLOSED → INTERVIEW → TEACHER_PICK → STUDENT_FINAL → DIVERSION → COMPLETED
- **推进过 `STUDENT_FINAL` 时自动处理**（终选收尾）：
  - 被恰好 1 位老师选中 → 直接匹配（source=1）
  - 被 ≥2 位选中但未提交终选（超时）→ 按面试平均分降序、选择时间先后自动分配仍有名额的老师，标记 `timeout=1`；无可分配老师则视为未录取进入分流
  - 未被任何老师选中 → 成为分流候选
- **推进过 `DIVERSION` 时自动处理**（轮次收尾）：
  - 未安置学生 → 有下一轮则流转（无需操作）；是最后一轮 → 全部写入待选池（status=0 待约谈）
  - 轮次置为已结束
- 响应 `data`：
```json
{ "fromStage": "STUDENT_FINAL", "toStage": "DIVERSION",
  "autoMatched": 3, "autoTimeout": 0, "toWaitPool": 0, "toNextRound": 5 }
```

### 6.3 分流管理

#### 分流候选总览
`GET /admin/flow/rounds/{roundId}/diversion/candidates` → `[DiversionCandidateVO]` + 每人 `scopeText`（实训室内/校内）与当前状态（未被老师选走/已安置）

#### 启动系统统筹（US-37）
`POST /admin/flow/rounds/{roundId}/diversion/system-place`
- 前置：阶段为 `DIVERSION`
- 算法：候选学生按本轮面试平均分降序（无分按志愿提交时间）依次安置：
  1. **第一优先级（实训室内）**：志愿老师所属实训室中仍有名额的老师（含服从校内学生）
  2. **第二优先级（全院）**：全校有名额老师（仅服从校内学生）
- 仅服从实训室内、第一级失败 → 不进第二级
- 每步写 `diversion_record`，安置成功写 `match_result(source=3)`
- 响应 `data`：
```json
{ "internalPlaced": 1, "crossPlaced": 2, "unplaced": [ { "studentId": 15, "studentName": "杨阳" } ] }
```

#### 分流记录
`GET /admin/flow/rounds/{roundId}/diversion/records` → `[DiversionRecordVO]`

### 6.4 待选池（US-38/39）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/wait-pool?status=` | 列表（可按状态过滤），元素见 `WaitPoolVO` |
| PUT | `/admin/wait-pool/{studentId}/interview` | `{interviewAt}` 标记已约谈 |
| POST | `/admin/wait-pool/{studentId}/settle` | `{teacherId, remark}` 人工安置 → `match_result(source=4)`，池状态置已安置 |

> 安置前置：该老师剩余名额 > 0；学生未被安置过。

### 6.5 统计与导出（US-40/41）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/stats/summary` | 总览：学生总数 / 已匹配 / 未匹配 / 待选池 / 各来源匹配数 / 当前进度 |
| GET | `/admin/stats/by-room` | 各实训室：老师数、名额总数、已录取、剩余 |
| GET | `/admin/stats/by-teacher` | 各老师：quota/used/remaining/各来源录取数 |
| GET | `/admin/stats/by-round` | 各轮次：参与人数、匹配数（终轮归属） |
| GET | `/admin/stats/export` | 匹配结果 CSV 导出（`Content-Type: text/csv`，含 BOM），列：学号/姓名/专业/班级/老师/实训室/来源/确定轮次/时间 |

### 6.6 系统配置
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/config` | 全部配置项 |
| PUT | `/admin/config/{key}` | `{value}` 更新（max_supplement_rounds / max_choices_per_round / teacher_default_quota / final_choice_timeout_min / choice_modify_enabled） |

---

## 七、关键业务时序（联调参考）

### 7.1 第一轮完整走查
```
1. 管理员 POST /admin/flow/rounds/1/start                       → CHOICE_OPEN 开放
2. 学生   GET  /public/teachers                                  → 浏览老师
   学生   POST /student/choices {teacherIds:[5,6,1], allowInternal:true, allowCrossRoom:true}
3. 管理员 POST /admin/flow/rounds/1/next-stage ×2                → 进入 INTERVIEW
4. 老师   GET  /teacher/candidates → POST /teacher/interviews → PUT /teacher/interviews/{id}
5. 管理员 POST /admin/flow/rounds/1/next-stage                   → TEACHER_PICK
6. 老师   GET  /teacher/candidates → POST /teacher/picks {studentId}
7. 管理员 POST /admin/flow/rounds/1/next-stage                   → STUDENT_FINAL
8. 学生   GET  /student/pickers → POST /student/final-choice {teacherId}（≥2 时）
9. 管理员 POST /admin/flow/rounds/1/next-stage                   → DIVERSION（自动：1 人选中直接匹配等）
10. 老师  GET  /teacher/diversion/candidates → POST /teacher/diversion/picks（名额未满可捞人）
11. 管理员 POST /admin/flow/rounds/1/diversion/system-place      → 系统统筹剩余服从调剂学生
12. 管理员 POST /admin/flow/rounds/1/next-stage                  → COMPLETED（未安置学生流转/进池）
13. 重复 1~12 于 rounds/2、rounds/3；第 3 轮收尾后未安置者自动入待选池
14. 管理员 GET/PUT /admin/wait-pool/**                           → 约谈、人工安置
```

### 7.2 学生状态判断要点（前端）
- `status.code = WAIT_FINAL` → 渲染终选弹窗（调 4.5/4.6）
- `status.code` 含 `DIVERSION` → 展示分流进度说明
- `matched != null` → 展示录取结果；`waitPool != null` → 展示待选池卡片
- 志愿表单仅在 `stage.stageName == 'CHOICE_OPEN'` 时可编辑

---

## 八、与需求待确认项（PRD §八）的本次实现口径

| 待确认项 | 实现口径 |
|----------|----------|
| 1. 补录轮是否限定名额>0 的老师 | 不限定，学生可自由选 3 名（前端展示剩余名额引导） |
| 2. 补录轮数 | 可配置 `max_supplement_rounds`（默认 2，共 3 轮） |
| 3. 补录轮调剂意愿 | 每轮重新提交（随志愿一起提交） |
| 5/6. 统筹排序 & 面试成绩 | 面试平均分降序 → 志愿提交时间升序；成绩由老师面试阶段录入 |
| 7. 终选超时 | 管理员推进阶段时自动分配（按面试分降序 + 有名额优先），标记 timeout=1 |
