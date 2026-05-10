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
gradle assembleDebug
gradle testDebugUnitTest
gradle lintDebug
```

如果本机没有 Android SDK，需要先安装 Android Studio 或 Android command line tools，并配置 `ANDROID_HOME`。
