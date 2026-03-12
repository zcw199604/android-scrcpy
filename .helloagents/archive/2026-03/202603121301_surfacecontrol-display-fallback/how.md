# 技术设计: surfacecontrol-display-fallback

## 目标
修复被控端 server 在部分 Android 15 / 厂商 ROM 上因 `SurfaceControl.createDisplay(String, boolean)` 缺失而导致的启动崩溃，同时保留旧设备上的现有兼容路径。

## 方案概述
### 方案A（采用）: `DisplayManager` 优先，`SurfaceControl` 回退
- 在 `VideoEncode.startEncode()` 中按如下顺序创建显示输出：
  1. 优先尝试 `DisplayManager.createVirtualDisplay(name, width, height, displayIdToMirror, surface)`；
  2. 若失败，则回退到当前 `SurfaceControl.createDisplay(...) + setDisplaySurface(...)` 路径；
  3. 若两者都失败，则抛出最终异常，并保留前一个失败原因作为 suppressed 信息。
- 通过 `System.out.println(...)` 输出当前采用的显示 API，便于主控端回收 server 日志。
- 在每次重配编码器时重建对应显示对象，避免旧 `Surface` 与旧显示对象残留。

**优点:**
- 与当前 scrcpy 的新显示兼容思路一致。
- 对现有协议、socket 和 app 侧逻辑无侵入。
- 风险集中在 `VideoEncode` 单点，便于验证。

**缺点:**
- 未接真机时无法覆盖所有厂商 ROM 分支，只能通过静态推断与构建验证。

## 关键设计
### 1. 显示输出对象双路径管理
- `VideoEncode` 同时维护：
  - `IBinder display`：旧 `SurfaceControl` 路径
  - `VirtualDisplay virtualDisplay`：新 `DisplayManager` 路径
- 统一提供释放逻辑，确保每次重配或退出时清理干净。

### 2. API 选择策略
- **优先:** `DisplayManager`
- **回退:** `SurfaceControl`
- **日志:**
  - `Display: using DisplayManager API`
  - `Display: DisplayManager API unavailable, fallback to SurfaceControl`
  - `Display: using SurfaceControl API`

### 3. 风险与规避
- **风险:** `DisplayManager` 路径同样在某些 ROM 上不可用
  - **规避:** 自动回退到 `SurfaceControl`
- **风险:** 编码器重启后旧显示对象未释放
  - **规避:** 在 `startEncode()` 前统一释放旧显示对象
- **风险:** 真机未覆盖导致仍有 ROM 特例
  - **规避:** 增加显示 API 选择日志，便于后续二次定位

## 验证计划
1. 执行 `./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug`。
2. 静态检查 `VideoEncode`：确认优先 `DisplayManager`、失败回退 `SurfaceControl`。
3. 静态检查日志：确认 server 输出能区分最终显示路径。
