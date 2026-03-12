# 变更提案: scrcpy-v3.3.4-official-sync

## 需求背景
当前 `easycontrol/server` 中与 scrcpy 相关的实现并不是官方 scrcpy 的轻量镜像，而是基于早期 scrcpy server 代码长期演化出的深度分叉版本。已确认当前官方最新稳定版为 **scrcpy v3.3.4**（2025-12-17 发布）。

用户已经明确要求：
- 同步 `easycontrol/server` 中所有借鉴 scrcpy 的代码；
- 允许联动修改 `easycontrol/app`、Gradle 配置与知识库；
- 必须保持以下外部行为不变：
  - `ClientStream` 现有 `app_process` 启动命令；
  - 当前双 socket 连接方式；
  - 现有 `ControlPacket` 包格式；
  - `R.raw.easycontrol_server` 的内嵌打包方式；
- 验收标准必须覆盖：投屏、触控、音频、剪贴板、分辨率切换均保持可用。

这意味着本次变更不能简单替换为官方 `scrcpy-server`，而需要在“尽量靠近 scrcpy v3.3.4 内核结构”和“保持 Easycontrol 现有对外契约”之间建立适配层与迁移边界。

## 变更内容
1. 以 **scrcpy v3.3.4** 为基线，重构 `easycontrol/server` 中借鉴 scrcpy 的兼容层、生命周期管理与采集/控制实现。
2. 保留 Easycontrol 现有外部契约：启动参数、双 socket、`ControlPacket`、raw 资源打包与 Android-to-Android 的控制流程。
3. 在重构前固定 scrcpy v3.3.4 对照基线、协议护栏与静态架构边界，避免“边改边猜”导致返工。
4. 必要时联动改造 `easycontrol/app`，仅用于适配新版 server 内部变化，不改变现有用户可见操作路径。
5. 补充知识库与变更记录，明确本次“近官方架构同步”的迁移边界、ADR 与验证矩阵。

## 影响范围
- **模块:** `easycontrol_server`、`easycontrol_app`、`repository_docs`
- **文件:**
  - `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/**`
  - `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/Pointer.java`
  - `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/entity/PointersState.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/**`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/entity/Device.java`
  - `easycontrol/server/build.gradle`
  - `easycontrol/app/build.gradle`
  - `.helloagents/context.md`
  - `.helloagents/modules/easycontrol_server.md`
  - `.helloagents/modules/easycontrol_app.md`
  - `.helloagents/CHANGELOG.md`
- **API:** 无对外 HTTP/API 变更；仅涉及 app 与嵌入式 server 的内部通信与启动契约
- **数据:** 无数据库 schema 变更；仅涉及运行期控制协议和媒体流元数据

## 核心场景

### 需求: 保持 Easycontrol 现有启动与连接契约
**模块:** easycontrol_app / easycontrol_server
在把 server 内核同步到 scrcpy v3.3.4 思路的同时，主控端仍必须以当前方式启动并连接被控端嵌入式 server。

#### 场景: 现有 app_process 启动参数仍可直接启动新版 server
主控端通过 `ClientStream` 推送 `easycontrol_server.jar` 并执行现有 `app_process -Djava.class.path=... top.saymzx.easycontrol.server.Server ...` 命令。
- server 能正确解析现有参数集合并完成初始化。
- server 同时兼容历史键 `listenerClip` 与当前 app 实际发送的 `listenClip`，且优先保持当前 app 启动命令不变。
- `R.raw.easycontrol_server` 的打包与下发链路保持可用。
- 主控端仍通过当前双 socket 连接方式建立控制/媒体连接。

### 需求: 保持媒体与控制能力不回退
**模块:** easycontrol_server / easycontrol_app
同步 scrcpy v3.3.4 后，现有媒体与控制能力必须保持工作。

#### 场景: 投屏、触控与按键控制持续可用
主控端连接被控端后，用户执行镜像显示、触摸、返回/Home/任务键等操作。
- 视频流可正常解码显示。
- 触控与按键事件可被正确注入到被控端。
- 心跳与断线处理行为不弱于当前版本。

#### 场景: 音频能力在支持设备上保持工作
主控端连接 Android 12+ 被控端并启用音频。
- server 继续根据设备能力协商 AAC/Opus。
- app 侧能按现有流程接收并解码音频。
- 不支持音频时仍维持当前降级行为而非整体失败。

### 需求: 保持设备控制扩展能力
**模块:** easycontrol_server
Easycontrol 在官方 scrcpy 基础上追加的分辨率切换、旋转、背光与电源控制能力必须继续保留。

#### 场景: 分辨率切换、旋转、背光与电源控制仍按旧协议工作
主控端发送当前 `ControlPacket` 定义的控制指令。
- server 继续识别现有指令编号与参数格式。
- 分辨率切换与虚拟显示/编码面更新保持一致。
- 旋转、背光、电源控制语义不变。

### 需求: 同步 scrcpy v3.3.4 的兼容性与生命周期改进
**模块:** easycontrol_server
近官方架构同步应真正吸收最新版 scrcpy 的兼容性、系统服务适配和生命周期处理，而不是仅停留在命名对齐。

#### 场景: 新版 Android 系统服务兼容逻辑被吸收到当前 server
升级后的 server 在新版 Android 系统上运行。
- `FakeContext`、`InputManager`、`ClipboardManager`、`DisplayManager`、`WindowManager`、`SurfaceControl` 等兼容层对齐到 scrcpy v3.3.4 时代的实现思路。
- server 生命周期、清理、异常处理和资源释放比当前版本更稳健。
- 新增的兼容逻辑不会破坏 Easycontrol 的现有协议与用户流程。

## 风险评估
- **风险:** 官方 scrcpy v3.3.4 架构已与当前仓库深度分叉，直接替换会导致协议、连接方式与打包链路同时失效。
- **缓解:** 采用“近官方内核 + 兼容外壳”的迁移方案，先固定外部契约，再逐层替换内部实现。
- **风险:** 当前 `Server` / `ControlPacket` / `Device` 依赖大量静态字段与方法，若直接照搬 upstream 的实例化架构，改动面会瞬间扩散。
- **缓解:** 在本轮迁移中保留静态外壳边界，只同步兼容性与内部逻辑，不在同一轮内强推全量实例化重构。
- **风险:** 当前 CLI 环境缺少 Java / Android SDK，无法在本地完成完整构建与真机回归。
- **缓解:** 在实现阶段优先完成静态一致性与代码路径审计，并把 Gradle 构建与真机验证列为强制验收任务。
- **风险:** Easycontrol 自定义控制指令（分辨率、旋转、背光、电源）在迁移过程中容易被新版内部抽象绕开。
- **缓解:** 单独保留旧协议适配层，并为自定义控制能力建立回归检查清单。
