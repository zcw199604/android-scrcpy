# easycontrol_app

## 职责

Android 主控端应用模块，负责设备列表管理、USB/网络 ADB 连接、客户端流/控制/播放组件协调、界面展示以及本地配置持久化。当前开源版代码已移除首次进入和 USB 入口的捐赠/激活门控。

## 接口定义

### 公共入口
| 函数/方法 | 参数 | 返回值 | 说明 |
|----------|------|--------|------|
| `MainActivity.onCreate(Bundle)` | `savedInstanceState` | `void` | 初始化 `AppData`、设备列表、广播监听与启动时自动连接逻辑，不再跳转激活页。 |
| `UsbActivity.onCreate(Bundle)` | `savedInstanceState` | `void` | USB 附加入口，直接启动主界面或发送 USB 更新广播。 |
| `Client.startDevice(Device)` | `device` | `void` | 创建并启动单个设备会话。 |
| `Client.sendAction(String, String, ByteBuffer, int)` | `uuid`, `action`, `byteBuffer`, `delay` | `void` | 向已建立的客户端会话发送控制动作。 |
| `AdbTools.connectADB(Device)` | `device` | `Adb` | 为 USB/网络设备建立或复用 ADB 连接。 |
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

## 依赖关系

```yaml
依赖: easycontrol_server, repository_docs
被依赖: 无（仓库主入口模块）
```
