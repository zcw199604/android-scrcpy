# 技术设计: sdk-35-upgrade

## 目标
在不改动业务代码的前提下，把 Android 构建链路升级到 Android 15 / API 35 可支持的最小稳定组合，并验证现有多模块打包链路继续可用。

## 方案概述
### 方案A（采用）: 升级到 AGP 8.6.1 + Gradle 8.7，并同步 app/server 到 SDK 35
- `easycontrol/app/build.gradle` / `easycontrol/server/build.gradle`
  - `com.android.application` → `8.6.1`
  - `compileSdk` → `35`
  - `targetSdk` → `35`
- `easycontrol/gradle/wrapper/gradle-wrapper.properties`
  - `gradle-8.2-bin.zip` → `gradle-8.7-bin.zip`
- 本地补齐 `platforms;android-35` 与 `build-tools;35.0.0` 后执行构建验证。

**优点:**
- 满足 Android 官方对 API 35 的最低要求。
- 变更范围集中在构建配置，不影响业务逻辑。
- 与当前 JDK 17 环境兼容，升级成本低。

**缺点:**
- 首次构建需要下载新 Gradle 发行版与 Android 35 SDK 组件。
- AGP 升级后可能暴露更严格的构建校验，需要进行一次完整调试构建验证。

### 方案B（未采用）: 仅把 `compileSdk` / `targetSdk` 改到 35，保留 AGP 8.2.2 + Gradle 8.2
**拒绝原因:** Android 官方文档明确 API 35 需要 AGP 8.6+；保留旧版构建链路会导致配置不满足最低兼容要求。

## 风险与规避
- **风险:** AGP 8.6.1 对现有 Gradle Wrapper 版本不兼容。
  - **规避:** 同步升级到官方兼容矩阵要求的 Gradle 8.7。
- **风险:** 本地 Android SDK 未安装 API 35 平台导致构建失败。
  - **规避:** 先安装 `platforms;android-35` 与 `build-tools;35.0.0` 再执行验证。
- **风险:** server 复制到 app raw 目录的打包链路在新 AGP 下回归失败。
  - **规避:** 使用 `:app:assembleDebug` 做联合构建验证，确保 `:server:copyDebug` 仍被触发。

## 验证计划
1. 执行 `./gradlew -version`，确认 wrapper 已切换到 Gradle 8.7。
2. 执行 `./gradlew :app:assembleDebug`，验证 app/server 联合构建通过。
3. 静态检查 `app` / `server` 的 `compileSdk` / `targetSdk` 已升级为 35。
