# Changelog

本文件记录项目所有需求、原型、设计规范、架构、代码、测试和流程类变更。任何改动缺少 changelog 都不能视为完整交付。

## 2026-05-11

### 流程纠偏：代码改动前必须先确认原型

- 类型：流程 / 需求 / 原型。
- 内容：明确任何影响界面、交互、文案或业务流程的代码改动，都必须先更新 PRD、Pencil 原型和预览导出，并经用户确认后再进入开发。
- 影响范围：`Agent.md`、后续所有 Android 前端开发任务。
- 用户确认：待确认。
- 是否进入开发：否。该规则先作为流程约束生效，后续开发必须遵守。
- 教训：不能因为修正看似简单就直接改代码；否则原型会失去作为开发依据的意义。Pencil 改完必须显式保存，并用 `git status` 确认 `.pen` 源文件进入版本差异。

### 一键还页面：借出 Tab 列表项信息修正

- 类型：需求 / 原型。
- 内容：借出 Tab 的列表项不展示「我借出去的」「我借入的」身份标签，也不提供「一键还」动作；应展示设备名、当前持有人、持有人工号、IMEI、持有天数，并在右侧提供「催还机」。借入 Tab 才提供「一键还」动作。
- 影响范围：`docs/PRD-手机借机管理.md`、`prototype/手机借机管理.pen`、`prototype/exports/CjA4k.png`。
- 用户确认：已确认。
- 是否进入开发：是。Android 代码已开始按确认后的原型修正。

### 一键还页面：催还机动作新增

- 类型：需求 / 原型。
- 内容：在「我借出去的」列表项右侧新增「催还机」按钮，点击后给当前持有人发送催还消息，不改变设备归还状态；「我借入的」列表项右侧按钮文案保持「一键还」。
- 影响范围：`docs/PRD-手机借机管理.md`、`prototype/手机借机管理.pen`、`prototype/exports/CjA4k.png`。
- 用户确认：已确认。
- 是否进入开发：是。Android 代码已开始按确认后的原型修正。

### 一键还页面：Android 按确认原型实现

- 类型：代码 / 测试。
- 内容：Android 一键还页面按确认后的原型实现：借出 Tab 列表项展示设备名、当前持有人、工号、IMEI、持有天数，右侧为「催还机」；借入 Tab 右侧为「一键还」。
- 影响范围：`android-app/app/src/main/java/com/yjyh/phoneloan/feature/returnloan/ReturnLoanScreen.kt`、`docs/v0.3-android-test-checklist.md`。
- 验证：`assembleDebug`、`lintDebug`、`testDebugUnitTest` 通过；模拟器功能路径通过，但视觉验收未通过，按钮出现双行/显示不全。
- 用户确认：开发后待人工验收。
- 是否进入开发：是。

### 测试纠偏：界面自测必须截图对照原型

- 类型：流程 / 测试。
- 内容：补充 Android 界面自测规则，要求所有界面改动在提交用户确认前必须截图对照 Pencil 导出图；按钮、Tab、列表项必须检查文字是否单行、完整、未裁剪。
- 影响范围：`Agent.md`、`docs/android-test-plan.md`、`docs/v0.3-android-test-checklist.md`、`docs/project-management.md`。
- 用户确认：用户已指出问题，流程规则立即生效。
- 是否进入开发：否。下一步先按已确认原型修复按钮视觉问题，再重新截图自测。

### 一键还页面：按钮视觉修复

- 类型：代码 / 测试。
- 内容：修复「催还机」「一键还」列表内快捷按钮因 Material 默认内边距导致的双行/显示不全问题；按钮按 Pencil 原型保持蓝底、8dp 圆角、单行完整显示。
- 影响范围：`android-app/app/src/main/java/com/yjyh/phoneloan/feature/returnloan/ReturnLoanScreen.kt`、`docs/v0.3-android-test-checklist.md`、`docs/test-artifacts/v0.3/*.png`。
- 验证：`assembleDebug`、`lintDebug`、`testDebugUnitTest` 通过；模拟器截图验证按钮单行完整显示；一键还和催还机功能回归通过。
- 用户确认：开发后待人工验收。
- 是否进入开发：是。

### V0.3 扫码借 / 手机注册闭环自测

