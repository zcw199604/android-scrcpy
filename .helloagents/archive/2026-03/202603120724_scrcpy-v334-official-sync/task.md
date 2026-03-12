# 任务清单: scrcpy-v3.3.4-official-sync

目录: `.helloagents/archive/2026-03/202603120724_scrcpy-v334-official-sync/`

---

## 0. 上游基线与差异盘点
- [√] 0.1 在执行环境中固定 scrcpy 官方基线到 `v3.3.4`（优先复用 `/tmp/scrcpy-upstream`，不存在时重新克隆并 checkout 标签），记录 tag、commit SHA 与参考目录，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server
- [√] 0.2 对比 `easycontrol/server` 当前类清单与 scrcpy `v3.3.4` server 类清单，明确“修改现有文件 / 新增适配文件 / 仅借逻辑不借结构”的映射结果，重点覆盖 `Server`、`entity/**`、`helper/**`、`wrappers/**`，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server，依赖任务0.1

## 1. 对外契约护栏
- [√] 1.1 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ClientStream.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/Server.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/Options.java` 中落实现有启动命令、参数兼容解析与双 socket 连接契约，显式兼容 `listenClip` / `listenerClip`，并以 `how.md` 中“参数兼容矩阵”为回归基准，验证 why.md#需求-保持-easycontrol-现有启动与连接契约-场景-现有-app_process-启动参数仍可直接启动新版-server，依赖任务0.2
- [√] 1.2 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ControlPacket.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/ControlPacket.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/Server.java` 中落实现有 1-9 控制协议、心跳保活与媒体包格式的兼容分发逻辑，并以 `how.md` 中“协议基线表”为回归基准，验证 why.md#需求-保持-easycontrol-现有启动与连接契约-场景-现有-app_process-启动参数仍可直接启动新版-server，依赖任务1.1

## 2. scrcpy v3.3.4 兼容层同步
- [√] 2.1 在 `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/FakeContext.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/wrappers/ClipboardManager.java` 中同步 scrcpy v3.3.4 的上下文与剪贴板兼容实现，并适配当前静态调用方式，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server，依赖任务1.2
- [√] 2.2 在 `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/wrappers/InputManager.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/wrappers/WindowManager.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/wrappers/SurfaceControl.java` 中同步最新版输入/窗口/Surface 兼容逻辑，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server，依赖任务2.1
- [√] 2.3 在 `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/wrappers/DisplayManager.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/DisplayInfo.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/Device.java` 中同步最新版显示信息解析与虚拟显示兼容策略，同时保留现有分辨率控制语义，验证 why.md#需求-保持设备控制扩展能力-场景-分辨率切换旋转背光与电源控制仍按旧协议工作，依赖任务2.2
- [√] 2.4 执行第一阶段构建闸门：在具备 Java / Android SDK 环境时运行 `cd easycontrol && ./gradlew :server:compileDebugJavaWithJavac`，若环境缺失则记录阻断信息，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server，依赖任务2.3
> 备注: 已在仓库内安装本地 Temurin JDK 17（`.local-jdks/jdk-17`）与 Android SDK（`.android-sdk`），并通过 `easycontrol/local.properties` 固定 `sdk.dir`；`cd easycontrol && ./gradlew :server:compileDebugJavaWithJavac` 已验证成功。

## 3. server 近官方内核重构
- [√] 3.1 在 `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/Server.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/Options.java` 中同步 scrcpy v3.3.4 的生命周期、超时/清理策略与参数模型实现思路，但保留当前静态外壳、入口与连接协议边界，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server，依赖任务2.4
- [√] 3.2 在 `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/Device.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/Pointer.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/PointersState.java` 中吸收 scrcpy 新版输入与显示控制实现思路，保持触摸、按键、旋转、电源与分辨率控制行为不变，验证 why.md#需求-保持媒体与控制能力不回退-场景-投屏触控与按键控制持续可用，依赖任务2.3、3.1
- [√] 3.3 在 `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/VideoEncode.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/AudioEncode.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/AudioCapture.java` 中同步 scrcpy v3.3.4 的媒体采集与编码兼容逻辑，同时保持现有 main/video 双通道封包方式，验证 why.md#需求-保持媒体与控制能力不回退-场景-音频能力在支持设备上保持工作，依赖任务3.2
- [√] 3.4 执行第二阶段构建闸门：在具备 Java / Android SDK 环境时运行 `cd easycontrol && ./gradlew :server:compileDebugJavaWithJavac`，若环境缺失则记录阻断信息，验证 why.md#需求-同步-scrcpy-v334-的兼容性与生命周期改进-场景-新版-android-系统服务兼容逻辑被吸收到当前-server，依赖任务3.3
> 备注: 第二次编译闸门复核已通过；本地 JDK 17 + Android SDK 环境下 `:server:compileDebugJavaWithJavac` 可稳定完成。

