# 任务清单: surfacecontrol-display-fallback

目录: `.helloagents/plan/202603121301_surfacecontrol-display-fallback/`

- [√] 1. 调整 `VideoEncode` 显示创建逻辑，优先尝试 `DisplayManager` 并在失败时回退 `SurfaceControl`
- [√] 2. 补充 server 显示 API 选择日志，并确保重配/释放路径正确
- [√] 3. 更新知识库与变更记录
- [√] 4. 执行 `./gradlew :server:assembleDebug :server:copyDebug :app:assembleDebug` 验证
