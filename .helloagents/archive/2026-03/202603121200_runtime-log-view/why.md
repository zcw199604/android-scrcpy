# 变更提案: runtime-log-view

## 需求背景
当前 Android 主控端仅会把部分异常通过 `Toast` 或 `Logcat` 暴露给用户，缺少应用内可直接查看的运行日志界面。用户希望：
- 在设置页中新增一个日志查看入口；
- 把运行过程中的日志保存在内存中；
- 能在应用内直接查看这些日志；
- 新入口放在“查看本机IP”下面。

由于当前仓库没有集中日志查看能力，运行过程中一旦连接失败、自动重连或 server 输出异常，用户只能依赖瞬时 Toast 或外部 Logcat，排障成本较高。因此需要补充一个轻量、进程内的运行日志视图。

## 变更内容
1. 为 app 模块新增进程内运行日志缓冲区，统一记录关键运行日志。
2. 在现有日志输出工具中接入内存记录能力，确保现有错误日志与新增运行阶段日志都可保留。
3. 新增日志查看 Activity，用于展示当前进程内存中的日志内容。
4. 在设置页“查看本机IP”下方增加“查看运行日志”入口。
5. 更新知识库与变更记录，说明该能力的边界（仅内存保存，重启后清空）。

## 影响范围
- **模块:** `easycontrol_app`, `repository_docs`
- **文件:**
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/SetActivity.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/MainActivity.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/LogActivity.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/Client.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ClientStream.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/entity/AppData.java`
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/helper/PublicTools.java`
  - `easycontrol/app/src/main/res/layout/activity_log.xml`
  - `easycontrol/app/src/main/res/values/strings.xml`
  - `easycontrol/app/src/main/res/values-en/strings.xml`
  - `easycontrol/app/src/main/AndroidManifest.xml`
  - `.helloagents/modules/easycontrol_app.md`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`

## 验收标准
1. 设置页中出现“查看运行日志”入口，位置在“查看本机IP”下方。
2. 运行日志保存在进程内存中，应用不重启时可持续查看；重启后允许清空。
3. 连接启动、成功、失败、断开、自动重连等关键流程可在日志界面看到。
4. 现有 `PublicTools.logToast()` 记录的日志也会进入内存日志缓冲区。
5. `./gradlew :app:assembleDebug` 构建通过。
