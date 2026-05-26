# V0.6 Android 人工验收包

## 1. 产物

- APK：`android-app/app/build/outputs/apk/debug/app-debug.apk`
- 后端：本地 Spring Boot `local` profile，地址 `http://localhost:8080`
- 模拟器访问后端地址：`http://10.0.2.2:8080`

## 2. 测试账号

| 角色 | 工号 | 密码 |
|------|------|------|
| Owner | `10086` | `password123` |
| 普通用户 | `10248` | `password123` |
| 普通用户 | `10881` | `password123` |

默认邀请码：

- `OWNER-SEED-0001`

## 3. 本地启动

启动后端：

```sh
./scripts/run-local-backend.sh
```

构建、安装并打开 APK：

```sh
./scripts/build-install-debug-apk.sh
```

也可以手动执行：

```sh
cd android-app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.yjyh.phoneloan/.MainActivity
```

## 4. 验收路径

1. 启动 App，确认登录页展示默认工号 `10086` 和密码。
2. 点击登录，确认进入首页。
3. 首页确认最近动态为“已连接本地后端，数据来自真实 API”。
4. 首页确认手上持有为 `2 台`，待处理为 `2 条`。
5. 进入设备页，确认有 3 台设备。
6. 进入一键还，确认“我借出去的”和“我借入的”各有记录。
7. 在“我借出去的”点击“催还机”，确认出现已催还反馈。
8. 在“我借入的”点击“一键还”，确认出现已归还反馈。
9. 进入我的页，确认账号信息和 Owner 管理入口可打开。
10. 回到登录页或重启 App 后，使用错误密码登录，确认出现错误提示。

## 5. 日志与埋点检查

查看 App 崩溃和埋点上传失败：

```sh
$ANDROID_HOME/platform-tools/adb logcat -d -t 700 | rg "PhoneLoanAnalytics|AndroidRuntime|FATAL EXCEPTION|Cleartext|NetworkOnMainThread|remote_.*failed|login_failed"
```

查看本地埋点队列是否清空：

```sh
$ANDROID_HOME/platform-tools/adb shell run-as com.yjyh.phoneloan wc -l files/analytics-events.jsonl
```

期望：

- 无 `FATAL EXCEPTION`。
- 无 `Cleartext HTTP traffic`。
- 无 `NetworkOnMainThreadException`。
- 队列行数为 `0`，表示事件已上报到后端。

## 6. 本地门禁

每次交付前执行：

```sh
cd android-app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug

cd ../backend
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew test
```

## 7. 已知限制

- 当前是 Debug APK，未做正式签名和生产发布配置。
- 当前后端 `local` profile 使用 H2 文件数据库，适合本地验收；正式环境仍应使用 PostgreSQL。
- 扫码借已具备 CameraX / ML Kit 能力，但真实业务边界错误提示还需要继续补齐。

## 8. 本次 Agent 验证

验证日期：2026-05-26

已通过：

- `./scripts/run-local-backend.sh` 可启动本地后端。
- `./scripts/build-install-debug-apk.sh` 可构建、安装并启动 APK。
- Android `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 通过。
- 后端 `./gradlew test` 通过。
- 模拟器点击登录后进入首页。
- 首页显示“已连接本地后端，数据来自真实 API”。
- Logcat 未发现 `FATAL EXCEPTION`、明文 HTTP 阻断或主线程网络错误。
- 本地埋点队列为 `0`。
