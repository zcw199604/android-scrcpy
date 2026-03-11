# 变更提案: github-tag-release-build

## 元信息
```yaml
类型: 新功能
方案类型: implementation
优先级: P1
状态: 待实施
创建: 2026-03-11
负责人: pkg_keeper[降级执行]
模式: R2
关联任务包: 202603111449_github-tag-release-build
```

---

## 1. 需求

### 背景
当前仓库还没有 `.github/workflows/`，也没有可复用的 CI / Release 打包流程。与此同时，`easycontrol` 的 Android 构建链路并不是单纯执行 `:app:assembleRelease`：`app` 运行时依赖 `R.raw.easycontrol_server`，而该资源需要先通过 `:server:copyRelease` 从 `server` 模块生成并复制到 `app/src/main/res/raw/easycontrol_server.jar`。用户希望在 GitHub 上补一个“tag release 打包”工作流，把这条真实构建链路固化下来，减少手工打包和漏步骤风险。

### 目标
- 新增一个 GitHub Actions 工作流，在推送 tag 时自动执行 Android release 打包。
- 将真实构建顺序固化为先 `:server:copyRelease`、再 `:app:assembleRelease`。
- 在工作流中产出可下载的 APK 产物，并同步到当前 tag 对应的 GitHub Release。
- 适度同步 README 与知识库，避免仓库仍显示“没有 CI / Release 自动化”的过时信息。

### 约束条件
```yaml
时间约束: 本次仅补最小可用的 tag release 打包链路，不扩展到 PR 校验、单元测试矩阵或自动签名发布
性能约束: 复用现有 Gradle Wrapper 与 GitHub 缓存能力，不额外引入复杂的多 Job 拆分
兼容性约束: 不改动 app/server 业务代码；仅在仓库层新增 workflow 与说明文档
业务约束: workflow 必须反映当前源码的真实构建链路；tag 触发后应能在 GitHub Release 中看到 APK 资产
```

### 验收标准
- [ ] 仓库新增 `.github/workflows/android-release.yml`，并在 push tag 时触发执行。
- [ ] workflow 使用 JDK 17 与 Gradle Wrapper，执行 `bash ./gradlew :server:copyRelease :app:assembleRelease`。
- [ ] workflow 会收集 `app` 与 `server` 的 release APK，既上传为 workflow artifact，也发布到当前 tag 对应的 GitHub Release。
- [ ] `README.md` 与知识库中不再保留“当前无 CI / 无 tag release 打包”的过时表述。
- [ ] 至少完成一次本地静态验证（如 YAML 语法检查、关键命令/路径检查、`git diff --check`），并记录无法在当前环境完成的部分。

---

## 2. 方案

### 技术方案
本次采用“单工作流 + 单 Job”的最小可用发布方案，保持实现直接、可维护：

1. **触发策略**
   - 新增 `.github/workflows/android-release.yml`。
   - 默认监听 `push.tags: ['*']`，覆盖当前仓库尚未约定前缀规范的 tag 发布方式。
   - 额外提供 `workflow_dispatch`，允许维护者手工指定 tag 重新构建已有 release。
2. **构建环境**
   - 运行环境固定为 `ubuntu-24.04`，避免 `ubuntu-latest` 漂移。
   - 使用 `actions/checkout`、`actions/setup-java`、`gradle/actions/setup-gradle` 配置源码、JDK 17 与 Gradle 缓存。
3. **构建链路固化**
   - 在 `easycontrol` 目录中执行 `bash ./gradlew :server:copyRelease :app:assembleRelease`。
   - 让 `server` 先生成并复制 `easycontrol_server.jar`，再打包 `app` 的 release APK，符合当前源码的真实依赖关系。
4. **产物与发布**
   - 收集 `easycontrol/app/build/outputs/apk/release/*.apk` 与 `easycontrol/server/build/outputs/apk/release/*.apk`。
   - 将产物复制到 `dist/`，按 tag 重命名后上传为 workflow artifact。
   - 使用 GitHub CLI 的 `gh release create` / `gh release upload --clobber` 创建或更新当前 tag 对应的 GitHub Release 资产。
5. **文档与知识同步**
   - 在 `README.md` 中补充 GitHub tag release 自动打包说明。
   - 更新 `.helloagents/context.md`、`.helloagents/modules/repository_docs.md`，反映仓库已具备最小 GitHub Release 打包能力。

### 影响范围
```yaml
涉及模块:
  - .github/workflows/android-release.yml: 新增 tag release 自动打包与 GitHub Release 发布工作流
  - README.md: 补充 GitHub tag release 自动打包说明
  - .helloagents/context.md: 更新项目约束与技术债务，移除“当前无 CI”的过时结论
  - .helloagents/modules/repository_docs.md: 记录仓库级 release workflow 与文档入口行为
  - .helloagents/CHANGELOG.md: 记录新增 GitHub Release 打包能力
预计变更文件: 5-7
```

