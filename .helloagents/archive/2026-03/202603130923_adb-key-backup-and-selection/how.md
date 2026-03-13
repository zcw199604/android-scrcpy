# 技术设计: adb-key-backup-and-selection

## 目标
在不改变 ADB 连接主流程的前提下，明确区分“默认密钥”和“软件独立密钥”的生命周期，并把设备级密钥选择纳入现有持久化配置。

## 方案概述
### 方案A（采用）: 默认密钥走 SharedPreferences 持久化，软件独立密钥继续走文件，并新增设备级开关
- `PublicTools.readDefaultKeyPair()`：
  - 先从 `SharedPreferences(adb_key)` 读取默认公私钥；
  - 若不存在则兼容旧版 `public.key/private.key` 文件；
  - 若文件存在则迁移保存到 `SharedPreferences`；
  - 若仍不存在则生成新密钥并同步写入文件与 `SharedPreferences`。
- `PublicTools.readAdbKeyPair()`：继续读取 app 私有文件中的软件独立密钥。
- `AppData`：新增 `appKeyPair`，启动时同时加载默认密钥与软件独立密钥。
- `Device` / `DbHelper`：新增 `useAppKey` 字段与数据库版本升级。
- `AdbTools.connectADB()`：按 `useAppKey` 选择 `AppData.keyPair` 或 `AppData.appKeyPair`。
- `DeviceDetailActivity` / `SetActivity` / `AdbKeyActivity`：新增设备级开关，并让自定义/重置操作只影响软件独立密钥。
- `AndroidManifest.xml`：开启 `allowBackup`，为默认密钥的系统备份恢复提供前提。

**优点:**
- 不破坏现有 ADB 连接主流程。
- 兼容旧版文件密钥并在首次读取时完成迁移。
- 允许单个设备单独切换到软件独立密钥，降低授权耦合。

**缺点:**
- 默认密钥能否真正随重装恢复，仍依赖 Android 设备是否启用并支持系统备份。
- 需要一次数据库版本升级来持久化新开关。

### 方案B（未采用）: 所有设备统一改用软件独立密钥
**拒绝原因:** 会导致所有既有授权关系重新建立，兼容性与迁移成本较高。

## 风险与规避
- **风险:** 老用户升级后默认密钥丢失。
  - **规避:** 读取顺序优先兼容旧文件，并在首次成功读取后回写到 `SharedPreferences`。
- **风险:** 设备级开关没有持久化，导致连接行为不可预期。
  - **规避:** 通过 `DbHelper` 升级数据库版本并新增 `useAppKey` 字段。
- **风险:** 用户误以为两套密钥都会被系统恢复。
  - **规避:** UI 文案明确“使用软件独立密钥”在重装后仍需要重新授权。

## 验证计划
1. 执行 `./gradlew :app:assembleDebug`，验证 app 侧编译通过。
2. 静态检查 `AdbTools`：确认连接时根据 `useAppKey` 选择不同密钥。
3. 静态检查 `DbHelper` / `DeviceDetailActivity`：确认设备级开关已入库并有 UI 入口。
