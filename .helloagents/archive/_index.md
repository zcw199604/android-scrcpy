# 方案归档索引

> 通过此文件快速查找历史方案。

## 快速索引（当前年份）

| 时间戳 | 名称 | 类型 | 涉及模块 | 决策 | 结果 |
|--------|------|------|---------|------|------|
| 202603131102 | version-v101 | implementation | easycontrol_app, repository_docs | version-v101#D001 | ✅完成 |
| 202603130923 | adb-key-backup-and-selection | implementation | easycontrol_app, repository_docs | adb-key-backup-and-selection#D001 | ✅完成 |
| 202603130854 | sdk-35-upgrade | implementation | easycontrol_app, easycontrol_server, repository_docs | sdk-35-upgrade#D001 | ✅完成 |
| 202603121316 | server-jar-sync-fingerprint | implementation | easycontrol_app, repository_docs | server-jar-sync-fingerprint#D001 | ✅完成 |
| 202603121301 | surfacecontrol-display-fallback | implementation | easycontrol_server, repository_docs | surfacecontrol-display-fallback#D001 | ✅完成 |
| 202603121200 | runtime-log-view | implementation | easycontrol_app, repository_docs | runtime-log-view#D001 | ✅完成 |
| 202603121142 | force-adb-forward-connect | implementation | easycontrol_app, repository_docs | force-adb-forward-connect#D001 | ✅完成 |
| 202603120724 | scrcpy-v334-official-sync | implementation | easycontrol_server, easycontrol_app, repository_docs | scrcpy-v334-official-sync#D001 | ⚠️部分完成 |
| 202603120206 | publish-v0-2-tag | implementation | repository_docs | publish-v0-2-tag#D001 | ✅完成 |
| 202603120131 | commit-release-signing-fix | implementation | repository_docs | commit-release-signing-fix#D001 | ✅完成 |
| 202603120032 | fix-release-apk-signing | implementation | repository_docs | fix-release-apk-signing#D001 | ✅完成 |
| 202603111523 | publish-v0-1-tag | implementation | repository_docs | publish-v0.1-tag#D001 | ✅完成 |
| 202603111449 | github-tag-release-build | implementation | repository_docs | github-tag-release-build#D002 | ✅完成 |
| 202603111413 | cleanup-docs-build-validation | implementation | repository_docs | cleanup_docs_build_validation#D001 | ✅完成 |
| 202603111355 | remove-activation-logic | implementation | easycontrol_app, cloud | remove_activation_logic#D001 | ✅完成 |

## 按月归档

### 2026-03
- [202603131102_version-v101](./2026-03/202603131102_version-v101/) - 将 app 安装版本提升到 1.0.1，并创建发布 tag `v1.0.1`
- [202603130923_adb-key-backup-and-selection](./2026-03/202603130923_adb-key-backup-and-selection/) - 为默认 ADB 密钥补充 SharedPreferences 持久化与设备级“使用软件独立密钥”开关
- [202603130854_sdk-35-upgrade](./2026-03/202603130854_sdk-35-upgrade/) - 升级到 AGP 8.6.1 + Gradle 8.7，并将 app/server 的 compileSdk/targetSdk 提升到 35
- [202603121316_server-jar-sync-fingerprint](./2026-03/202603121316_server-jar-sync-fingerprint/) - 修复同 versionCode 下 release 包继续复用旧被控端 server.jar，改为按 server 载荷 CRC 自动重推
- [202603121301_surfacecontrol-display-fallback](./2026-03/202603121301_surfacecontrol-display-fallback/) - 修复部分 Android 15 / 厂商 ROM 上 `SurfaceControl.createDisplay(String, boolean)` 缺失导致的 server 启动后断开
- [202603121200_runtime-log-view](./2026-03/202603121200_runtime-log-view/) - 新增应用内运行日志查看界面，并把关键运行日志保存在进程内存中
- [202603121142_force-adb-forward-connect](./2026-03/202603121142_force-adb-forward-connect/) - 为网络设备新增“强制走 ADB forward”连接时配置项，跳过 direct socket 直连
- [202603120724_scrcpy-v334-official-sync](./2026-03/202603120724_scrcpy-v334-official-sync/) - 同步 scrcpy v3.3.4 兼容层与构建链路，保留 Easycontrol 现有 app_process / 双 socket / ControlPacket / raw 打包契约
- [202603120206_publish-v0-2-tag](./2026-03/202603120206_publish-v0-2-tag/) - 将归档补充提交推送到 origin/main，并创建发布标签 v0.2
- [202603120131_commit-release-signing-fix](./2026-03/202603120131_commit-release-signing-fix/) - 将当前未提交的 release 签名修复与归档内容一次性创建为本地提交
- [202603120032_fix-release-apk-signing](./2026-03/202603120032_fix-release-apk-signing/) - 修正 GitHub tag release APK 的签名与发布策略，避免继续公开发布不可安装或易误装的 APK 资产
- [202603111523_publish-v0-1-tag](./2026-03/202603111523_publish-v0-1-tag/) - 将当前工作区改动提交推送到 origin/main，并创建发布标签 v0.1
- [202603111449_github-tag-release-build](./2026-03/202603111449_github-tag-release-build/) - 新增 GitHub tag release 自动打包工作流，并同步 README / 知识库中的发布说明
- [202603111413_cleanup-docs-build-validation](./2026-03/202603111413_cleanup-docs-build-validation/) - 清理过时激活/构建说明，并记录本地打包验证阻塞点
- [202603111355_remove-activation-logic](./2026-03/202603111355_remove-activation-logic/) - 移除首次进入与 USB 入口的捐赠/激活门控，并清理激活残留

## 结果状态说明
- ✅ 完成
- ⚠️ 部分完成
- ❌ 失败/中止
- ⏸ 未执行
- 🔄 已回滚
- 📄 概述
