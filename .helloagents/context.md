# 项目上下文

## 1. 基本信息

```yaml
名称: android-scrcpy（Easycontrol）
描述: 基于 Scrcpy 深度改造的 Android 控制 Android 项目，主控端为 Android App，被控端运行嵌入式 server。
类型: Android 应用 + Android 端嵌入式 server
状态: 维护中（根据 README 与发布/反馈说明推断）
```

## 2. 技术上下文

```yaml
语言: Java, XML, AIDL, Gradle（Groovy DSL）
框架: Android SDK, ViewBinding, USB Host, MediaCodec/AIDL
包管理器: Gradle Wrapper
构建工具: Android Gradle Plugin 8.6.1 + Gradle 8.7
```

### 主要依赖
| 依赖 | 版本 | 用途 |
|------|------|------|
| Android SDK | compileSdk/targetSdk 35 | Android 应用、系统服务与媒体/输入能力 |
| Java | sourceCompatibility/targetCompatibility 1.8 | 主体业务与协议实现 |
| AIDL | 内置 Android 机制 | server 模块跨进程/系统接口声明 |
| Gradle Wrapper | 8.7 | 多模块构建与打包 |
| 第三方库 | 未检测到显式声明 | 当前仓库主要依赖 Android/Java 标准能力 |

## 3. 项目概述

### 核心功能
- Android 设备控制 Android 设备（主控端/被控端均为 Android）
- 支持 USB 与无线 ADB 连接
- 支持音频传输、视频解码与控制事件转发
- 支持多设备列表、剪贴板同步、小窗/全屏显示与旋转控制

### 项目边界
```yaml
范围内:
  - Android 主控端 UI、ADB 连接、设备配置持久化
  - 被控端 server 的音视频采集、控制事件处理与 socket 通信
  - 根目录 README / DONATE / HOW_TO_USE / PRIVACY 等说明文档与配图资源
  - README / DONATE 已明确：开源源码可直接构建与使用，不再以内置激活/订单号作为前置条件；DONATE 仅描述自愿支持与历史发行版差异
范围外:
  - iOS、桌面端或 Web 控制端实现
  - 官方商业版或私有 cloud / 激活后端源码
  - 完整自动化测试体系
```

## 4. 开发约定

### 代码规范
```yaml
命名风格: Java 类使用 PascalCase，方法/字段使用 camelCase
文件命名: Activity/Helper/Entity 按职责分层；资源文件使用小写下划线
目录组织: easycontrol/app 与 easycontrol/server 双模块，根目录保存说明文档与图片资源
```

### 错误处理
```yaml
错误码格式: 未检测到统一错误码体系，主要依赖异常打印、Toast 与日志输出
日志级别: 以界面提示和工具类日志为主；当前 app 已新增进程内运行日志缓冲与查看页，可回看最近关键运行日志
```

### 测试要求
```yaml
测试框架: 未检测到自动化测试框架
覆盖率要求: 未定义
测试文件位置: 未检测到 test/、tests/ 或 androidTest/
```

### Git规范
```yaml
分支策略: 当前仓库以 main 分支为主
提交格式: 仓库内未声明统一提交规范
远端仓库: origin -> https://github.com/zcw199604/android-scrcpy.git
```

## 5. 当前约束（来自代码与现有文档）

