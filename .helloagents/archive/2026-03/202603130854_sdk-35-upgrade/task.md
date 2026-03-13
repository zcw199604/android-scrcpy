# 任务清单: sdk-35-upgrade

目录: `.helloagents/plan/202603130854_sdk-35-upgrade/`

- [√] 1. 核查当前 AGP / Gradle 与 API 35 的官方兼容要求，并确定升级目标版本
- [√] 2. 升级 `app` / `server` 模块 AGP 与 SDK 配置到 API 35 可支持版本
- [√] 3. 升级 Gradle Wrapper 并补齐本地 Android 35 SDK 组件
- [√] 4. 执行 `./gradlew :app:assembleDebug` 验证构建
- [√] 5. 同步更新知识库、变更日志与归档索引
