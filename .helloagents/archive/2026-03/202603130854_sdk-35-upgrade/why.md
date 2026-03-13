# 变更提案: sdk-35-upgrade

## 需求背景
用户希望先确认当前 Android Gradle Plugin（AGP）与 Gradle Wrapper 是否兼容 API 35，再在兼容前提下把 Android 项目的 SDK 升级到 35。

经核查，当前仓库使用：
- AGP `8.2.2`
- Gradle Wrapper `8.2`
- `app` / `server` 模块 `compileSdk 34`、`targetSdk 34`

根据 Android 官方文档：
- API 35（Android 15）要求项目至少使用 AGP `8.6.0`；
- Android 15 SDK 设置页面进一步建议使用 AGP `8.6.1` 或更高版本；
- AGP `8.6` 对应的最低 Gradle 版本为 `8.7`。

因此，当前构建链路本身并不满足 API 35 的最低要求，需先升级 AGP / Gradle，再升级 SDK。

## 变更内容
1. 将 `easycontrol` 多模块工程的 AGP 从 `8.2.2` 升级到 `8.6.1`。
2. 将 Gradle Wrapper 从 `8.2` 升级到 `8.7`。
3. 将 `app` 与 `server` 模块的 `compileSdk` / `targetSdk` 从 `34` 升级到 `35`。
4. 同步更新知识库与变更日志，记录官方兼容结论和本地验证结果。

## 影响范围
- **模块:** `easycontrol_app`, `easycontrol_server`, `repository_docs`
- **文件:**
  - `easycontrol/app/build.gradle`
  - `easycontrol/server/build.gradle`
  - `easycontrol/gradle/wrapper/gradle-wrapper.properties`
  - `.helloagents/context.md`
  - `.helloagents/modules/easycontrol_app.md`
  - `.helloagents/modules/easycontrol_server.md`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`

## 验收标准
1. 当前构建脚本中的 AGP / Gradle 版本满足 Android 官方对 API 35 的最低要求。
2. `app` 与 `server` 模块已升级到 `compileSdk 35`、`targetSdk 35`。
3. 本地执行 `./gradlew :app:assembleDebug` 通过。
4. 知识库与变更日志已同步到最新事实。
