# 手机借机管理 App — 后端与数据库架构

> 目的：提前确认服务器框架、数据库模型和接口边界，让 Android 先用 mock 数据开发时也能贴近真实后端，避免后期大改。

## 1. 结论

当前建议：

- 后端框架：Kotlin / Java + Spring Boot。
- 数据库：PostgreSQL。
- 数据访问：Spring Data JPA 或 MyBatis 二选一，初期优先 Spring Data JPA。
- 数据迁移：Flyway。
- 接口形式：REST API。
- 认证：工号 + 密码登录，服务端签发 access token / refresh token。
- 密码：BCrypt 或 Argon2 哈希存储。
- 部署：先单体服务，后续按需要再拆分。
- 通知：V0.1 / V0.2 先做站内通知记录表；真实推送后置。
- 可观测性：后端提供事件/日志接收接口，支持 Android 上报用户行为、错误和诊断信息。

## 2. 为什么现在要想

现在需要确定：

- 核心实体有哪些。
- 借机、还机、owner、邀请码这些规则如何落库。
- Android mock repository 的字段如何设计。
- 后续 API 大概是什么形状。

现在不急着实现：

- 真实服务器部署。
- 完整后台管理端。
- 消息推送服务。
- 审计导出。
- 多租户、复杂权限、审批流。

## 3. 版本节奏

| 版本 | 后端策略 |
|------|----------|
| V0.2 Android 壳 | 不实现后端，Android 使用 mock repository，但字段按本文设计 |
| V0.3 核心流程 | 仍可 mock，补齐借机 / 还机 / 手机注册状态流 |
| V0.4 API 契约准备 | 细化 REST API、错误码、通知类型和数据库字段，不实现真实服务端 |
| V0.5 后端 MVP | 建 Spring Boot + PostgreSQL，实现登录、设备、借还、邀请码核心 API |
| V0.6 联调验收 | Android 从 mock 切换真实 API，做端到端测试 |

## 4. 核心数据表

### 4.1 users

用户表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| employee_no | 工号，全局唯一 |
| name | 展示名 |
| password_hash | 密码哈希 |
| role | `USER` / `OWNER` |
| invited_by_user_id | 邀请人 |
| invite_quota_used | 普通用户累计申请邀请码数量 |
| created_at | 注册时间 |
| updated_at | 更新时间 |

约束：

- `employee_no` 唯一。
- 普通用户邀请码累计上限为 10。

### 4.2 invite_codes

邀请码表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| code | 邀请码，唯一 |
| created_by_user_id | 生成或申请人 |
| used_by_user_id | 使用人 |
| status | `UNUSED` / `USED` / `EXPIRED` / `REVOKED` |
| expires_at | 过期时间 |
| used_at | 使用时间 |
| created_at | 创建时间 |

规则：

- Owner 可无限生成。
- 普通用户累计最多申请 10 张，所有状态都计入配额。
- 注册时必须使用有效邀请码。

### 4.3 devices

设备表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| name | 手机名称 |
| imei1 | 完整 IMEI，唯一 |
| imei2 | 双卡 IMEI2，可为空 |
| owner_user_id | 设备绑定 owner |
| current_holder_user_id | 当前持有人 |
| status | `AVAILABLE` / `HELD_BY_ME` / `BORROWED_OUT` / `PENDING_RETURN` |
| created_at | 建档时间 |
| updated_at | 更新时间 |

规则：

- `imei1` 必须唯一。
- 任意时刻每台设备只有一个绑定 owner。
- 当前持有人由借机 / 还机流程自动维护。

### 4.4 loan_records

借用记录表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| device_id | 设备 |
| borrower_user_id | 本次借机人 |
| previous_holder_user_id | 上一位持有人 |
| owner_user_id | 借机时设备 owner 快照 |
| started_at | 借走时间 |
| ended_at | 归还时间 |
| status | `ACTIVE` / `RETURNED` |

规则：

- 同一设备同一时间只能有一条 `ACTIVE` 记录。
- 扫码借成功后，结束上一段持有关系，并创建新的 `ACTIVE` 记录。
- 一键还后，将当前 `ACTIVE` 记录置为 `RETURNED`。

### 4.5 owner_transfer_records

设备 owner 转让记录表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| device_id | 设备 |
| from_owner_user_id | 原 owner |
| to_owner_user_id | 新 owner |
| transferred_by_user_id | 操作人 |
| created_at | 转让时间 |

规则：

