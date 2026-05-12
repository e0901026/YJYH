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
- 用户确认：待确认。
- 是否进入开发：否。待用户确认 V0.4 范围后，先补 Pencil 原型，再进入 Android 开发。