## 4. app 侧兼容适配
- [√] 4.1 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ClientStream.java`、`easycontrol/app/src/main/java/top/saymzx/easycontrol/app/entity/Device.java`、`easycontrol/app/src/main/java/top/saymzx/easycontrol/app/DeviceDetailActivity.java` 中补齐与重构后 server 的参数兼容、协商容错与配置传递，验证 why.md#需求-保持-easycontrol-现有启动与连接契约-场景-现有-app_process-启动参数仍可直接启动新版-server，依赖任务3.4
> 备注: `ClientStream` 已同步参数常量；`app/entity/Device.java` 与 `DeviceDetailActivity.java` 经审计后保持现状即可兼容当前 server 参数模型。
- [√] 4.2 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ClientController.java`、`easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ControlPacket.java`、`easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/ControlPacket.java` 中回归并固化现有控制协议行为，确保分辨率/旋转/背光/电源等扩展能力不回退，验证 why.md#需求-保持设备控制扩展能力-场景-分辨率切换旋转背光与电源控制仍按旧协议工作，依赖任务4.1
> 备注: 现有扩展控制协议编号与 `ClientController` 调用链保持不变，仅统一 app/server 常量表达。
- [√] 4.3 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/decode/VideoDecode.java`、`easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/decode/AudioDecode.java`、`easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ClientPlayer.java` 中适配新版 server 的初始化/编解码细节变化并保持现有播放行为，验证 why.md#需求-保持媒体与控制能力不回退-场景-音频能力在支持设备上保持工作，依赖任务3.3、4.2
> 备注: `ClientPlayer` 已改为复用协议事件常量；`VideoDecode` / `AudioDecode` 经静态审计后无需额外改动。

## 5. 打包链路与构建验证
- [√] 5.1 在 `easycontrol/server/build.gradle`、`easycontrol/app/build.gradle` 中确认并修正新版 server 改造后所需的构建/复制链路，继续产出 `app/src/main/res/raw/easycontrol_server.jar`，同时检查 `aidl true` 相关生成路径与构建依赖未被破坏，验证 why.md#需求-保持-easycontrol-现有启动与连接契约-场景-现有-app_process-启动参数仍可直接启动新版-server，依赖任务4.3
> 备注: 已在 `easycontrol/app/build.gradle` 中为 `preDebugBuild` / `preReleaseBuild` 显式挂接 `:server:copyDebug` / `:server:copyRelease`，修复 AGP 8.2 下对 `easycontrol_server.jar` 的隐式任务依赖校验错误，并保持 `app/src/main/res/raw/easycontrol_server.jar` 打包链路不变。
- [√] 5.2 执行构建验证：在具备 Java / Android SDK 环境时运行 `cd easycontrol && ./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug`，并记录环境受限时的阻断信息，验证 why.md#需求-保持-easycontrol-现有启动与连接契约-场景-现有-app_process-启动参数仍可直接启动新版-server，依赖任务5.1
> 备注: 已在仓库内本地 JDK 17 + Android SDK 环境下通过 `cd easycontrol && ./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug` 与 `cd easycontrol && ./gradlew :app:assembleDebug` 双重验证；debug 构建链路可自动触发 `:server:copyDebug` 并成功产出 app APK。

## 6. 安全检查
- [√] 6.1 执行安全检查（按 G9: 输入验证、敏感信息处理、权限控制、EHRB 风险规避），重点复核系统服务反射、剪贴板访问、输入注入、电源控制与 shell 启动路径，依赖任务5.2
> 备注: 已完成静态安全审查与 `git diff --check` 校验，未发现新增明文密钥、危险命令拼接或越界协议改动。

## 7. 文档更新
- [√] 7.1 更新 `.helloagents/context.md`、`.helloagents/modules/easycontrol_server.md`、`.helloagents/modules/easycontrol_app.md`，同步记录 scrcpy v3.3.4 基线、兼容层设计与协议保留边界，依赖任务6.1
> 备注: 已同步 `.helloagents/context.md`、`.helloagents/modules/easycontrol_server.md`、`.helloagents/modules/easycontrol_app.md` 的 scrcpy v3.3.4 兼容边界与阻断信息。
- [√] 7.2 更新 `.helloagents/CHANGELOG.md`，补充本次“近官方架构同步”的变更说明与验证结论，依赖任务7.1
> 备注: 已在 `.helloagents/CHANGELOG.md` 记录本轮近官方兼容同步与构建阻断。

## 8. 功能回归
- [X] 8.1 执行回归验证清单：投屏、触控、音频、剪贴板、分辨率切换、旋转、背光、电源控制、心跳超时、断线恢复；若当前环境无法真机验证，明确列出未完成项与所需环境，验证 why.md#需求-保持媒体与控制能力不回退-场景-投屏触控与按键控制持续可用，依赖任务5.2
> 备注: 当前仓库内 JDK / Android SDK 与 debug 构建已验证通过，但仍缺少真机/ADB 设备环境，无法完成投屏、触控、音频、剪贴板、分辨率切换、旋转、背光、电源控制等端到端回归。
