# 变更提案: server-jar-sync-fingerprint

## 需求背景
用户在修复 `SurfaceControl.createDisplay(String, boolean)` 兼容问题后再次真机验证，日志仍然显示同一个旧异常，并且没有看到新加入的 `Display: ...` server 输出。进一步检查后确认：
- `ClientStream.startServer()` 当前仅使用 `BuildConfig.VERSION_CODE` 生成被控端 server 文件名；
- release 构建的 `ENABLE_DEBUG_FEATURE=false`，当 `/data/local/tmp/easycontrol_server_10507.jar` 已存在时，不会重新推送 server；
- 当前多个 tag 发布并未更新 app 的 `versionCode`，因此即使 `R.raw.easycontrol_server` 内容变了，被控端仍会继续执行旧 jar。

所以本次“还是同一个错误”的根因不是显示兼容修复无效，而是**新 server 根本没有下发到被控端**。

## 变更内容
1. 调整 `ClientStream` 的被控端 server 文件命名策略，从“仅 versionCode”改为“versionCode + server 载荷指纹”。
2. 让 release 包在 `R.raw.easycontrol_server` 内容变化时自动生成新的远端文件名，触发重新推送。
3. 补充运行日志，明确区分“正在同步被控端服务文件”与“被控端服务文件已是最新版本”。
4. 更新知识库与变更记录，说明该机制用于避免同版本号下的 server 载荷复用问题。

## 影响范围
- **模块:** `easycontrol_app`, `repository_docs`
- **文件:**
  - `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/client/tools/ClientStream.java`
  - `.helloagents/context.md`
  - `.helloagents/modules/easycontrol_app.md`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`

## 验收标准
1. 当 `R.raw.easycontrol_server` 内容变化但 `versionCode` 不变时，客户端仍会向被控端重新推送新的 server。
2. 运行日志可明确显示“同步”或“已是最新版本”。
3. `./gradlew :app:assembleDebug` 构建通过。
4. 新构建安装后，用户再次连接时应能看到新的 server 日志，而不是继续静默复用旧 jar。
