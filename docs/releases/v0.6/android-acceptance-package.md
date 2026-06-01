# V0.6 Android 人工验收包

## 1. 产物

- APK：`android-app/app/build/outputs/apk/debug/app-debug.apk`
- 本轮验收 APK：`releases/v0.6/YJYH-phone-loan-v0.6.0-debug.apk`
- 真机验收 APK：`releases/v0.6/YJYH-phone-loan-v0.6.0-lan-debug.apk`
- APK 版本：`versionCode=6`，`versionName=0.6.0`
- SHA-256：`74c4bb8c5afcba79ae81a05c00d7ae1a22a489676bdc97206caae75f6497d677`
- 真机验收 APK SHA-256：`f0426a75293df77f2b6d5cfc9dc08555d040539dc17ce1e48e3e59b662a227b5`
- 后端：本地 Spring Boot `local` profile，地址 `http://localhost:8080`
- 模拟器访问后端地址：`http://10.0.2.2:8080`
- 真机访问后端地址：使用 Mac 的局域网 IP，例如 `http://192.168.0.110:8080`

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

构建真机验收 APK：

```sh
./scripts/build-lan-debug-apk.sh
```

如果 Mac 的局域网 IP 不是自动识别结果，可以手动指定：

```sh
LAN_IP=192.168.0.110 ./scripts/build-lan-debug-apk.sh
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
- 扫码借已具备 CameraX / ML Kit 能力；注册手机、确认借走、一键还、催还机已有真实 API 失败提示。
- `10.0.2.2` 只适用于 Android 模拟器访问 Mac 本机；真机必须安装 LAN APK，并确保手机和 Mac 连接同一个 Wi-Fi。
- 真机验收前必须启动本地后端，并允许 Mac 防火墙接受 Java / Spring Boot 的局域网访问。

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
- 一键还页面点击“催还机”后成功反馈可见。

## 9. 2026-05-31 最终 APK 包验证

本轮输出：

- APK：`releases/v0.6/YJYH-phone-loan-v0.6.0-debug.apk`
- 大小：约 `19M`
- SHA-256：`74c4bb8c5afcba79ae81a05c00d7ae1a22a489676bdc97206caae75f6497d677`

验证命令：

```sh
cd android-app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug

cd ../backend
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew test

cd ..
./scripts/run-local-backend.sh
./scripts/build-install-debug-apk.sh
```

验证结果：

- Android 构建、单测、lint 通过。
- 后端测试通过。
- APK 安装成功，并启动 `com.yjyh.phoneloan/.MainActivity`。
- 模拟器包信息为 `versionCode=6`、`versionName=0.6.0`。
- 登录页展示默认工号 `10086` 和密码占位。
- 点击登录后进入首页。
- 首页展示 `2 台`、`2 条` 和“已连接本地后端，数据来自真实 API。”。
- Logcat 未见 `FATAL EXCEPTION`、明文 HTTP 阻断或主线程网络错误。
- 本地埋点队列为 `0`。

## 10. 2026-06-01 真机连接修正

问题现象：

- 真机安装原模拟器 APK 后，登录失败并提示 `failed to connect to /10.0.2.2 (port 8080)`。

根因：

- `10.0.2.2` 是 Android 模拟器访问宿主机的特殊地址；真机上该地址无效。

修正：

- Android 构建支持通过 `API_BASE_URL` 或 `-PapiBaseUrl` 注入后端地址，默认仍保留模拟器地址 `http://10.0.2.2:8080`。
- 新增 `./scripts/build-lan-debug-apk.sh`，自动使用 Mac 局域网 IP 生成真机验收 APK。
- 当前真机验收 APK 指向 `http://192.168.0.110:8080`。

验证：

- Android `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 通过。
- 后端 `./gradlew test` 通过。
- LAN APK 构建成功，SHA-256 为 `f0426a75293df77f2b6d5cfc9dc08555d040539dc17ce1e48e3e59b662a227b5`。
- 本地后端启动后，`http://localhost:8080/api/devices` 和 `http://192.168.0.110:8080/api/devices` 均返回 `200`。
