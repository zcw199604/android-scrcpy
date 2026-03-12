# 任务清单: fix-release-apk-signing

> **@status:** completed | 2026-03-12 00:41

```yaml
@feature: fix-release-apk-signing
@created: 2026-03-12
@status: completed
@mode: R2
```

<!-- LIVE_STATUS_BEGIN -->
状态: completed | 进度: 5/5 (100%) | 更新: 2026-03-12 00:40:51
当前: 修复已完成，待归档
<!-- LIVE_STATUS_END -->

## 进度概览

| 完成 | 失败 | 跳过 | 总数 |
|------|------|------|------|
| 5 | 0 | 0 | 5 |

---

## 任务列表

### 1. 构建链路修复

- [√] 1.1 更新 `easycontrol/app/build.gradle`，为 release 构建增加签名参数读取与缺失校验 | depends_on: []
- [√] 1.2 更新 `easycontrol/server/build.gradle`，让 `copyRelease` 兼容 signed/unsigned 输出文件名 | depends_on: [1.1]

### 2. Workflow 与说明同步

- [√] 2.1 更新 `.github/workflows/android-release.yml`，在 CI 中准备签名配置，并只把 app APK 发布到 GitHub Release | depends_on: [1.2]
- [√] 2.2 更新 `README.md`，补充 GitHub Release 所需签名 secrets 与公开发布资产说明 | depends_on: [2.1]
- [√] 2.3 同步 `.helloagents/context.md` 与 `.helloagents/modules/repository_docs.md`，反映新的 release 约束与资产发布策略 [降级执行] | depends_on: [2.2]

---

## 执行日志

| 时间 | 任务 | 状态 | 备注 |
|------|------|------|------|
| 2026-03-12 00:40:51 | 1.1 | completed | app release 构建已支持从 Gradle 属性/环境变量读取签名配置，并在缺失时抛出明确错误 |
| 2026-03-12 00:40:51 | 1.2 | completed | server `copyRelease` 已改为兼容 `server-release*.apk`，不再硬编码未签名文件名 |
| 2026-03-12 00:40:51 | 2.1 | completed | workflow 已新增签名准备步骤；GitHub Release 仅公开发布 app APK，server APK 仅保留在 workflow artifact |
| 2026-03-12 00:40:51 | 2.2 | completed | README 已补充 GitHub Secrets 要求与公开发布资产说明 |
| 2026-03-12 00:40:51 | 2.3 | completed | 知识库已同步新的 release 约束；文档 worker 已尝试执行，主代理完成最终合并 |

---

## 执行备注

> 记录执行过程中的重要说明、决策变更、风险提示等
- 当前问题已明确发生在“安装 GitHub Release APK”阶段，而不是 server 运行时或应用内安装链路。
- 根因判断基于仓库事实与 Android 官方要求：`release` 没有签名配置，workflow 却直接发布 release APK；Android 安装前必须先完成签名。
- 当前本地环境仍缺少 Java / Android SDK，因此本次仅完成 Gradle/YAML 静态检查，未做真实 APK 构建复验。
