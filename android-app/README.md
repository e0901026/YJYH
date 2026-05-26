# 手机借机管理 Android App

Android V0.2 工程壳。

## 技术栈

- Kotlin
- Jetpack Compose
- 单 Activity
- Navigation Compose
- MVVM-ready 结构
- mock repository

## V0.2 范围

- 登录 / 注册页面壳。
- 首页 / 设备 / 我的底部导航。
- 扫码借、手机注册、一键还、设备详情、Owner 用户列表、Owner 邀请码页面路由。
- 全局低保真主题、按钮、卡片、Tab 组件。
- mock 数据展示。

暂不包含：

- 真实登录。
- 真实相机扫码。
- 真实后端 API。
- 真实通知推送。

## 本地命令

```sh
cd android-app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"

./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Debug APK 输出位置：

```txt
android-app/app/build/outputs/apk/debug/app-debug.apk
```

如果本机没有 Android SDK，需要先安装 Android Studio 或 Android command line tools，并配置 `ANDROID_HOME`。首次执行构建时，Gradle 可能会自动安装缺失的 SDK Platform / Build Tools。
# Phone Loan Android

## 本地构建

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug
```

Debug APK：

```txt
app/build/outputs/apk/debug/app-debug.apk
```

## 构建并安装

从项目根目录执行：

```sh
./scripts/build-install-debug-apk.sh
```

App 默认连接模拟器可访问的本地后端地址：

```txt
http://10.0.2.2:8080
```

请先启动后端：

```sh
./scripts/run-local-backend.sh
```
