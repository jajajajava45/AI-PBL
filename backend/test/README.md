# 端到端测试（E2E）

`e2e.ps1` 覆盖 105 项接口级检查（鉴权 / 志愿 / 面试 / 选人 / 终选 / 分流 / 三轮流转 / 待选池 / 统计与基础数据），
**必须在独立测试库上运行**，不要指向开发库 `leb-sele`。

## 1. 建测试库 `leb-sele-test`

```bash
# macOS / Linux / Git Bash（在 backend 目录执行）
sed 's/leb-sele/leb-sele-test/g' ../sql/schema.sql    > /tmp/schema_test.sql
sed 's/leb-sele/leb-sele-test/g' ../sql/seed_demo.sql > /tmp/seed_test.sql
mysql -uroot -p < /tmp/schema_test.sql
mysql -uroot -p < /tmp/seed_test.sql
mysql -uroot -p < test/reset.sql
```

Windows PowerShell 版：

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
$s = (Get-Content ..\sql\schema.sql -Raw -Encoding UTF8) -replace 'leb-sele','leb-sele-test'
[System.IO.File]::WriteAllText("$env:TEMP\schema_test.sql", $s, $enc)
$d = (Get-Content ..\sql\seed_demo.sql -Raw -Encoding UTF8) -replace 'leb-sele','leb-sele-test'
[System.IO.File]::WriteAllText("$env:TEMP\seed_test.sql", $d, $enc)
mysql -uroot -p < "$env:TEMP\schema_test.sql"
mysql -uroot -p < "$env:TEMP\seed_test.sql"
mysql -uroot -p < test/reset.sql
```

`reset.sql` 会清空流水表（志愿/面试/选人/终选/分流/匹配/待选池/名额日志）并把三轮状态复位，
因为 `seed_demo.sql` 里三轮均为"已结束"，不复位无法从第一轮重新驱动。

## 2. 启动测试实例（8081 + 测试库）

```powershell
cd backend
$env:DB_PASSWORD="你的MySQL密码"; $env:DB_NAME="leb-sele-test"; $env:SERVER_PORT="8081"
java -jar target/selection-backend-1.0.0.jar
```

## 3. 运行

```powershell
powershell -ExecutionPolicy Bypass -File test/e2e.ps1
# 指定地址：-Base http://localhost:8080
```

期望输出：

```
===== 结果：通过 105 / 失败 0 =====
```

脚本以失败数作为退出码（`exit $script:fail`），可直接接入 CI。

## 4. 重跑

先重新导入 `test/reset.sql` 复位数据，再执行脚本；否则轮次状态冲突会导致前段用例失败。

## 注意

- 脚本会新增老师/学生/实训室、修改配置、禁用并启用账号、重置密码，**只能对测试库执行**。
- 建议每次从 `schema.sql` + `seed_demo.sql` + `reset.sql` 全新建库，保证结果可复现。
- 依赖 Windows PowerShell 5.1+（脚本以 UTF-8 with BOM 保存，避免 PS5 中文乱码）。
