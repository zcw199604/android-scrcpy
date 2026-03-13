# 技术设计: version-v102

## 目标
以最小改动完成一次补丁版本发布：同步更新 Android 安装版本与 Git 发布 tag，保持当前构建链路和 GitHub tag release workflow 不变。

## 方案概述
### 方案A（采用）: app 版本号自增到 1.0.2，并在对应提交创建 `v1.0.2` tag
- 修改 `easycontrol/app/build.gradle`：
  - `versionCode 10001` → `10002`
  - `versionName "1.0.1"` → `"1.0.2"`
- 执行 `./gradlew :app:assembleDebug` 验证构建。
- 使用 `aapt dump badging` 校验 APK 内版本字段。
- 推送 `main` 后在最新提交创建并推送 annotated tag `v1.0.2`。
- 顺手把上一次 `version-v101` 归档里未勾选的“提交、推送并创建 tag”任务状态修正为已完成，保证知识库一致。

**优点:**
- 变更范围最小，不影响业务逻辑。
- 安装版本、提交历史与发布标签一致，便于发布和回溯。
- 与现有 GitHub tag release workflow 完全兼容。

**缺点:**
- 仅更新 app 安装版本，不涉及 server 自身版本号。

### 方案B（未采用）: 同时提升 app 与 server 双模块版本号
**拒绝原因:** 用户本次只要求安装版本与发布 tag，server 并非独立对外安装包，无需同步变更其内部版本号。

## 风险与规避
- **风险:** 仅修改版本号但未验证 APK 元数据，导致安装显示版本不一致。
  - **规避:** 构建后用 `aapt dump badging` 读取 APK 实际版本字段。
- **风险:** 本地或远端已存在同名 tag。
  - **规避:** 创建前先检查本地与远端 `v1.0.2` 是否存在。

## 验证计划
1. 执行 `./gradlew :app:assembleDebug`。
2. 执行 `aapt dump badging easycontrol/app/build/outputs/apk/debug/app-debug.apk` 验证版本号。
3. 执行 `git push origin main` 与 `git push origin v1.0.2`，确认远端标签存在。
