# 技术设计: version-v101

## 目标
以最小改动完成一次补丁版本发布：同步更新 Android 安装版本与 Git 发布 tag，保持现有构建链路和发布工作流不变。

## 方案概述
### 方案A（采用）: app 版本号自增到 1.0.1，并在对应提交创建 `v1.0.1` tag
- 修改 `easycontrol/app/build.gradle`：
  - `versionCode 10000` → `10001`
  - `versionName "1.0.0"` → `"1.0.1"`
- 执行 `./gradlew :app:assembleDebug` 验证构建。
- 使用 `aapt dump badging` 校验 APK 内版本字段。
- 在提交后创建并推送 annotated tag `v1.0.1`。

**优点:**
- 变更范围最小，不影响业务代码。
- 安装版本与 Git 发布版本保持一致，便于发布和回溯。
- 兼容现有 GitHub tag release workflow。

**缺点:**
- 仅更新 app 安装版本，不涉及 server 自身版本号。

### 方案B（未采用）: 同时提升 app 与 server 双模块版本号
**拒绝原因:** 用户当前诉求聚焦于安装版本与 tag；server 不作为独立安装包对外分发，无需同步提升其内部版本号。

## 风险与规避
- **风险:** 仅修改代码版本号但未验证 APK，导致安装显示版本仍异常。
  - **规避:** 构建后用 `aapt dump badging` 读取 APK 实际元数据。
- **风险:** 本地已存在同名 tag 或远端已有同名 tag。
  - **规避:** 创建前先检查本地与远端 `v1.0.1` 是否存在。

## 验证计划
1. 执行 `./gradlew :app:assembleDebug`。
2. 执行 `aapt dump badging easycontrol/app/build/outputs/apk/debug/app-debug.apk` 验证版本号。
3. 执行 `git push origin main` 与 `git push origin v1.0.1`，确认远端标签存在。