- 类型：测试。
- 内容：完成扫码借已建档路径、未建档注册路径和设备列表回归自测。已建档 IMEI 可展示设备详情并确认借走，首页手上持有台数、待处理和最近动态同步更新；未建档 IMEI 可进入注册手机页，建档后设备列表展示新设备。
- 影响范围：`docs/v0.3-android-test-checklist.md`、`docs/project-management.md`、`docs/test-artifacts/v0.3/*.png`。
- 验证：`clean assembleDebug lintDebug testDebugUnitTest` 通过；模拟器功能路径和截图视觉检查通过。
- 用户确认：已于 2026-05-12 人工确认 V0.3 通过。
- 是否进入开发：否，本轮为现有实现自测收口。

## 2026-05-12

### V0.3 人工验收通过

- 类型：测试 / 阶段门禁。
- 内容：用户人工确认 V0.3 通过。一键还、催还机、扫码借、未建档注册和设备列表闭环可进入下一阶段。
- 影响范围：`docs/project-management.md`、`docs/v0.3-android-test-checklist.md`。
- 用户确认：已确认。
- 是否进入开发：否。下一步进入 V0.4 范围确认。

### V0.4 范围草案

- 类型：需求 / 架构 / 项目管理。
- 内容：新增 V0.4 范围文档，建议 V0.4 以真实 Android 扫码能力为主，补齐相机权限、扫码失败、手动输入兜底、通知契约；后端本阶段只做 API 和数据库契约准备，不实现真实服务端。
- 影响范围：`docs/v0.4-scope.md`、`docs/project-management.md`、`docs/PRD-手机借机管理.md`、`docs/android-technical-architecture.md`、`docs/backend-database-architecture.md`。
- 用户确认：已确认。
- 是否进入开发：否。先补 Pencil 原型，再由用户确认是否进入 Android 开发。

### V0.4 范围确认通过

- 类型：需求 / 项目管理。
- 内容：确认 V0.4 以真实 Android 扫码能力优先；后端暂不实现真实服务，只做 API / 数据库 / 通知契约准备；暂不新增通知中心页面。
- 影响范围：`docs/v0.4-scope.md`、`docs/project-management.md`、`docs/PRD-手机借机管理.md`。
- 用户确认：已确认。
- 是否进入开发：否。下一步补充 Pencil 原型的相机权限、权限拒绝、扫码失败和手动输入兜底状态。

### V0.4 扫码原型状态补充

- 类型：原型 / 测试计划。
- 内容：在 Pencil 原型中新增扫码借 V0.4 状态：相机权限说明、权限拒绝、扫码失败、手动输入 IMEI 兜底；同步导出预览图并补充 V0.4 Android 测试清单。
- 影响范围：`prototype/手机借机管理.pen`、`prototype/exports/HKnZj.png`、`prototype/exports/u2ZLbB.png`、`prototype/exports/WB25D.png`、`prototype/exports/d7YuFR.png`、`prototype/pencil-prototype.html`、`docs/v0.4-android-test-checklist.md`。
- 用户确认：已确认。
- 是否进入开发：是。用户确认 V0.4 原型后，进入 Android CameraX / ML Kit 开发。

### V0.4 Android 真实扫码开发

- 类型：代码 / 测试 / 架构。
- 内容：接入 CameraX 预览和 ML Kit 条码扫描，新增相机权限说明、权限拒绝、扫描中、扫码失败、手动输入 IMEI 兜底、已建档确认借走、未建档注册分支；新增 IMEI 解析器和单元测试。
- 影响范围：`android-app/app/build.gradle.kts`、`android-app/app/src/main/AndroidManifest.xml`、`android-app/app/src/main/java/com/yjyh/phoneloan/feature/scanborrow/*`、`android-app/app/src/test/java/com/yjyh/phoneloan/feature/scanborrow/ImeiParserTest.kt`、`docs/v0.4-android-test-checklist.md`、`docs/android-technical-architecture.md`、`docs/project-management.md`、`docs/test-artifacts/v0.4/*.png`。
- 验证：`:app:testDebugUnitTest`、`:app:assembleDebug`、`:app:lintDebug` 通过；模拟器验证权限说明、权限拒绝、相机扫描态、手动输入已建档、确认借走成功、手动输入未建档分支通过。
- 用户确认：待人工验收。
- 是否进入开发：是，本轮开发已完成，下一步进入 V0.4 人工验收。

### Agent 编码原则补充

- 类型：流程 / 工程规范。
- 内容：在 `Agent.md` 中补充 Karpathy 编码四原则：先想清楚再编码、最小代码解决当前问题、手术刀式修改、以验收目标驱动执行。
- 影响范围：`Agent.md`。
- 用户确认：用户要求补充。
- 是否进入开发：否，本轮为流程规范补充。

