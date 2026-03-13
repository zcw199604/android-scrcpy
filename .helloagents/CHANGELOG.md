# 变更日志

## [Unreleased]

### 版本
- **[easycontrol_app]**: 将 Android 安装版本提升到 `versionCode 10001` / `versionName 1.0.1`，并发布 Git annotated tag `v1.0.1`，使安装版本与发布标签保持一致 — by zcw
  - 方案: [202603131102_version-v101](archive/2026-03/202603131102_version-v101/)
  - 验证: `./gradlew :app:assembleDebug`、`aapt dump badging`、`git push origin v1.0.1`

### 连接认证
- **[easycontrol_app]**: 新增默认 ADB 密钥与软件独立密钥双轨策略；默认密钥优先从 `SharedPreferences` 读取并兼容旧文件迁移，设备可通过“使用软件独立密钥”切换到 app 私有密钥；同时开启 `allowBackup` 以为系统备份恢复默认密钥提供前提 — by zcw
  - 方案: [202603130923_adb-key-backup-and-selection](archive/2026-03/202603130923_adb-key-backup-and-selection/)
  - 验证: `./gradlew :app:assembleDebug` 已通过

### 构建链路
- **[easycontrol_app, easycontrol_server]**: 将 Android 构建链路升级到 AGP `8.6.1` + Gradle `8.7`，并把 `app` / `server` 的 `compileSdk`、`targetSdk` 升级到 `35`；同时把 `BuildConfig` 开关迁移到 app 模块级 `buildFeatures`，消除 AGP 8.6 的构建弃用告警 — by zcw
  - 方案: [202603130854_sdk-35-upgrade](archive/2026-03/202603130854_sdk-35-upgrade/)
  - 验证: `./gradlew -version`、`./gradlew :app:assembleDebug` 已通过

### 修复
- **[easycontrol_app]**: 修复 release 包在 `versionCode` 不变时继续复用旧被控端 server.jar 的问题；`ClientStream` 现按 `versionCode + server载荷CRC` 生成远端文件名，确保新的内嵌 server 修复能真正下发到被控端 — by zcw
  - 方案: [202603121316_server-jar-sync-fingerprint](archive/2026-03/202603121316_server-jar-sync-fingerprint/)
  - 验证: `./gradlew :app:assembleDebug` 已通过

### 修复
- **[easycontrol_server]**: 修复部分 Android 15 / 厂商 ROM 上 `SurfaceControl.createDisplay(String, boolean)` 缺失导致的 server 启动后断开问题；`VideoEncode` 现优先尝试 `DisplayManager` 显示 API，失败后回退 `SurfaceControl` — by zcw
  - 方案: [202603121301_surfacecontrol-display-fallback](archive/2026-03/202603121301_surfacecontrol-display-fallback/)
  - 验证: `./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug` 已通过

### 新增
- **[easycontrol_app]**: 新增应用内“查看运行日志”界面，并将主界面启动、ADB 建连、server 启动、数据通道建立、断开与自动重连等关键运行日志保存在进程内存中 — by zcw
  - 方案: [202603121200_runtime-log-view](archive/2026-03/202603121200_runtime-log-view/)
  - 验证: `./gradlew :app:assembleDebug` 已通过

### 开发环境
- **[repository_docs]**: 在仓库内安装本地 Temurin JDK 17（`.local-jdks/jdk-17`）与 Android SDK（`.android-sdk`），并让 `easycontrol/gradlew` / `easycontrol/local.properties` 自动指向仓库内构建环境 — by zcw
  - 结果: `./gradlew -version`、`./gradlew :server:compileDebugJavaWithJavac`、`./gradlew :app:assembleDebug` 已验证成功
