# AI+PBL 师生双向选择系统 — 后端

> 技术栈：Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + MySQL 8 + JWT + Spring Security
> Java 17 | 接口契约见 `../docs/API接口文档.md`

## 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | `java -version` 确认 |
| Maven | 3.9+ | 构建/启动用；本机没装可改用 IDE（IDEA 自带 Maven）执行 `spring-boot:run` |
| MySQL | 8.0+ | 本地 3306 可连，库名 `leb-sele`（脚本会自动创建） |

## 快速启动

```bash
# 1. 准备数据库（在仓库根目录执行；先建库建表，再导入演示数据）
mysql -uroot -p < sql/schema.sql      # 建库建表（含 6 实训室/3 轮次/5 配置）
mysql -uroot -p < sql/seed_demo.sql   # 可选：演示数据（12 老师 20 学生，密码均 123456）

# 2. 配置数据库连接（无需改代码，用环境变量覆盖）
#    PowerShell：
$env:DB_PASSWORD="你的MySQL密码"
#    macOS / Linux：
#    export DB_PASSWORD=你的MySQL密码

# 3. 进入 backend 目录启动（两种方式任选）
mvn spring-boot:run
# 或
mvn -DskipTests package && java -jar target/selection-backend-1.0.0.jar
```

支持的环境变量：`DB_HOST`（默认 localhost）、`DB_PORT`（3306）、`DB_NAME`（leb-sele）、
`DB_USERNAME`（root）、`DB_PASSWORD`（默认 123456）、`JWT_SECRET`。

- 服务地址：`http://localhost:8080`
- 接口文档（Swagger）：`http://localhost:8080/swagger-ui.html`
- 登录：`POST /api/auth/login`，其余接口带 `Authorization: Bearer <token>`

## 冒烟测试

后端启动后，在 `backend/` 目录执行（需 curl，Git Bash / macOS / Linux 均可）：

```bash
bash smoke-test.sh http://localhost:8080
```

走通第一轮完整流程（启动轮次 → 填志愿 → 面试打分 → 老师选人 → 终选自动匹配），
最后一步 `status=MATCHED` 即通过。注意：需使用未导入演示数据（或已清空流水表）的库，否则轮次状态冲突。

完整回归测试（105 项接口级检查，需独立测试库）：见 [test/README.md](test/README.md)。

## 演示账号（导入 seed_demo.sql 后）

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 老师 | T-SD01 / T-AI01 / T-RB01 ...（工号见 seed） | 123456 |
| 学生 | S-A01 ~ S-I02（20 名） | 123456 |

## 目录结构

```
src/main/java/com/pbl/selection/
├── SelectionApplication.java
├── config/        # Security(JWT) / MyBatis-Plus / Swagger 配置
├── common/        # Result 统一响应 / BizException / 全局异常 / 枚举
├── security/      # JwtUtil / JwtAuthFilter / LoginUser
├── entity/        # 16 张表实体（与 sql/schema.sql 对应）
├── mapper/        # MyBatis-Plus Mapper（含联表查询）
├── dto/           # 请求对象（含参数校验）
├── vo/            # 响应对象
├── service/       # 业务逻辑（见下）
└── controller/    # auth / public / student / teacher / admin×4
```

### 核心服务

| 服务 | 职责 |
|------|------|
| `FlowService` | 当前轮次/阶段、流程总览（阶段机：CHOICE_OPEN→…→COMPLETED，7 阶段） |
| `RoundFlowService` | 启动轮次、阶段推进 + 自动处理钩子（终选收尾/轮次收尾） |
| `ChoiceService` | 志愿填报（平行志愿 ≤3、跨实训室）+ 调剂意愿（可多选/单选/不选） |
| `InterviewService` | 面试安排、成绩录入（用于统筹排序） |
| `PickService` | 老师选学生（仅限选自己当志愿的学生，受名额约束） |
| `FinalChoiceService` | ≥2 终选 / =1 直接匹配 / 超时自动分配（面试分降序→名额降序） |
| `DiversionService` | 每轮后分流：①调剂范围内老师选（即录）②系统统筹（实训室内→全院）③流转下一轮/待选池 |
| `WaitPoolService` | 待选池约谈、人工安置（match_source=4） |
| `QuotaService` | 剩余名额 = 上限 − 已录取 − 本轮暂选未落地 |
| `StatsService` | 统计（按实训室/老师/轮次）+ CSV 导出 |

## 关键设计决策（对应 PRD §八 待确认项）

1. **补录轮不限定名额>0 的老师**：学生可自由选 3 名，前端展示剩余名额引导（`GET /api/public/teachers`）。
2. **补录轮数可配置**：`system_config.max_supplement_rounds`（默认 2，共 3 轮）。
3. **调剂意愿每轮重新提交**：随志愿一起全量提交（`POST /api/student/choices`）。
4. **统筹排序规则**：面试平均分降序 → 志愿提交时间升序；老师侧优先剩余名额多者（并列取 id 小）。
5. **终选超时**：管理员推进阶段时自动分配（面试分降序 + 有名额优先），`timeout=1` 标记。
6. **暂选占名额**：老师选学生（阶段③）为暂选，占用名额；学生被其他老师录取后自动释放。
7. **待选池触发**：最后一轮 `DIVERSION → COMPLETED` 推进时，未安置学生自动入池（status=0 待约谈）。

## 注意事项

- 演示数据中 3 个轮次均为"已结束"状态（历史剧本）；如需走通完整流程，请仅导入 `schema.sql`，
  然后按接口文档 §7.1 时序从 `POST /api/admin/flow/rounds/1/start` 开始驱动。
- 老师名额并发场景（多名老师同时抢同一学生）已用 `match_result` 学生唯一键兜底，重复录取返回 409。