## 2026-05-24

### V0.4 人工验收通过

- 类型：测试 / 阶段门禁。
- 内容：用户确认 V0.4 可进入下一步。真实扫码页的权限说明、权限拒绝、相机扫描态、手动输入兜底、已建档确认借走和未建档注册分支完成阶段验收。
- 影响范围：`docs/project-management.md`、`docs/v0.4-android-test-checklist.md`。
- 用户确认：已确认。
- 是否进入开发：否。下一步进入 V0.5 后端 MVP 范围确认。

### V0.5 后端 MVP 范围草案

- 类型：需求 / 架构 / 项目管理。
- 内容：新增 V0.5 范围文档，建议下一阶段先建设 Spring Boot + PostgreSQL 后端 MVP，覆盖认证、设备、借还、邀请码和通知记录核心 API；Android 暂不在 V0.5 全量切换真实 API。
- 影响范围：`docs/v0.5-scope.md`、`docs/project-management.md`。
- 用户确认：待确认。
- 是否进入开发：否。待用户确认 V0.5 范围后，再创建后端工程脚手架。

### 完整 APK 与埋点日志目标

- 类型：需求 / 架构 / 数据分析。
- 内容：项目最终交付目标调整为完整可安装 Android APK；App 必须具备行为埋点、错误日志和问题诊断能力，支持用户人工使用后持续收集问题并优化。
- 影响范围：`docs/PRD-手机借机管理.md`、`docs/android-technical-architecture.md`、`docs/backend-database-architecture.md`、`docs/telemetry-logging-plan.md`、`docs/project-management.md`。
- 用户确认：用户明确要求。
- 是否进入开发：是。后续后端和 Android 联调必须同步建设埋点与日志闭环。

### V0.5 后端 MVP 初稿

- 类型：代码 / 架构 / 测试 / CI。
- 内容：新增 Spring Boot 后端工程，包含 Flyway 数据库迁移、认证、设备、借还、邀请码、Owner 用户、通知、App 事件上报核心 API；新增后端集成测试和 Backend CI。
- 影响范围：`backend/**`、`.github/workflows/backend-ci.yml`、`docs/cicd-rules.md`、`docs/v0.5-scope.md`、`docs/project-management.md`。
- 验证：本地 `backend ./gradlew test` 通过，覆盖注册/登录、设备建档、IMEI 查询、借机、归还、事件上报。
- 用户确认：待人工审查。
- 是否进入开发：是。下一步进入 V0.6 Android 真实 API 联调与 APK 验收包。

### 暂停 GitHub Actions，改为本地门禁

- 类型：流程 / CI/CD。
- 内容：因当前 GitHub 没有 CI 权限，暂停云端 GitHub Actions；workflow 文件移入 `.github/workflows-disabled/`，后续阶段先以本地构建、测试、lint、模拟器验收作为质量门禁。
- 影响范围：`.github/workflows-disabled/**`、`docs/cicd-rules.md`、`docs/project-management.md`。
- 用户确认：用户明确要求。
- 是否进入开发：是。继续本地 Android 构建和模拟器测试。

### 本地后端联调配置

- 类型：架构 / 测试。
- 内容：新增后端 `local` profile，使用 H2 文件数据库，支持无 PostgreSQL 环境下启动本地后端；Android 事件上报字段对齐后端 `/api/events` 契约；Android 本地网络安全配置允许模拟器访问 `10.0.2.2` 明文 HTTP。
- 影响范围：`backend/build.gradle.kts`、`backend/src/main/resources/application-local.yml`、`android-app/app/src/main/AndroidManifest.xml`、`android-app/app/src/main/res/xml/network_security_config.xml`、`android-app/app/src/main/java/com/yjyh/phoneloan/core/analytics/AnalyticsLogger.kt`。
- 验证：本地后端 `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun` 启动成功；`/api/events` curl 冒烟返回事件 id；Android `assembleDebug`、`testDebugUnitTest`、`lintDebug` 通过；模拟器安装启动成功；Logcat 无崩溃和明文 HTTP 阻断；App 本地事件队列清空。
- 是否进入开发：是。本地验收继续。

### V0.6 Android 真实 API 第一批联调

