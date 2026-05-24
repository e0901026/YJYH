# 埋点与日志方案

> 目标：应用交付给用户人工使用后，能收集关键行为、错误和诊断信息，支持数据分析和持续优化。

## 1. 原则

- 先记录关键闭环，不追求一开始就大而全。
- 事件必须结构化，不能只写自然语言日志。
- 隐私优先：密码、token 不入日志；IMEI 默认脱敏。
- 任何用户遇到 bug 的路径，都应能回答：谁、在哪个页面、做了什么、结果是什么、错误码是什么。

## 2. 首批事件

| 事件名 | 页面 | 触发 |
|--------|------|------|
| `auth_login_submit` | 登录 | 点击登录 |
| `scan_permission_request` | 扫码借 | 请求相机权限 |
| `scan_permission_denied` | 扫码借 | 用户拒绝权限 |
| `scan_started` | 扫码借 | 进入扫码态 |
| `scan_parse_failed` | 扫码借 | 条码无法解析 IMEI |
| `manual_imei_submit` | 手动输入 | 提交 IMEI |
| `borrow_confirm_submit` | 扫码借 | 确认借走 |
| `borrow_confirm_result` | 扫码借 | 借走成功或失败 |
| `device_register_submit` | 手机注册 | 提交建档 |
| `return_submit` | 一键还 | 点击一键还 |
| `urge_return_submit` | 一键还 | 点击催还机 |
| `api_error` | 全局 | API 返回错误 |
| `app_exception` | 全局 | App 捕获异常 |

## 3. Android 侧

- 新增 `AnalyticsLogger`，统一记录行为事件和错误事件。
- Debug 阶段先写 Logcat，并在本地保留最近事件队列。
- V0.6 接入后端后，批量上报到 `/api/events`。
- 截图和人工验收时保留对应日志，方便回放路径。

## 4. 后端侧

- 新增 `app_events` 表。
- 新增 `POST /api/events`。
- 事件上下文字段保存脱敏 JSON。
- 后续可增加按用户、页面、错误码统计。

## 5. 人工验收要求

- 用户遇到 bug 后，开发者能根据日志定位到页面、动作、错误码和近邻事件。
- 验收 APK 必须开启 debug 级别本地日志。
- 正式版本默认只上报必要事件和错误，不上报敏感内容。
