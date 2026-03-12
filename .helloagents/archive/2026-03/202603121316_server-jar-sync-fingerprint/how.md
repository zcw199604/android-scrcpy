# 技术设计: server-jar-sync-fingerprint

## 目标
确保 app 内嵌 server 载荷一旦变化，即使 app `versionCode` 没变，被控端也能自动收到新的 jar，而不是继续执行旧缓存文件。

## 方案概述
### 方案A（采用）: 远端 server 文件名加入载荷 CRC 指纹
- 在 `ClientStream` 中对 `R.raw.easycontrol_server` 计算 CRC32。
- 把远端文件名从：
  - `easycontrol_server_<versionCode>.jar`
- 改为：
  - `easycontrol_server_<versionCode>_<crc>.jar`
- 连接时检查远端是否存在该完整文件名：
  - 不存在 → 删除旧 `easycontrol_*` 并重新推送
  - 存在 → 记录“已是最新版本”并复用

**优点:**
- 不依赖人为维护 `versionCode` 递增。
- 只有载荷真正变化时才重新推送，避免每次连接都上传。
- 对现有 server 启动命令与协议无侵入。

**缺点:**
- 首次连接需要额外计算一次 CRC32，但成本很低。

### 方案B（未采用）: 每次连接都强制删除并重传 server
**拒绝原因:** 虽然简单，但每次都上传 APK 载荷会增加连接耗时，且没有必要。

## 关键设计
### 1. 指纹生成
- 输入: `R.raw.easycontrol_server`
- 算法: CRC32
- 输出: `versionCode_crcHex`
- 缓存: 进程内缓存一次，避免重复计算

### 2. 同步策略
- `debug` 仍维持“总是同步”的既有行为
- `release` 改为按指纹判断是否需要同步
- 新增日志：
  - `正在同步被控端服务文件`
  - `被控端服务文件已是最新版本`

## 风险与规避
- **风险:** CRC 计算失败
  - **规避:** 回退到旧的 `versionCode` 命名策略，不阻断连接
- **风险:** 远端残留多个旧文件
  - **规避:** 在需要同步时继续清理 `easycontrol_*`
- **风险:** 用户当前设备已缓存旧 jar
  - **规避:** 新文件名变化后，下次连接会自动重新推送

## 验证计划
1. 执行 `./gradlew :app:assembleDebug` 验证编译通过。
2. 静态检查 `ClientStream`：确认文件名基于 `versionCode + crc`。
3. 静态检查日志：确认存在“同步中”与“已是最新版本”两条分支日志。
