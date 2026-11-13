#!/usr/bin/env bash
# 一键冒烟测试：走通第一轮完整流程（需先启动后端 + MySQL，并只导入 schema.sql）
# 用法: bash smoke-test.sh [BASE_URL]   默认 http://localhost:8080
set -e
BASE=${1:-http://localhost:8080}

req() { # method path token body
  local m=$1 p=$2 t=$3 b=$4
  if [ -n "$b" ]; then
    curl -s -X "$m" "$BASE$p" -H "Content-Type: application/json" \
      ${t:+-H "Authorization: Bearer $t"} -d "$b"
  else
    curl -s -X "$m" "$BASE$p" ${t:+-H "Authorization: Bearer $t"}
  fi
}

echo "== 1. 管理员登录 =="
ADMIN_TOKEN=$(req POST /api/auth/login "" '{"username":"admin","password":"123456"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "$ADMIN_TOKEN" | head -c 30; echo " ..."

echo "== 2. 启动第一轮 =="
req POST /api/admin/flow/rounds/1/start "$ADMIN_TOKEN"

echo "== 3. 学生登录 + 查老师 =="
STU_TOKEN=$(req POST /api/auth/login "" '{"username":"S-A01","password":"123456"}')
STU_TOKEN=$(echo "$STU_TOKEN" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
req GET /api/public/teachers "$STU_TOKEN" | head -c 200; echo " ..."

echo "== 4. 学生填志愿（AI 两位老师 + SD 林工，服从校内调剂）=="
req POST /api/student/choices "$STU_TOKEN" '{"teacherIds":[5,6,1],"allowInternal":true,"allowCrossRoom":true}'

echo "== 5. 推进到面试 → 老师安排面试并打分 =="
req POST /api/admin/flow/rounds/1/next-stage "$ADMIN_TOKEN"   # CHOICE_OPEN -> CHOICE_CLOSED
req POST /api/admin/flow/rounds/1/next-stage "$ADMIN_TOKEN"   # -> INTERVIEW
T_TOKEN=$(req POST /api/auth/login "" '{"username":"T-SD01","password":"123456"}')
T_TOKEN=$(echo "$T_TOKEN" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
req POST /api/teacher/interviews "$T_TOKEN" '{"studentId":1,"interviewTime":"2026-11-15 09:00:00","location":"SD-A301"}'
IV_ID=$(req GET /api/teacher/interviews "$T_TOKEN" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
req PUT /api/teacher/interviews/$IV_ID "$T_TOKEN" '{"score":85,"feedback":"不错","status":2}'

echo "== 6. 老师选学生 =="
req POST /api/admin/flow/rounds/1/next-stage "$ADMIN_TOKEN"   # -> TEACHER_PICK
req POST /api/teacher/picks "$T_TOKEN" '{"studentId":1}'

echo "== 7. 终选阶段（1 位老师选中 → 自动直接匹配）=="
req POST /api/admin/flow/rounds/1/next-stage "$ADMIN_TOKEN"   # -> STUDENT_FINAL，进入时自动匹配

echo "== 8. 学生查看状态 =="
req GET /api/student/status "$STU_TOKEN" | head -c 400; echo

echo "== 完成：若第 8 步 status.code=MATCHED，冒烟通过 =="
