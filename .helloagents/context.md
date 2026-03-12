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
构建工具: Android Gradle Plugin 8.2.2 + Gradle 8.2
```

### 主要依赖
| 依赖 | 版本 | 用途 |
|------|------|------|
| Android SDK | compileSdk/targetSdk 34 | Android 应用、系统服务与媒体/输入能力 |
| Java | sourceCompatibility/targetCompatibility 1.8 | 主体业务与协议实现 |
| AIDL | 内置 Android 机制 | server 模块跨进程/系统接口声明 |
| Gradle Wrapper | 8.2 | 多模块构建与打包 |
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
日志级别: 以界面提示和工具类日志为主，未见集中日志框架
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
| 当前执行环境未提供 Java / Android SDK，且 `bash ./gradlew :server:copyDebug` 已在 Gradle Wrapper 启动前失败 | `JAVA_HOME` 未设置且 PATH 中不存在 `java`，无法进入 Android 构建阶段 | 本次任务执行环境检测，`easycontrol/gradlew` |
| `app` 与 `server` 之间没有显式 Gradle 模块依赖来自动生成 raw 资源 | 本地构建时需先执行 `:server:copyDebug` 或 `:server:copyRelease`，再打包 `app` | `easycontrol/server/build.gradle`, `ClientStream.java` |
| GitHub tag release 发布链路需要沿用 release 构建顺序并提供签名配置 | 发布场景应先执行 `:server:copyRelease`，再执行 `:app:assembleRelease`；`app` release 需通过 `EC_RELEASE_*` 参数完成签名，workflow 对应依赖 GitHub Secrets，且 GitHub Release 只公开发布 app APK | `.github/workflows/android-release.yml`, `README.md`, `easycontrol/app/build.gradle`, `easycontrol/server/build.gradle` |
| 当前 Gradle 构建仅包含 `:app` 与 `:server` | `:cloud` 已不在当前构建中，历史 cloud/激活能力不应视为当前源码依赖 | `easycontrol/settings.gradle`, `modules/cloud.md` |

## 6. 已知技术债务

| 债务描述 | 优先级 | 来源 | 建议处理时机 |
|---------|--------|------|-------------|
| 当前自动化范围主要覆盖 GitHub tag release 打包，尚未覆盖自动化测试链路 | P1 | `.github/workflows/android-release.yml`, `README.md`, `easycontrol/app/build.gradle` | 进行较大功能变更前补齐最小可运行验证链路 |
| 当前环境缺少 Java / Android SDK，且 `bash ./gradlew :server:copyDebug` 因 `JAVA_HOME is not set` 失败，难以在 CLI 中完成编译级回归 | P2 | 本次执行环境检测 | 需要正式验收或发布前在完整 Android 环境复验 |
| `server` 产物复制到 `app` raw 目录依赖手动 Gradle 任务顺序 | P2 | `server/build.gradle` 与 `ClientStream.java` | 后续可考虑将该链路固化到统一构建任务中 |
