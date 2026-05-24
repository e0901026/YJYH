# Phone Loan Backend

V0.5 后端 MVP，目标是先跑通真实服务端核心业务，为 V0.6 Android 联调做准备。

## 本地运行

需要 PostgreSQL，并设置连接信息：

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

## 验证

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew test
```
