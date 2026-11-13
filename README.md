# AI-PBL 师生双向选择系统

AI+PBL 师生双向选择系统（Spring Boot 3.3.5 + MyBatis-Plus + MySQL 8 + JWT）。

## 目录说明

| 目录 | 内容 |
|------|------|
| `backend/` | 后端服务（启动步骤见 [backend/README.md](backend/README.md)） |
| `sql/` | `schema.sql`（建库建表）、`seed_demo.sql`（演示数据） |
| `docs/` | 需求文档、用户故事、API 接口文档（前后端接口契约） |

## 快速开始

```bash
mysql -uroot -p < sql/schema.sql
mysql -uroot -p < sql/seed_demo.sql   # 可选
cd backend
mvn spring-boot:run                   # 需 JDK 17 + Maven 3.9+ + MySQL 8
```

数据库密码通过环境变量 `DB_PASSWORD` 传入，详见 `backend/README.md`。
服务默认端口 `8080`，Swagger：`http://localhost:8080/swagger-ui.html`。