- 类型：代码 / 后端 / 数据分析 / 测试。
- 内容：Android 新增统一数据入口和远端仓库，优先连接本地后端，后端不可用时回落演示数据；首页、设备、扫码借、注册设备、一键还、催还机、Owner 管理页改为通过统一 repository 读取；后端活跃借还接口补齐“我借出去的”记录；本地后端种子数据补齐 3 台设备和 2 条活跃借还；埋点队列兼容旧版 `payload` 字段并迁移为 `context` 上报。
- 影响范围：`android-app/app/src/main/java/com/yjyh/phoneloan/core/data/**`、各功能页面的数据入口、`backend/src/main/java/com/yjyh/phoneloan/backend/common/DataInitializer.java`、`backend/src/main/java/com/yjyh/phoneloan/backend/loan/**`、`backend/src/test/java/com/yjyh/phoneloan/backend/BackendIntegrationTest.java`。
- 验证：后端 `./gradlew test` 通过；Android `:app:assembleDebug` 通过；本地后端 `/api/devices` 返回 3 台设备，`/api/loans/active` 返回 2 条活跃借还；模拟器安装启动成功，Logcat 未见崩溃、明文 HTTP 阻断或主线程网络错误。
- 是否进入开发：是。下一步补齐登录/注册输入和 API 错误在页面上的用户可见反馈。

### V0.6 登录注册真实表单

- 类型：代码 / 用户体验 / 数据分析 / 测试。
- 内容：登录页和注册页由静态字段改为可输入字段；登录调用后端 `/api/auth/login`，注册调用 `/api/auth/register`；表单校验、密码遮罩、错误提示、登录/注册成功失败埋点已补齐；默认登录账号保留为 `10086 / password123` 方便本地验收。
- 影响范围：`android-app/app/build.gradle.kts`、`android-app/app/src/main/java/com/yjyh/phoneloan/feature/auth/AuthScreens.kt`、`android-app/app/src/main/java/com/yjyh/phoneloan/core/data/**`、`android-app/app/src/main/java/com/yjyh/phoneloan/core/design/Components.kt`。
- 验证：Android `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 通过；后端 `./gradlew test` 通过；本地后端启动后，模拟器点击登录成功进入首页并显示“已连接本地后端，数据来自真实 API”；Logcat 无崩溃、网络阻断或登录失败；埋点队列为 0。
- 是否进入开发：是。下一步继续补完整人工验收包和真实流程边界错误。

### V0.6 Android 人工验收包

- 类型：交付 / 测试 / 文档。
- 内容：新增 V0.6 Android 人工验收包，明确 APK 路径、本地后端启动方式、测试账号、验收路径、日志和埋点检查方式；新增本地启动后端和构建安装 APK 脚本。
- 影响范围：`docs/releases/v0.6/android-acceptance-package.md`、`scripts/run-local-backend.sh`、`scripts/build-install-debug-apk.sh`、`backend/README.md`、`android-app/README.md`、`docs/android-test-plan.md`。
- 验证：`./scripts/run-local-backend.sh` 启动成功；`./scripts/build-install-debug-apk.sh` 构建、安装、启动成功；Android `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 通过；后端 `./gradlew test` 通过；模拟器登录进入真实 API 首页；Logcat 无崩溃、网络阻断和主线程网络错误；埋点队列为 `0`。
- 是否进入开发：是。下一步继续补真实流程边界错误提示。

### V0.6 真实流程边界错误提示

- 类型：代码 / 用户体验 / 数据分析 / 测试。
- 内容：新增真实操作结果接口，注册手机、确认借走、一键还、催还机不再只乐观展示成功；后端失败会在页面显示错误提示，同时记录可回溯埋点。注册手机显示建档失败提示；扫码借确认借走显示借走失败提示；一键还和催还机显示对应失败提示并避免重复点击。
- 影响范围：`android-app/app/src/main/java/com/yjyh/phoneloan/core/data/**`、`android-app/app/src/main/java/com/yjyh/phoneloan/feature/registerdevice/RegisterDeviceScreen.kt`、`android-app/app/src/main/java/com/yjyh/phoneloan/feature/scanborrow/ScanBorrowScreen.kt`、`android-app/app/src/main/java/com/yjyh/phoneloan/feature/returnloan/ReturnLoanScreen.kt`。
- 验证：Android `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug` 通过；后端 `./gradlew test` 通过；本地后端启动后模拟器登录、进入一键还、点击催还机成功反馈可见；Logcat 无崩溃、网络阻断、主线程网络错误或可见错误埋点；埋点队列为 `0`。
- 是否进入开发：是。下一步继续补扫码借/注册手机的人工边界路径和最终 APK 验收包。
