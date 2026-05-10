# 手机借机管理 App — CI/CD 规则

> 目的：定义 Android、后端、文档和原型在开发过程中的自动检查与交付规则，让每次进入下一阶段都有明确质量门禁。

## 1. 结论

需要 CI/CD，但分阶段建设。

当前项目不应一开始做复杂发布流水线，先做最小可用质量门禁：

- V0.2：Android 构建、单元测试、静态检查、文档存在性检查。
- V0.3：增加 Compose UI 测试、核心业务单元测试、测试包产物。
- V0.4：增加后端构建、后端单元测试、数据库迁移校验。
- V0.5：增加 Android + 后端联调环境和端到端冒烟测试。

## 2. 基本原则

- CI 是质量门禁，不替代人工原型验收。
- CI 通过不代表产品通过；产品仍需按 PRD、原型和测试清单确认。
- 原型变更必须先确认 `.pen` 源文件和导出预览，再进入开发。
- Android V0.2 / V0.3 可以不依赖真实后端，使用 mock repository。
- 后端实现前，CI 只检查后端文档和接口边界；后端代码出现后再启用后端构建。

## 3. 分支与合并规则

建议分支：

| 分支 | 用途 |
|------|------|
| `main` | 稳定基线，只放已验收版本 |
| `develop` | 日常集成分支 |
| `feature/*` | 单个功能或页面开发 |
| `fix/*` | 缺陷修复 |

合并到 `develop` 前必须：

- 对应 PRD / 原型 / 架构文档已更新。
- 本地构建通过。
- 本地测试通过。
- 无明显原型偏差。

合并到 `main` 前必须：

- CI 全部通过。
- 本轮测试清单完成。
- 阻塞问题为 0。
- 用户确认该版本可以作为阶段基线。

## 4. Android CI

V0.2 起启用。

检查项：

- Gradle 构建：`./gradlew assembleDebug`
- 单元测试：`./gradlew testDebugUnitTest`
- 静态检查：`./gradlew lintDebug`
- Kotlin 编译告警检查。
- 关键文件存在检查：主题、导航、主要页面入口、mock repository。

V0.3 增加：

- Compose UI 测试。
- 核心业务单元测试：借机、还机、邀请码配额、设备注册状态变化。
- debug APK 产物归档。

通过标准：

- 构建成功。
- 单元测试通过。
- lint 无阻塞级问题。
- debug APK 可安装启动。

## 5. 后端 CI

V0.4 起启用。

检查项：

- 构建：`./gradlew build`
- 单元测试。
- API 层测试。
- Flyway migration 校验。
- 数据库 schema 基础约束检查。

通过标准：

- 服务可启动。
- 核心业务事务测试通过。
- 数据库迁移可从空库执行成功。
- 登录、设备查询、扫码借、一键还、邀请码接口冒烟通过。

## 6. 文档与原型检查

每次进入开发或测试前检查：

- `docs/PRD-手机借机管理.md` 存在且对应需求已更新。
- `docs/project-management.md` 已记录当前版本状态。
- `docs/android-technical-architecture.md` 已覆盖当前 Android 方案。
- `docs/backend-database-architecture.md` 已覆盖数据模型和 API 边界。
- `docs/android-test-plan.md` 已覆盖当前测试方式。
- `prototype/手机借机管理.pen` 存在。
- `prototype/pencil-prototype.html` 可作为预览入口。

原型相关变更必须手动确认：

- Pencil 源文件已保存。
- 导出图已更新。
- 页面无明显布局重叠、裁剪或风格漂移。

## 7. 测试包交付规则

每个 Android 测试包必须附带：

- 版本号或构建说明。
- APK 路径或运行方式。
- 本次改动范围。
- 本次测试清单。
- mock 数据说明。
- 已知问题。
- 是否需要真机测试。

V0.2 测试包目标：

- 能安装启动。
- 底部导航可用。
- 主要页面可打开。
- UI 风格接近 Pencil 原型。

V0.3 测试包目标：

- 核心 mock 流程可走通。
- 扫码借已建档 / 未建档两条分支可模拟。
- 一键还可模拟状态变化。

## 8. 发布规则

当前阶段不做正式发布，只做内部测试包。

正式发布前需要补充：

- 签名配置。
- 版本号规则。
- 内部分发渠道。
- 崩溃日志收集。
- 隐私与权限说明。
- 真实后端环境配置。

## 9. 最小 GitHub Actions 草案

当前已建立 GitHub Actions：

```yaml
name: Android CI

on:
  pull_request:
  push:
    branches: [develop, main]

jobs:
  android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Build debug
        run: gradle assembleDebug
        working-directory: android-app
      - name: Unit tests
        run: gradle testDebugUnitTest
        working-directory: android-app
      - name: Lint
        run: gradle lintDebug
        working-directory: android-app
```

后端代码出现后再增加后端 job。

## 10. 当前阶段执行口径

现在需要做：

- 把 CI/CD 规则纳入项目管理。
- Android 工程创建后立即配置云端 CI 构建、测试和 lint。
- 本地 Android 工具链仍需补齐，用于生成本地测试包和真机联调。

现在不急着做：

- 生产发布流水线。
- 自动部署后端。
- 多环境发布审批。
- 应用商店发布。
