# 手机借机管理 App — Android 技术架构

> 目的：确认 Android 优先版本的技术选型、工程结构、分层方式和测试策略，避免进入开发后反复摇摆。

## 1. 架构结论

当前项目优先采用：

- 平台：Android 原生 App。
- 语言：Kotlin。
- UI：Jetpack Compose。
- 架构模式：单 Activity + Navigation Compose + MVVM。
- 异步：Kotlin Coroutines + StateFlow。
- 数据源：V0.2 / V0.3 先使用内存 mock repository，后续再替换为真实 API。
- 扫码：Android 相机权限 + CameraX + ML Kit Barcode Scanning，优先支持二维码 / 条码解析 IMEI。
- 本地存储：DataStore 保存轻量登录态和用户偏好；如后续需要离线借用记录，再引入 Room。
- 网络：等真实后端接口确定后再引入 Retrofit / OkHttp。
- 测试：单元测试 + Compose UI 测试 + Android 真机冒烟测试。

## 2. 为什么这样选

- 本项目优先 Android，原生 Kotlin 能最直接处理相机权限、扫码和真机能力。
- Jetpack Compose 更适合快速还原当前移动端低保真原型，也方便沉淀按钮、卡片、Tab、底部导航等组件。
- 单 Activity + Navigation Compose 足够支撑当前 11 个页面，不需要复杂多模块。
- MVVM 能把页面状态和业务动作分开，后续从 mock 数据切换真实 API 时风险较低。
- V0.2 / V0.3 先 mock 数据，可以先验证原型还原和业务流程，不被后端阻塞。

## 3. V0.2 工程目标

V0.2 只搭可运行 Android 壳子，不做真实后端。

必须完成：

- Android 工程能构建和启动。
- 首页、设备、我的三栏底部导航。
- 登录、注册、首页、扫码借、手机注册、一键还、设备列表、设备详情、我的、Owner 用户列表、Owner 邀请码页面路由。
- 全局主题：颜色、字号、卡片、按钮、Tab 风格对齐原型。
- 页面先使用 mock 数据填充。
- 提供运行方式和 V0.2 测试清单。

暂不完成：

- 真实登录接口。
- 真实扫码识别。
- 真实消息通知。
- 真实后端持久化。
- iOS / Web 版本。

## 4. 推荐目录结构

```txt
android-app/
  app/
    src/main/
      java/com/yjyh/phoneloan/
        MainActivity.kt
        app/
          PhoneLoanApp.kt
          AppNavGraph.kt
          AppRoute.kt
        core/
          design/
            AppTheme.kt
            AppColors.kt
            AppTypography.kt
            Components.kt
          model/
            Device.kt
            User.kt
            InviteCode.kt
            LoanRecord.kt
          data/
            MockPhoneLoanRepository.kt
            PhoneLoanRepository.kt
        feature/
          auth/
          home/
          scanborrow/
          registerdevice/
          returnloan/
          devices/
          profile/
          owner/
```

说明：

- `core/design`：承接 `docs/system-design-system.md` 和 Pencil 原型的视觉规范。
- `core/model`：放业务实体。
- `core/data`：先放 mock repository，后续替换真实 API。
- `feature/*`：按页面或业务域组织 UI 和 ViewModel。

## 5. 页面与路由

| 路由 | 页面 | 是否显示底部导航 |
|------|------|------------------|
| `login` | 登录 | 否 |
| `register` | 注册 | 否 |
| `home` | 首页 | 是 |
| `scanBorrow` | 扫码借 - 识别与确认 | 否 |
| `registerDevice` | 手机注册 | 否 |
| `returnLoan` | 一键还 | 否 |
| `devices` | 设备列表 | 是 |
| `deviceDetail/{id}` | 设备详情 | 否 |
| `profile` | 我的 | 是 |
| `ownerUsers` | Owner - 用户列表 | 否 |
| `ownerInvites` | Owner - 邀请码 | 否 |

底部导航只在顶层主页面显示：首页、设备、我的。

## 6. 数据模型草案

```kotlin
data class User(
    val id: String,
    val employeeNo: String,
    val name: String,
    val role: UserRole,
    val inviteUsed: Int,
    val inviteLimit: Int = 10,
)

data class Device(
    val id: String,
    val name: String,
    val imei1: String,
    val imei2: String?,
    val owner: UserSummary,
    val currentHolder: UserSummary?,
    val status: DeviceStatus,
)

data class LoanRecord(
    val id: String,
    val deviceId: String,
    val borrower: UserSummary,
    val previousHolder: UserSummary?,
    val startedAt: String,
    val endedAt: String?,
)
```

## 7. 扫码能力方案

V0.2：

- 先做扫码页面壳、权限入口和 mock 识别结果。
- 页面要能模拟“已建档 IMEI”和“未建档 IMEI”两条路径。

V0.3：

- 接入 CameraX 和 ML Kit Barcode Scanning。
- 支持相机权限申请、拒绝提示、重新授权引导。
- 解析二维码 / 条码内容后提取 IMEI。
- 无法解析时提供手动输入 IMEI 兜底。

注意：

- Android 10 以后不能随意读取设备自身 IMEI；本项目不是读取本机 IMEI，而是扫描被借手机工程界面展示的 IMEI 条码/二维码。
- 完整 IMEI 属于敏感信息，UI 展示和日志输出需要谨慎。

## 8. 状态管理

- 每个页面使用 ViewModel 暴露 `StateFlow<UiState>`。
- UI 只渲染状态，不直接处理业务数据变更。
- 用户操作通过 ViewModel 方法触发，例如 `confirmBorrow()`、`returnDevice()`、`applyInviteCode()`。
- V0.2 / V0.3 使用 mock repository 保持页面状态可运行。

## 9. 测试策略

V0.2：

- 构建测试：`./gradlew assembleDebug`
- 页面冒烟：App 可启动，主路由可切换。
- Compose UI 测试：底部导航、页面标题、关键按钮存在。
- 原型对照：页面结构、颜色、文案与 Pencil 原型一致。

V0.3：

- 业务单元测试：借机、还机、邀请码配额、设备注册状态变化。
- UI 流程测试：登录后进入首页、扫码借分支、一键还。
- 真机测试：相机权限、扫码入口、返回键、不同 Android 屏幕适配。

## 10. 后端接口边界

当前阶段不强依赖后端。

后续需要的接口方向：

- 登录 / 注册。
- 当前用户信息。
- 设备列表 / 详情。
- 根据 IMEI 查询设备。
- 注册设备。
- 确认借走。
- 一键归还。
- 邀请码申请。
- Owner 用户列表。
- Owner 邀请码生成与列表。

接口未确定前，Android 端通过 repository 接口隔离数据源。

## 11. 技术风险

| 风险 | 应对 |
|------|------|
| 扫描不同手机工程界面中的 IMEI 格式不统一 | V0.3 增加解析规则和手动输入兜底 |
| Android 相机权限被拒绝 | 提供拒绝提示和重新授权路径 |
| 原型低保真与真实 Compose 控件细节不同 | 以原型结构、文案、层级和视觉主风格为准 |
| 后端接口未准备好 | mock repository 先跑通页面和业务流 |
| IMEI 敏感信息泄露 | 避免日志输出完整 IMEI，展示遵循 PRD 权限要求 |

## 12. 开发准入

开始写 Android 代码前，需要确认：

- V0.1 原型已被用户确认作为开发基准。
- 本技术架构已被用户确认。
- V0.2 范围仅为 Android 工程壳、基础页面、导航和主题。
- 测试按 `docs/android-test-plan.md` 执行。