### 风险评估
| 风险 | 等级 | 应对 |
|------|------|------|
| GitHub Runner 镜像升级导致 `ubuntu-latest` 或预装 Android 组件行为漂移 | 中 | 固定 `ubuntu-24.04`，并保持最小依赖链路，减少环境不确定性 |
| 仅 `:app:assembleRelease` 无法产出可运行资源，导致 Release APK 缺少内嵌 server | 高 | 明确先执行 `:server:copyRelease` 再执行 `:app:assembleRelease` |
| Release 已存在时重复上传资产失败 | 中 | 使用 `gh release view` 判断存在性，已存在则走 `gh release upload --clobber` |
| 当前本地环境缺少 Java / Android SDK，无法做真实构建验收 | 中 | 在本地完成 YAML/路径/命令静态检查，并在报告中明确真实打包需在 GitHub Runner 上复验 |

---

## 4. 核心场景

### 场景: 维护者推送发布 tag
**模块**: repository_docs
**条件**: 仓库已存在 `.github/workflows/android-release.yml`，维护者向 GitHub 仓库推送任意 tag。
**行为**: GitHub Actions 自动检出对应 tag，配置 JDK 17 与 Gradle，执行 `:server:copyRelease` 和 `:app:assembleRelease`，随后上传 APK 并创建/更新该 tag 的 GitHub Release。
**结果**: 维护者不需要手工登录 Runner 打包，tag 对应的 Release 页面会附带 APK 资产。

### 场景: 维护者手工重建某个 tag 的 release 资产
**模块**: repository_docs
**条件**: 维护者在 Actions 页面手工触发 workflow，并填写目标 tag。
**行为**: workflow 检出指定 tag，对已有 Release 重新上传 APK 产物；若 Release 尚不存在则自动创建。
**结果**: 某个历史 tag 的 release 资产可以被补发或覆盖，无需改代码重打 tag。

### 场景: 用户阅读仓库构建/发布说明
**模块**: repository_docs
**条件**: 用户阅读 README 或知识库，想知道当前仓库是否具备自动打包能力。
**行为**: 文档明确说明当前 GitHub 仓库支持 tag 触发的 release 打包，构建链路仍遵循 `server -> app` 的顺序。
**结果**: 用户不会再把仓库误判为“只能本地手工打包、没有任何 CI / release 自动化”。

---

## 5. 技术决策

> 本方案涉及的技术决策，归档后成为决策的唯一完整记录

### github-tag-release-build#D001: 默认对任意 tag 触发 release 打包
**日期**: 2026-03-11
**状态**: ✅采纳
**背景**: 用户只明确提出“tag release 打包”，当前仓库又没有既定的 tag 命名约定，也尚未存在历史 tag。
**选项分析**:
| 选项 | 优点 | 缺点 |
|------|------|------|
| A: 仅匹配 `v*` 前缀 tag | 能减少误触发，更贴近常见语义化版本风格 | 需要维护者先接受 `v` 前缀约定；若直接推送 `1.5.8` 之类 tag 会静默不触发 |
| B: 匹配任意 tag | 与用户“tag release 打包”的直接诉求一致，不依赖额外命名约定 | 维护者推送实验性 tag 时也会触发 workflow |
**决策**: 选择方案 B
**理由**: 在仓库尚无 tag 规范的前提下，优先避免“打了 tag 却没触发”的隐性失败；后续若团队形成 `v*` 规范，可再局部收紧触发条件。
**影响**: `.github/workflows/android-release.yml`、README 中对触发方式的说明

### github-tag-release-build#D002: 用 GitHub Release 资产承载 APK，而不是只保留 workflow artifact
**日期**: 2026-03-11
**状态**: ✅采纳
**背景**: 单纯上传 workflow artifact 只方便维护者在 Actions 页面下载，不利于以 tag 为中心沉淀公开发布记录。
**选项分析**:
| 选项 | 优点 | 缺点 |
|------|------|------|
| A: 仅上传 workflow artifact | 实现简单，便于临时下载 | 用户必须进入 Actions 才能取包，tag 与产物的发布关系不直观 |
| B: 同时上传 workflow artifact 并同步到 GitHub Release | 既保留构建日志下载入口，也让 tag 页面直接带 APK 产物 | 需要额外处理 Release 存在与资产覆盖逻辑 |
**决策**: 选择方案 B
**理由**: 用户明确提到“tag release 打包”，tag 对应 Release 页面天然就是更合适的交付载体；保留 workflow artifact 则有助于调试和回溯。
**影响**: `.github/workflows/android-release.yml`、README 发布说明、知识库仓库文档

### github-tag-release-build#D003: 固化 release 构建顺序为 `:server:copyRelease` → `:app:assembleRelease`
**日期**: 2026-03-11
**状态**: ✅采纳
**背景**: 当前 `app` 并没有直接依赖 `server` 模块的 Gradle 关系，`server` APK 需要先复制到 `app/src/main/res/raw/easycontrol_server.jar` 后，主控端打包才完整。
**选项分析**:
| 选项 | 优点 | 缺点 |
|------|------|------|
| A: 仅执行 `:app:assembleRelease` | workflow 更短 | 无法保证 `R.raw.easycontrol_server` 对应资源已由最新 server 构建生成 |
| B: 先执行 `:server:copyRelease`，再执行 `:app:assembleRelease` | 与当前源码依赖关系一致，可保证 app 内嵌的 server 载荷来自当前 tag 的 release 构建 | 比单任务打包多一步，构建时间略长 |
**决策**: 选择方案 B
**理由**: 这是当前仓库真实且必要的构建链路，不能为了简化 workflow 而省略。
**影响**: `.github/workflows/android-release.yml`、README 构建说明、知识库中的构建约束描述
