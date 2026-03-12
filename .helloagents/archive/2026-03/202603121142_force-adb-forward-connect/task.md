# 任务清单: force-adb-forward-connect

目录: `.helloagents/plan/202603121142_force-adb-forward-connect/`

- [√] 1. 在 `Device` / `DbHelper` 中新增“网络设备强制走 ADB forward”配置字段并完成持久化
- [√] 2. 在 `DeviceDetailActivity` 的“连接时操作”中新增对应开关与文案
- [√] 3. 在 `ClientStream` 中接入该配置：网络设备启用时跳过 direct socket，直接走 ADB forward
- [√] 4. 更新知识库并执行 `./gradlew :app:assembleDebug` 验证
