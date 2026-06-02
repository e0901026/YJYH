# Codex Handoff Context

本文件用于在另一台电脑上通过 GitHub 继续本项目。目标是：新电脑 `git clone` 后，在 Codex 中添加项目文件夹，就能读取同一套项目上下文、规则、当前状态和启动方式继续开发。

## 1. 新电脑接手步骤

### 1.1 安装基础工具

- 安装 Git。
- 安装 Android Studio，并至少打开一次完成 Android SDK 初始化。
- 安装 Codex 桌面端。
- 可选：安装 Pencil。如果需要继续修改 `.pen` 原型文件，必须安装 Pencil。

### 1.2 拉取 GitHub 项目

```sh
cd ~/Documents
git clone https://github.com/e0901026/YJYH.git
cd YJYH
```

如果仓库未来改回 private，需要先在新电脑完成 GitHub 登录/授权。

### 1.3 在 Codex 中添加项目文件夹

1. 打开 Codex。
2. 选择添加/打开本地文件夹。
3. 选择刚 clone 的目录，例如 `~/Documents/YJYH`。
4. 新对话开始时，让 Codex 先读：
   - `Agent.md`
   - `docs/codex-handoff.md`
   - `docs/project-management.md`
   - `docs/CHANGELOG.md`

推荐给新电脑 Codex 的第一句话：

```txt
请先阅读 Agent.md、docs/codex-handoff.md、docs/project-management.md 和 docs/CHANGELOG.md，确认当前项目状态，然后继续开发。
```

## 2. Codex 必读上下文

每次跨机器或新会话继续项目时，先读这些文件：

- `Agent.md`：最高工作原则，包含 PRD → 原型 → 开发 → 测试闭环。
- `docs/codex-handoff.md`：跨电脑接手、环境、当前状态和下一步。
- `docs/project-management.md`：阶段看板和当前版本目标。
- `docs/CHANGELOG.md`：所有需求、原型、架构、代码和测试变更。
- `docs/PRD-手机借机管理.md`：业务需求源头。
- `docs/system-design-system.md`：设计规范。
- `prototype/手机借机管理.pen`：Pencil 源文件。
- `prototype/pencil-prototype.html`：原型预览入口。

## 3. 当前项目状态

截至 2026-06-02：

- GitHub 仓库：`https://github.com/e0901026/YJYH`
- 当前分支：`main`
- 最新已推送提交：`c18eea6 owner: add real user crud flow`
- 当前阶段：V0.8 Owner 用户管理真实 CRUD 与邀请码生成已完成本地验证。
- Android 优先开发。
- CI/CD：GitHub Actions 因权限问题暂停，当前以本地门禁为准。

已完成重点：

- Android Kotlin + Jetpack Compose 工程。
- Spring Boot 后端 local profile，可用 H2 本地数据库。
- 登录 / 注册真实表单。
- 真实扫码能力与手动 IMEI 兜底。
- 设备建档、借走、归还、催还机。
- 首页、设备列表、设备详情、Owner 用户管理按 V0.7 原型调整。
- Owner 用户新增、编辑、停用和邀请码生成真实 API。
- 行为埋点和错误日志上报基础能力。

## 4. 本地环境命令

### 4.1 通用环境变量

macOS + Android Studio 默认路径：

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
```

如果新电脑 Android Studio 安装路径不同，先找到 JBR 路径后再设置 `JAVA_HOME`。

### 4.2 启动本地后端

从项目根目录执行：

```sh
./scripts/run-local-backend.sh
```

等后端启动后，检查：

```sh
curl http://127.0.0.1:8080/api/devices
```

local profile 默认账号：

- 工号：`10086`
- 密码：`password123`
- 初始邀请码：`OWNER-SEED-0001`

### 4.3 构建 Android Debug APK

```sh
cd android-app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug
```

APK 路径：

```txt
android-app/app/build/outputs/apk/debug/app-debug.apk
```

### 4.4 模拟器安装并启动

先启动 Android 模拟器，再从项目根目录执行：

```sh
./scripts/build-install-debug-apk.sh
```

模拟器 APK 默认访问：

```txt
http://10.0.2.2:8080
```

### 4.5 真机验收 APK

真机不能访问 `10.0.2.2`，必须使用 Mac 的局域网 IP。

从项目根目录执行：

```sh
LAN_IP=你的Mac局域网IP ./scripts/build-lan-debug-apk.sh
```

例如：

```sh
LAN_IP=192.168.0.110 ./scripts/build-lan-debug-apk.sh
```

真机和 Mac 必须在同一 Wi-Fi，Mac 防火墙需要允许 Java / Spring Boot 被局域网访问。

## 5. 本地质量门禁

每轮代码交付前至少执行：

```sh
cd android-app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

```sh
cd backend
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew test
```

影响界面的改动还必须：

- 安装到模拟器或真机。
- 截图保存到 `docs/test-artifacts/<版本>/`。
- 对照 `prototype/exports/*.png` 或对应原型说明。

## 6. Pencil 原型注意事项

- 原型源文件：`prototype/手机借机管理.pen`
- 预览页：`prototype/pencil-prototype.html`
- 导出图：`prototype/exports/*.png`

若新电脑需要修改原型：

1. 安装 Pencil。
2. 用 Pencil 打开 `.pen` 文件。
3. 修改后显式保存。
4. 导出相关页面 PNG。
5. 用 `git status` 确认 `.pen` 和导出图都进入变更。

只更新 PNG 不算完成原型修改。

## 7. 下一步建议

继续做完项目时，优先级建议：

1. 整理 V0.8 最新 APK 验收包，替代旧 V0.6 验收包。
2. 完善 Owner 用户管理真实表单的交互体验：输入校验、停用二次确认、编辑后的列表定位。
3. 补设备 owner 转让真实接口和 Android 入口。
4. 补设备名称编辑真实接口。
5. 补通知列表或通知记录查看能力，让借走/催还/归还通知可追溯。
6. 继续打包可安装 APK 给用户人工使用。

## 8. Git 纪律

每次完整变更必须包含：

- PRD 或需求说明变更，如果业务规则变了。
- Pencil 原型变更，如果界面/交互变了。
- 代码变更。
- 测试记录。
- `docs/CHANGELOG.md`。
- `docs/project-management.md` 阶段状态。

完成后：

```sh
git status
git add ...
git commit -m "..."
git push origin main
```

跨电脑继续开发前，先执行：

```sh
git pull origin main
```