| 约束 | 原因 | 来源 |
|------|------|------|
| `server` 模块产物需要通过 `:server:copyDebug` 或 `:server:copyRelease` 先复制为 `app/src/main/res/raw/easycontrol_server.jar` | Android 主控端依赖内嵌 server 载荷运行被控端逻辑，`ClientStream` 会读取 `R.raw.easycontrol_server` | `easycontrol/server/build.gradle`, `easycontrol/app/src/main/java/.../ClientStream.java` |
| 当前执行环境已在仓库内补齐本地 JDK 17 与 Android SDK | `easycontrol/gradlew` 会自动回退到 `.local-jdks/current`，`easycontrol/local.properties` 已固定 `sdk.dir=/mnt/android-scrcpy/.android-sdk`，当前 `:server:compileDebugJavaWithJavac` 与 `:app:assembleDebug` 已可执行 | 本次任务执行环境检测，`easycontrol/gradlew`, `easycontrol/local.properties`, `./gradlew :server:compileDebugJavaWithJavac`, `./gradlew :app:assembleDebug` |
| `app` 与 `server` 仍通过复制 raw 资源共享内嵌 server 载荷 | `app` 模块已在 `preDebugBuild` / `preReleaseBuild` 显式依赖 `:server:copyDebug` / `:server:copyRelease`，从而保留 `R.raw.easycontrol_server` 打包方式，并继续满足 AGP 8.6.1 / API 35 构建链路下的显式任务依赖校验；`ClientStream` 现会按 `versionCode + server载荷CRC` 生成被控端文件名，避免同版本号下继续复用旧 server.jar | `easycontrol/app/build.gradle`, `easycontrol/server/build.gradle`, `ClientStream.java` |
| GitHub tag release 发布链路需要沿用 release 构建顺序并提供签名配置 | 发布场景应先执行 `:server:copyRelease`，再执行 `:app:assembleRelease`；`app` release 需通过 `EC_RELEASE_*` 参数完成签名，workflow 对应依赖 GitHub Secrets，且 GitHub Release 只公开发布 app APK | `.github/workflows/android-release.yml`, `README.md`, `easycontrol/app/build.gradle`, `easycontrol/server/build.gradle` |
| 当前 Gradle 构建仅包含 `:app` 与 `:server` | `:cloud` 已不在当前构建中，历史 cloud/激活能力不应视为当前源码依赖 | `easycontrol/settings.gradle`, `modules/cloud.md` |
| 默认 ADB 密钥与软件独立密钥已分离 | `AppData.keyPair` 现优先从 `SharedPreferences(adb_key)` 读取默认密钥并兼容旧 `public.key/private.key` 文件迁移，`AppData.appKeyPair` 继续读取 app 私有文件；设备级 `useAppKey` 决定连接时使用哪把密钥，且 Manifest 已开启 `allowBackup=true` | `easycontrol/app/src/main/AndroidManifest.xml`, `AppData.java`, `PublicTools.java`, `AdbTools.java`, `DbHelper.java` |

## 6. 已知技术债务

| 债务描述 | 优先级 | 来源 | 建议处理时机 |
|---------|--------|------|-------------|
| 当前自动化范围主要覆盖 GitHub tag release 打包，尚未覆盖自动化测试链路 | P1 | `.github/workflows/android-release.yml`, `README.md`, `easycontrol/app/build.gradle` | 进行较大功能变更前补齐最小可运行验证链路 |
| 当前缺少真机 / ADB 设备回归环境，投屏、触控、音频、剪贴板与分辨率切换仍无法做端到端验证 | P2 | 本次执行环境检测 | 获取可控 Android 设备后继续功能回归 |
| `server` 产物仍通过 copy 任务回写到 `app/src/main/res/raw/` 源码目录 | P2 | `easycontrol/server/build.gradle`, `easycontrol/app/build.gradle`, `ClientStream.java` | 后续可考虑改为 variant-aware 生成资源目录，减少源码目录被构建产物污染的风险 |

## 7. 当前实施快照（2026-03-12）

```yaml
任务: scrcpy v3.3.4 近官方兼容同步
方案包: .helloagents/archive/2026-03/202603120724_scrcpy-v334-official-sync/
上游基线: /tmp/scrcpy-upstream 的 v3.3.4 tag -> fb6381f5b9bb96f3fa823d899f4c32de2ec84ab3
当前保留边界:
  - ClientStream 的 app_process 启动命令
  - main/video 双 socket 连接拓扑
  - ControlPacket 现有 1-9 控制协议与媒体事件格式
  - R.raw.easycontrol_server 的内嵌打包链路
已完成实现:
  - server 侧参数解析、协议常量、FakeContext/Clipboard/Input/Window/Surface/Display 兼容层同步
  - Device / Pointer / PointersState / VideoEncode / AudioEncode / AudioCapture 生命周期与兼容性改造
  - app 侧 ClientStream / ControlPacket / ClientPlayer 的协议一致性整理
阻断项:
  - 当前环境无真机 / ADB 设备，无法完成投屏、触控、音频、剪贴板、分辨率切换回归
```

> 当前本地构建环境已补齐 JDK 17 与 Android SDK 35，且已在 AGP 8.6.1 + Gradle 8.7 + compileSdk/targetSdk 35 组合下验证 `./gradlew :app:assembleDebug` 通过；剩余阻断是真机回归环境。