- 仅当前设备 owner 可发起转让。
- 转让后仅新 owner 可修改设备档案。

### 4.6 notifications

通知记录表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| recipient_user_id | 接收人 |
| type | 通知类型 |
| title | 标题 |
| content | 内容 |
| related_device_id | 关联设备 |
| related_loan_record_id | 关联借用记录 |
| read_at | 阅读时间 |
| created_at | 创建时间 |

规则：

- 扫码借成功后，通知上一位持有人和设备绑定 owner。
- V0.1 / V0.2 原型先用成功提示和记录说明，后端 MVP 可先记录站内通知，不做真实推送。

### 4.7 app_events

App 行为与错误事件表。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| user_id | 用户，可为空 |
| session_id | App 会话 ID |
| event_name | 事件名 |
| screen | 页面 |
| action | 用户动作或系统动作 |
| result | `SUCCESS` / `FAILURE` / `CANCELLED` |
| severity | `INFO` / `WARN` / `ERROR` |
| context_json | 脱敏上下文 |
| app_version | App 版本 |
| device_model | 设备型号 |
| os_version | 系统版本 |
| created_at | 创建时间 |

规则：

- 密码、token 不得上报。
- IMEI 必须脱敏。
- bug 相关事件必须能关联页面、动作和错误码。

## 5. 关键业务事务

### 5.1 注册

事务步骤：

1. 校验邀请码存在且状态为 `UNUSED`。
2. 校验工号未注册。
3. 创建用户，记录邀请人。
4. 将邀请码改为 `USED`，写入使用人和使用时间。

### 5.2 扫码借已建档手机

事务步骤：

1. 根据 IMEI 查询设备。
2. 查询当前持有人和当前 active 借用记录。
3. 结束上一条 active 借用记录。
4. 创建新的 active 借用记录，borrower 为当前扫码用户。
5. 更新设备当前持有人。
6. 给上一位持有人和设备 owner 创建通知记录。

### 5.3 扫码借未建档手机

事务步骤：

1. 校验 IMEI 未建档。
2. 创建设备档案，owner 默认为当前扫码用户。
3. 创建新的 active 借用记录。
4. 更新设备当前持有人为当前用户。

### 5.4 一键还

事务步骤：

1. 查询当前用户相关的 active 借用记录。
2. 用户点击某条记录一键还。
3. 将记录置为 `RETURNED`，写入 `ended_at`。
4. 更新设备状态和当前持有人。
5. 必要时创建通知记录。

## 6. API 草案

认证：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

当前用户：

- `GET /api/me`
- `GET /api/me/summary`

设备：

- `GET /api/devices`
- `GET /api/devices/{id}`
- `GET /api/devices/by-imei/{imei}`
- `POST /api/devices`
- `PATCH /api/devices/{id}`
- `POST /api/devices/{id}/transfer-owner`

借还：

- `POST /api/loans/borrow-by-imei`
- `GET /api/loans/active`
- `POST /api/loans/{id}/return`

邀请码：

- `GET /api/invite-codes/my`
- `POST /api/invite-codes/apply`
- `GET /api/owner/invite-codes`
- `POST /api/owner/invite-codes`

Owner：

- `GET /api/owner/users`

通知：

- `GET /api/notifications`
- `POST /api/notifications/{id}/read`

事件：

- `POST /api/events`

## 7. Android 对接策略

- Android V0.2 / V0.3 的 mock repository 字段按本文数据模型设计。
- Android ViewModel 不直接依赖 mock 数据，统一依赖 repository 接口。
- 后端 MVP 完成后，只新增真实 API repository，不重写页面。
- API 返回结构要服务原型页面，避免页面为了接口结构重排。

## 8. 安全与合规

- 密码只存哈希，不存明文。
- IMEI 不写入普通调试日志。
- 完整 IMEI 展示遵循 PRD 的权限要求。
- 所有写操作需要登录态。
- Owner 接口必须校验角色。
- 借还、设备变更、owner 转让需要保留操作记录。

## 9. 暂缓事项

- 独立后台 Web 管理端。
- 真实推送服务。
- 审计导出。
- 多组织 / 多租户。
- 复杂审批流。
- 高可用集群部署。

## 10. 开发准入

开始后端开发前，需要确认：

- Android V0.2 壳和主要页面已跑通。
- 本文数据模型和 API 草案已被确认。
- 是否存在真实部署环境要求。
- 是否需要先接公司统一登录或内部账号体系。
