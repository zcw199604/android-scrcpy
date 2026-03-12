# 变更日志

## [Unreleased]

### 新增
- **[knowledge-base]**: 初始化 HelloAGENTS 项目知识库（入口、上下文、模块索引、归档索引） — by zcw
  - 方案: 无（通过 `~init` 初始化）
  - 决策: 无


### 快速修改
- **[repository_docs]**: 修复 GitHub Actions 发布工作流的 YAML 语法错误，恢复 `android-release.yml` 可解析状态 — by zcw
  - 类型: 快速修改（无方案包）
  - 文件: .github/workflows/android-release.yml:196-213

## [1.6.1] - 2026-03-12

### 修复
- **[repository_docs]**: 修正 GitHub tag release APK 的签名与发布策略，避免继续公开发布不可安装或易误装的 APK 资产 — by zcw
  - 方案: [202603120032_fix-release-apk-signing](archive/2026-03/202603120032_fix-release-apk-signing/)
  - 决策: fix-release-apk-signing#D001(缺少签名配置时直接失败，而不是继续上传不可安装 APK)

## [1.6.0] - 2026-03-11

### 新增
- **[repository_docs]**: 新增 GitHub tag release 自动打包工作流，并同步 README / 知识库中的发布说明到当前仓库事实 — by zcw
  - 方案: [202603111449_github-tag-release-build](archive/2026-03/202603111449_github-tag-release-build/)
  - 决策: github-tag-release-build#D002(同时上传 workflow artifact 并同步 GitHub Release 资产)

## [1.5.9] - 2026-03-11

### 修复
- **[repository_docs]**: 清理过时的激活/构建说明，改为项目支持说明，并记录本地打包验证受 `JAVA_HOME` 缺失阻塞 — by zcw
  - 方案: [202603111413_cleanup-docs-build-validation](archive/2026-03/202603111413_cleanup-docs-build-validation/)
  - 决策: cleanup_docs_build_validation#D001(文档按当前代码事实与已验证构建结果更新)

## [1.5.8] - 2026-03-11

### 修复
- **[easycontrol_app]**: 移除首次进入与 USB 入口的捐赠/激活门控，并清理缺失的激活页面、相关资源与 cloud 构建残留 — by zcw
  - 方案: [202603111355_remove-activation-logic](archive/2026-03/202603111355_remove-activation-logic/)
  - 决策: remove_activation_logic#D001(直接删除激活模块并开放入口)

## 基线版本
- `easycontrol/app`: `versionCode 10507` / `versionName 1.5.7`
- `easycontrol/server`: `versionCode 20000` / `versionName 2.0.0`
- 当前知识库已累计归档 5 个 HelloAGENTS 方案包（见 `archive/_index.md`）。
