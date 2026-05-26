# Phone Loan Backend

V0.5 后端 MVP，目标是先跑通真实服务端核心业务，为 V0.6 Android 联调做准备。

## 本地运行

推荐先使用 `local` profile，本地会使用 H2 文件数据库，不依赖 PostgreSQL：

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

也可以直接从项目根目录执行：

```sh
./scripts/run-local-backend.sh
```

如需验证 PostgreSQL 配置，再设置连接信息：

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export DB_URL="jdbc:postgresql://localhost:5432/phone_loan"
export DB_USERNAME="phone_loan"
export DB_PASSWORD="phone_loan"
./gradlew bootRun
```

开发默认会创建一个 Owner 账号和一个初始邀请码：

- 工号：`10086`
- 密码：`password123`
- 邀请码：`OWNER-SEED-0001`

`local` profile 还会补充 3 台演示设备和 2 条活跃借还记录，用于 Android 人工验收。

## 验证

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew test
```
