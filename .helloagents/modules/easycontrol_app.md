# easycontrol_app

## 职责

Android 主控端应用模块，负责设备列表管理、USB/网络 ADB 连接、客户端流/控制/播放组件协调、界面展示以及本地配置持久化。当前开源版代码已移除首次进入和 USB 入口的捐赠/激活门控。

## 接口定义

### 公共入口
| 函数/方法 | 参数 | 返回值 | 说明 |
|----------|------|--------|------|
| `MainActivity.onCreate(Bundle)` | `savedInstanceState` | `void` | 初始化 `AppData`、设备列表、广播监听与启动时自动连接逻辑，不再跳转激活页。 |
| `UsbActivity.onCreate(Bundle)` | `savedInstanceState` | `void` | USB 附加入口，直接启动主界面或发送 USB 更新广播。 |
| `Client.startDevice(Device)` | `device` | `void` | 创建并启动单个设备会话，并记录运行阶段日志。 |
| `Client.sendAction(String, String, ByteBuffer, int)` | `uuid`, `action`, `byteBuffer`, `delay` | `void` | 向已建立的客户端会话发送控制动作。 |
| `LogActivity.onResume()` | 无 | `void` | 刷新并展示当前进程内存中的运行日志。 |
| `AdbTools.connectADB(Device)` | `device` | `Adb` | 为 USB/网络设备建立或复用 ADB 连接。 |
| `ClientStream.startServer(Device)` | `device` | `void` | 根据内嵌 server 载荷生成带 CRC 指纹的远端文件名，并决定是否需要重新推送被控端 server。 |
| `DbHelper.getAll()` | 无 | `ArrayList<Device>` | 读取已保存的设备配置列表。 |

### 数据结构
| 字段/类型 | 位置 | 说明 |
|----------|------|------|
| `Device` | `entity/Device.java` | 设备连接参数、窗口行为与分辨率等持久化配置。 |
| `Setting` | `entity/Setting.java` | 应用级偏好，如语言、本地 UUID 与显示设置；已不再保存激活状态。 |
| `AppData` | `entity/AppData.java` | 全局上下文、系统服务、数据库与 ADB 密钥入口。 |

## 行为规范

### 主界面启动
**条件**: 用户打开应用。
**行为**: `MainActivity` 调用 `AppData.init()`，注册广播、装配 `DeviceListAdapter`，并在延时任务中按 `connectOnStart` 自动连接设备。
**结果**: 主界面完成初始化并展示可连接设备列表，无需先通过激活页。

### USB 附加处理
**条件**: 系统收到 USB 设备附加事件。
**行为**: `UsbActivity` 不再检查 `isActive`，而是直接启动 `MainActivity` 或广播 `ACTION_UPDATE_USB`。
**结果**: USB 连接流程与普通主界面启动保持一致。

### 建立设备控制会话
**条件**: 用户点击设备或触发自动连接。
**行为**: `Client` 组装 `ClientStream`、`ClientController`、`ClientPlayer`，根据设备配置执行分辨率、亮屏/熄屏与窗口切换动作。
**结果**: 主控端开始接收音视频流并向被控端发送控制事件。

### 配置持久化
**条件**: 设备会话关闭或用户修改设备参数。
**行为**: `DbHelper` 通过 SQLite 表 `DevicesDb` 保存设备配置；`Setting` 通过 `SharedPreferences` 保存语言、本地 UUID 等应用级开关。
**结果**: 设备和应用设置在下次启动时可恢复。

### 运行日志查看
**条件**: 用户在设置页点击“查看运行日志”。
**行为**: `LogActivity` 从 `AppData` 的进程内日志缓冲区读取最近日志，并展示主界面启动、ADB 建连、server 启动、数据通道建立、断开与自动重连等关键节点。
**结果**: 用户无需连接 Logcat，即可在应用内查看最近运行日志；日志仅保存在当前进程内存中，重启应用后会清空。

## 依赖关系

```yaml
依赖: easycontrol_server, repository_docs
被依赖: 无（仓库主入口模块）
```

## 近期实现快照（2026-03-12）

### scrcpy v3.3.4 同步后的 app 侧边界
- `ClientStream` 继续维持 `app_process -Djava.class.path=... top.saymzx.easycontrol.server.Server` 的启动形式，并保持发送 `listenClip` 参数键。
- `ControlPacket` 继续维持现有 1-9 控制协议格式，仅把协议常量与 main socket 事件常量显式化，便于与 server 侧对齐审计。
- `ClientPlayer` 已改为复用统一事件常量解析 main socket 音频/剪贴板/视频尺寸事件；`VideoDecode` 与 `AudioDecode` 经静态审计后无需同步改动。
- `ClientStream` / `Adb` 现已按“ADB 建连 / 调试授权 / server 连接 / 超时”阶段输出更明确的用户可见提示，不再直接展示 `java.lang.Exception` 文本。
- `ClientStream` 现会根据 `R.raw.easycontrol_server` 的内容 CRC 生成被控端 server 文件名；即使 app `versionCode` 不变，只要内嵌 server 载荷变化，下一次连接也会自动重新推送。
- 设置页在“查看本机IP”下方新增“查看运行日志”入口；`PublicTools` 会把关键运行日志同时写入 Logcat 与进程内缓冲，`LogActivity` 可直接查看最近启动/连接/断开日志。
- 控制通道后台 `keepAlive` 失败导致断开时，客户端现仅提示“连接断开”，不再把内部动作名 `keepAlive` 暴露给用户。
- 网络设备的“连接时操作”现新增“强制走 ADB 转发”开关；启用后会跳过 direct socket，直接通过 ADB forward 建立 main/video 双通道，便于规避部分无线局域网直连不稳定问题。
- `AppData` 现同时维护默认 ADB 密钥与软件独立密钥：默认密钥优先从 `SharedPreferences(adb_key)` 读取并兼容旧文件迁移；设备详情页新增“使用软件独立密钥”开关，可按设备切换连接所用密钥；设置页的“自定义密钥 / 重置密钥”则作用于软件独立密钥。

### 当前阻断
- 仓库内 JDK 17 + Android SDK 35 已补齐，且已在 AGP 8.6.1 + Gradle 8.7 + compileSdk/targetSdk 35 组合下验证 `./gradlew :app:assembleDebug` 可自动触发 `:server:copyDebug` 并完成联合构建；当前剩余阻断是真机播放/控制回归环境不足。
