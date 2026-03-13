# 变更提案: adb-key-backup-and-selection

## 需求背景
当前 app 的 ADB 认证仅依赖 `files/` 目录下的一对密钥文件。这样会带来两个问题：
1. 默认连接密钥与用户手动维护的“软件独立密钥”没有被明确区分；
2. 仅依赖文件保存时，重装或迁移后更容易丢失默认授权状态，用户需要重新在被控端确认 ADB 授权。

本次改动的目标，是在保持现有连接逻辑兼容的前提下：
- 为 app 增加“默认密钥”和“软件独立密钥”两套明确的读取路径；
- 允许设备级别选择是否使用软件独立密钥；
- 为默认密钥提供更适合系统备份恢复的持久化载体。

## 变更内容
1. `AppData` 拆分 `keyPair` / `appKeyPair`：默认密钥改为优先从 `SharedPreferences` 读取，并兼容旧文件迁移；软件独立密钥继续从 app 私有文件读取。
2. `Device` / `DbHelper` 新增 `useAppKey` 字段与数据库迁移，用于按设备控制连接时选择哪一把 ADB 密钥。
3. 设备详情页新增“使用软件独立密钥”开关；设置页的“自定义密钥 / 重置密钥”改为作用于软件独立密钥。
4. `AndroidManifest.xml` 开启 `android:allowBackup="true"`，配合默认密钥保存到 `SharedPreferences`，为系统备份/恢复场景提供前提条件。

## 影响范围
- **模块:** `easycontrol_app`, `repository_docs`
- **文件:**
  - `easycontrol/app/src/main/AndroidManifest.xml`
  - `easycontrol/app/src/main/java/top/zcw/control/app/AdbKeyActivity.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/DeviceDetailActivity.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/SetActivity.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/client/tools/AdbTools.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/entity/AppData.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/entity/Device.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/helper/DbHelper.java`
  - `easycontrol/app/src/main/java/top/zcw/control/app/helper/PublicTools.java`
  - `easycontrol/app/src/main/res/values/strings.xml`
  - `easycontrol/app/src/main/res/values-en/strings.xml`
  - `.helloagents/context.md`
  - `.helloagents/modules/easycontrol_app.md`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`

## 验收标准
1. 连接设备时，可根据 `Device.useAppKey` 选择默认密钥或软件独立密钥。
2. 默认密钥可优先从 `SharedPreferences` 读取，并兼容旧版文件迁移。
3. 设置页/设备详情页提供对应 UI 入口，数据库能持久化 `useAppKey`。
4. `./gradlew :app:assembleDebug` 构建通过。
