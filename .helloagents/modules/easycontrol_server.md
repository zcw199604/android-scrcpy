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
**行为**: `copyDebug` 直接复制 debug APK；`copyRelease` 会先执行 `assembleRelease`，再在任务执行阶段解析 signed/unsigned release APK，并复制重命名为 `app/src/main/res/raw/easycontrol_server.jar`。
**结果**: 主控端 APK 可携带并分发 server 载荷。

## 依赖关系

```yaml
依赖: repository_docs
被依赖: easycontrol_app
```
