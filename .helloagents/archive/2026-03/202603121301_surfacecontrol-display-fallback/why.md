# 变更提案: surfacecontrol-display-fallback

## 需求背景
用户在真机运行时复现了被控端 server 连接后立即断开的故障。应用内运行日志显示：
- ADB forward 与数据通道已经建立；
- 随后 server 抛出 `java.lang.NoSuchMethodException: android.view.SurfaceControl.createDisplay(String, boolean)`；
- 最终导致视频通道中断，客户端出现 `BufferNew error` 与“连接断开”。

这说明当前 server 在该设备/ROM 上无法继续使用旧的 `SurfaceControl.createDisplay(String, boolean)` 反射签名创建显示输出，属于 Android 新版本/厂商 ROM 的显示兼容问题，而不是网络或 ADB 建连问题。

## 变更内容
1. 调整 `VideoEncode` 的显示创建流程，优先尝试较新的 `DisplayManager` 显示 API。
2. 当 `DisplayManager` API 不可用时，回退到现有 `SurfaceControl` 路径，保持旧设备兼容。
3. 在 server 侧补充显示 API 选择日志，便于后续在应用内日志中快速定位是走 `DisplayManager` 还是 `SurfaceControl`。
4. 更新知识库与变更记录，说明本次兼容修复的边界与验证方式。

## 影响范围
- **模块:** `easycontrol_server`, `repository_docs`
- **文件:**
  - `easycontrol/server/src/main/java/top/saymzx/easycontrol/server/helper/VideoEncode.java`
  - `.helloagents/modules/easycontrol_server.md`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`

## 验收标准
1. server 在不支持 `SurfaceControl.createDisplay(String, boolean)` 的设备上，不再直接因该方法缺失崩溃。
2. 优先使用 `DisplayManager` 显示 API；若失败，则自动回退到 `SurfaceControl`。
3. server 输出中能看出最终使用的显示 API。
4. `./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug` 构建通过。