### 同步/重构
- **[easycontrol_server]**: 在保持 `ClientStream` 启动命令、双 socket、`ControlPacket` 包格式与 `R.raw.easycontrol_server` 打包链路不变的前提下，同步 scrcpy v3.3.4 的兼容层与生命周期实现，并修复 AGP 8.2 下 app/server 构建链路的显式任务依赖 — by zcw
  - 方案: [202603120724_scrcpy-v334-official-sync](archive/2026-03/202603120724_scrcpy-v334-official-sync/)
  - 范围: `Server` / `Options` / `Device` / `Pointer` / `PointersState` / `FakeContext` / `ClipboardManager` / `InputManager` / `WindowManager` / `SurfaceControl` / `DisplayManager` / `DisplayInfo` / `VideoEncode` / `AudioEncode` / `AudioCapture` / `easycontrol/app/build.gradle`
  - 验证: `./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug` 与 `./gradlew :app:assembleDebug` 已通过；真机回归待补

### 协议整理
- **[easycontrol_app]**: 同步 app 侧 `ClientStream` / `ControlPacket` / `ClientPlayer` 的协议常量表达，保持现有连接与播放行为不变 — by zcw
  - 方案: [202603120724_scrcpy-v334-official-sync](archive/2026-03/202603120724_scrcpy-v334-official-sync/)
### 交互优化
- **[easycontrol_app]**: 补充连接失败场景的用户可见错误提示，区分 ADB 未连通、调试未授权、USB 通道异常、被控端服务连接失败与超时 — by zcw
  - 验证: `./gradlew :app:compileDebugJavaWithJavac :app:mergeDebugResources`、`./gradlew :app:assembleDebug` 已通过
### 连接策略
- **[easycontrol_app]**: 为网络设备新增“强制走 ADB forward”连接时配置项，允许跳过 direct socket 直连，直接使用 ADB 转发建立投屏通道 — by zcw
  - 验证: `./gradlew :app:assembleDebug` 已通过
### 新增
- **[knowledge-base]**: 初始化 HelloAGENTS 项目知识库（入口、上下文、模块索引、归档索引） — by zcw
  - 方案: 无（通过 `~init` 初始化）
  - 决策: 无


### 快速修改
- **[repository_docs]**: 修复 GitHub Actions 发布工作流的 YAML 语法错误，恢复 `android-release.yml` 可解析状态 — by zcw
  - 类型: 快速修改（无方案包）
  - 文件: .github/workflows/android-release.yml:196-213

## [1.6.1] - 2026-03-12

### 修复
- **[repository_docs]**: 修正 GitHub tag release APK 的签名与发布策略，避免继续公开发布不可安装或易误装的 APK 资产 — by zcw
  - 方案: [202603120032_fix-release-apk-signing](archive/2026-03/202603120032_fix-release-apk-signing/)
  - 决策: fix-release-apk-signing#D001(缺少签名配置时直接失败，而不是继续上传不可安装 APK)

## [1.6.0] - 2026-03-11

### 新增
- **[repository_docs]**: 新增 GitHub tag release 自动打包工作流，并同步 README / 知识库中的发布说明到当前仓库事实 — by zcw
  - 方案: [202603111449_github-tag-release-build](archive/2026-03/202603111449_github-tag-release-build/)
  - 决策: github-tag-release-build#D002(同时上传 workflow artifact 并同步 GitHub Release 资产)

## [1.5.9] - 2026-03-11

### 修复
- **[repository_docs]**: 清理过时的激活/构建说明，改为项目支持说明，并记录本地打包验证受 `JAVA_HOME` 缺失阻塞 — by zcw
  - 方案: [202603111413_cleanup-docs-build-validation](archive/2026-03/202603111413_cleanup-docs-build-validation/)
  - 决策: cleanup_docs_build_validation#D001(文档按当前代码事实与已验证构建结果更新)

## [1.5.8] - 2026-03-11

### 修复
- **[easycontrol_app]**: 移除首次进入与 USB 入口的捐赠/激活门控，并清理缺失的激活页面、相关资源与 cloud 构建残留 — by zcw
  - 方案: [202603111355_remove-activation-logic](archive/2026-03/202603111355_remove-activation-logic/)
  - 决策: remove_activation_logic#D001(直接删除激活模块并开放入口)

## 基线版本
- `easycontrol/app`: `versionCode 10001` / `versionName 1.0.1`
- `easycontrol/server`: `versionCode 20000` / `versionName 2.0.0`
- 当前知识库已累计归档 15 个 HelloAGENTS 方案包（见 `archive/_index.md`）。
