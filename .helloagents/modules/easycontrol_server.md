# easycontrol_server

## 职责

被控端嵌入式 server 模块，负责在 Android 设备侧初始化系统服务包装器、建立主控端 socket 连接、采集编码音视频流，并执行触摸、按键、剪贴板与屏幕相关控制指令。

## 接口定义

### 公共入口
| 函数/方法 | 参数 | 返回值 | 说明 |
|----------|------|--------|------|
| `Server.main(String... args)` | `args` | `void` | 解析参数、初始化系统管理器、建立双 socket 连接并启动音视频/控制线程。 |
| `Options.parse(String... args)` | `args` | `void` | 解析 `serverPort`、`isAudio`、`maxSize`、`supportH265` 等运行参数。 |
| `ControlPacket.sendVideoEvent(long, ByteBuffer)` | `pts`, `data` | `void` | 向视频 socket 写入带时间戳的视频帧数据。 |
| `ControlPacket.sendAudioEvent(ByteBuffer)` | `data` | `void` | 向主 socket 写入音频数据包。 |
| `ControlPacket.handleTouchEvent()` | 无 | `void` | 从主 socket 读取触摸事件并转发到 `Device.touchEvent(...)`。 |

### 数据结构
| 字段/类型 | 位置 | 说明 |
|----------|------|------|
| `Options` | `entity/Options.java` | server 运行时可调参数集合。 |
| `Device` | `entity/Device.java` | 被控端显示、输入与设备状态操作入口。 |
| `DisplayInfo` / `Pointer` | `entity/` | 显示信息与输入指针状态模型。 |

## 行为规范

### 启动 server
**条件**: 主控端下发并运行嵌入式 server。
**行为**: `Server.main(...)` 调用 `Options.parse()`，通过 `ServiceManager` 初始化 `WindowManager`、`DisplayManager`、`InputManager`、`ClipboardManager` 与 `SurfaceControl` 包装器。
**结果**: server 进入可建立连接和处理控制命令的状态。

### 媒体与控制通道工作
**条件**: 主 socket 与视频 socket 已建立。
**行为**: server 并行运行视频输出、音频输入/输出、控制输入线程；控制通道根据命令类型触发触摸、按键、剪贴板、旋转与分辨率变更。
**结果**: 主控端持续接收媒体数据，并可实时控制被控端。

### 打包到主控端应用
**条件**: 执行 `server` 模块的 `copyRelease` 或 `copyDebug` 任务。
**行为**: `copyDebug` 直接复制 debug APK；`copyRelease` 会先执行 `assembleRelease`，再在任务执行阶段解析 signed/unsigned release APK，并复制重命名为 `app/src/main/res/raw/easycontrol_server.jar`；`app` 模块的 `preDebugBuild` / `preReleaseBuild` 已显式依赖对应 copy 任务。
**结果**: 主控端 APK 可继续以内嵌 `R.raw.easycontrol_server` 的方式分发 server 载荷，并避免 AGP 8.2 的隐式依赖校验错误。

## 依赖关系

```yaml
依赖: repository_docs
被依赖: easycontrol_app
```

## 近期实现快照（2026-03-12）

### scrcpy v3.3.4 兼容同步
- 已在不改变 `Server.main()` 静态入口、双 socket 拓扑和 `ControlPacket` 既有协议编号的前提下，对齐 scrcpy v3.3.4 的系统兼容思路。
- 已同步的 server 侧重点包括：
  - `Options` / `Server`：参数常量化、`listenClip` / `listenerClip` 兼容、释放与超时清理增强
  - `FakeContext` / `ClipboardManager`：framework clipboard 优先 + 旧 `IClipboard` 反射兜底
  - `InputManager` / `WindowManager` / `SurfaceControl` / `DisplayManager` / `DisplayInfo`：新版显示、旋转、电源、IME 与 Android 14+ physical display 兼容能力
  - `Device` / `Pointer` / `PointersState`：多指顺序与 localId 分离，保持现有触控协议不变
  - `VideoEncode` / `AudioEncode` / `AudioCapture`：重配释放、真实音频读取长度、时间戳与缓冲区大小修正

### 当前阻断
- 仓库内 JDK 17 + Android SDK 已补齐，`./gradlew :server:compileDebugJavaWithJavac`、`./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug` 与 `./gradlew :app:assembleDebug` 均已通过。
- 真机回归（投屏、触控、音频、剪贴板、分辨率切换、旋转、背光、电源控制）待有设备环境后补齐。
