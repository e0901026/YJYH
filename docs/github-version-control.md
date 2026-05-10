# 手机借机管理 App — GitHub 版本管理规则

> 目的：确保需求、原型、架构、代码、测试和交付记录都能被 GitHub 追踪，任何阶段出问题都可以回溯、比较和回滚。

## 1. 核心原则

- GitHub 是项目版本真相，不能只用本地文件夹管理项目。
- 每次提交必须覆盖本次变更相关的全部文件。
- 需求变更、设计规范变更、原型变更、架构变更、代码变更、测试变更必须一起纳入版本管理。
- 不允许只改代码不提交 PRD / 原型 / 测试清单。
- 不允许只改原型不保存 `.pen` 源文件。
- 每个阶段基线必须能从 GitHub checkout 后恢复。

## 2. 当前仓库范围

必须纳入 Git：

- `Agent.md`
- `docs/*.md`
- `prototype/手机借机管理.pen`
- `prototype/pencil-prototype.html`
- `prototype/exports/*.png`
- 后续 Android 工程：`android-app/`
- 后续后端工程：`server/` 或 `backend/`
- CI 配置：`.github/workflows/*.yml`

不纳入 Git：

- `.DS_Store`
- IDE 临时文件
- Gradle / build 产物
- Android APK / AAB 大包产物，除非作为明确测试交付物另行约定
- 密钥、签名文件、token、数据库密码

## 3. 分支策略

建议使用：

| 分支 | 用途 |
|------|------|
| `main` | 稳定基线，只放用户确认过的阶段版本 |
| `develop` | 日常集成分支 |
| `feature/*` | 新功能、新页面、新架构文档 |
| `fix/*` | 缺陷修复 |
| `docs/*` | 文档和规则调整 |

当前早期阶段可以先在 `main` 上建立基线；进入代码开发后切出 `develop` 和功能分支。

## 4. 提交粒度

一个提交应表达一个完整意图。

正确示例：

- `docs: establish Android architecture and backend plan`
- `prototype: update device list tab style`
- `android: add V0.2 app shell and navigation`
- `test: add V0.2 Android acceptance checklist`

错误示例：

- `update`
- `fix`
- `改一下`
- 只提交代码但漏掉 PRD / 原型 / 测试清单。

## 5. 每次提交前检查清单

提交前必须检查：

- 是否有 PRD 变更。
- 是否有设计规范变更。
- 是否有 Pencil 原型变更。
- 是否已保存 `.pen` 源文件。
- 是否已更新原型导出图或预览 HTML。
- 是否有架构文档变更。
- 是否有 Android / 后端代码变更。
- 是否有测试计划或验收清单变更。
- 是否有 CI/CD 规则变更。
- 是否包含不应提交的临时文件或敏感信息。

## 6. 提交说明模板

每次提交信息建议包含：

```txt
<type>: <summary>

Changes:
- ...
- ...

Validation:
- ...

Docs/Prototype:
- ...
```

类型建议：

- `docs`
- `prototype`
- `android`
- `backend`
- `test`
- `ci`
- `chore`

## 7. PR 规则

进入多人协作或 GitHub 远端后，每个 PR 必须说明：

- 本次解决什么问题。
- 对应 PRD 章节。
- 对应原型页面。
- 涉及架构或设计规范变更。
- 测试结果。
- 是否影响 Android 真机测试。
- 是否有后端 / 数据库迁移影响。

PR 合并前必须：

- CI 通过。
- 文档和代码一致。
- 原型和开发一致。
- 用户确认关键产品变化。

## 8. 版本标签

阶段完成后打 tag：

| Tag | 含义 |
|-----|------|
| `v0.1-prototype` | 原型确认基线 |
| `v0.2-android-shell` | Android 壳和页面导航 |
| `v0.3-core-flow-mock` | mock 核心业务流程 |
| `v0.4-backend-mvp` | 后端 MVP |
| `v0.5-integration` | Android + 后端联调 |

## 9. 回滚策略

如果出现问题：

- 需求错：回到最后一个正确 PRD 版本，对比后修正。
- 原型错：回到对应 `.pen` 和导出图版本。
- 代码错：回滚或 revert 对应提交。
- 架构错：新提交修正架构文档，并同步调整代码计划。

原则：

- 优先使用 `git revert` 生成反向提交，保留历史。
- 不使用破坏历史的 `reset --hard` 处理已共享分支。
- 回滚后必须补充说明为什么回滚。

## 10. 当前执行口径

当前 GitHub 仓库：

- `https://github.com/e0901026/YJYH`

仓库可见性：

- Public。仓库曾为 Private；为排查并启用 GitHub Actions，已按用户要求调整为 Public。

当前执行规则：

1. 本地 Git 仓库和 GitHub 私有仓库已建立。
2. 当前 PRD、原型、架构、测试和 CI/CD 文档基线已推送到 `main`。
3. 从下一阶段起按分支和 PR 规则开发。
4. 任何开发任务开始前先确认当前分支和工作区状态。
5. 任何开发任务完成后先提交完整变更，再进入下一轮。

Codex 后续每次完成一轮实际修改时，需要主动提示：

- 改了哪些文件。
- 是否建议提交。
- 建议提交信息。
- 是否需要打版本 tag。
