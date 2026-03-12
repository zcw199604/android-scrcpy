# 变更提案: fix-release-apk-signing

## 元信息
```yaml
类型: 修复
方案类型: implementation
优先级: P0
状态: 待实施
创建: 2026-03-12
负责人: pkg_keeper[降级执行]
模式: R2
关联任务包: 202603120032_fix-release-apk-signing
```

---

## 1. 需求

### 背景
用户反馈从当前 tag release 产物安装 APK 时，手机提示 `packageinfo is null`。结合仓库现状可确认：`easycontrol/app/build.gradle` 的 `release` 构建没有任何 `signingConfig`；当前 `.github/workflows/android-release.yml` 又直接执行 `:server:copyRelease` 和 `:app:assembleRelease` 并把 release APK 上传到 GitHub Release。Android 官方要求所有 APK 在安装前必须先签名，因此当前 workflow 上传的 release APK 大概率属于未签名产物，用户直接安装时会在系统安装器侧失败。此外，workflow 还把 `server` APK 作为公开 Release 资产上传，容易让用户误装并进一步混淆问题。

### 目标
- 修复 GitHub release 产物“下载后无法直接安装”的主要原因。
- 为 `app` 模块补上可配置的 release 签名能力，并在缺少签名配置时明确失败，而不是继续发布不可安装 APK。
- 调整 workflow，只把面向用户的主控端 APK 发布到 GitHub Release；`server` 产物仅保留为内部构建产物。
- 同步 README 与知识库，明确 release 打包依赖签名 secrets。

### 约束条件
```yaml
时间约束: 本次只修复 release APK 的签名与发布链路，不扩展到应用内安装逻辑或设备端运行时问题
安全约束: 不把 keystore 或密码提交到仓库；签名信息只能来自本地环境变量或 GitHub Secrets
兼容性约束: 保留现有 debug 构建路径；不影响 `:server:copyDebug` / `:app:assembleDebug`
业务约束: GitHub Release 页面只发布用户应该安装的主控端 APK，避免继续暴露容易误装的 server APK
```

### 验收标准
- [ ] `app` 的 release 构建支持从环境变量/Gradle 属性读取签名配置，缺失配置时给出明确错误。
- [ ] workflow 在构建前准备 keystore 并注入签名参数，继续执行 `:server:copyRelease` → `:app:assembleRelease`。
- [ ] GitHub Release 只发布主控端 app APK，不再公开发布 server APK。
- [ ] README 与知识库已补充“release 依赖签名 secrets”的说明。
- [ ] 至少完成静态验证：Gradle 配置检查、YAML 解析/关键字段检查、`git diff --check`。

---

## 2. 方案

### 技术方案
1. **App release 签名改造**
   - 在 `easycontrol/app/build.gradle` 中增加可选的 `release` 签名配置，优先从 Gradle 属性读取，兼容环境变量注入。
   - 当任务图包含 release 构建但缺少必需签名参数时，直接抛出清晰错误，阻止继续产出不可安装 APK。
2. **Server 复制任务稳健化**
   - 将 `easycontrol/server/build.gradle` 中 `copyRelease` 对 `server-release-unsigned.apk` 的硬编码改为匹配 `server-release*.apk`，兼容未来签名/未签名两种输出文件名。
3. **Workflow 发布链路修正**
   - 在 `.github/workflows/android-release.yml` 中新增签名准备步骤：从 GitHub Secrets 读取 base64 keystore 与密码，落地到临时文件，并通过环境变量传给 Gradle。
   - 保留 `:server:copyRelease` → `:app:assembleRelease` 的真实构建顺序。
   - 将 workflow artifact 分为公开 app 产物和内部 server 产物；GitHub Release 仅上传 app APK。
4. **说明文档同步**
   - README 增加 release 签名前置说明与 secrets 名称提示。
   - 知识库更新当前约束，记录 GitHub Release 只发布 app APK，server 为内部构建载荷。

### 影响范围
```yaml
涉及模块:
  - easycontrol/app/build.gradle: 增加 release 签名参数读取与缺失校验
  - easycontrol/server/build.gradle: 让 copyRelease 兼容 signed/unsigned 输出文件名
  - .github/workflows/android-release.yml: 准备签名配置，调整产物发布范围
  - README.md: 补充 GitHub Release 签名前置条件
  - .helloagents/context.md: 更新 release 约束说明
  - .helloagents/modules/repository_docs.md: 更新发布说明核实规则
预计变更文件: 6-8
```

### 风险评估
| 风险 | 等级 | 应对 |
|------|------|------|
| GitHub Secrets 未配置，workflow 将直接失败 | 中 | 让失败发生在显式的“签名准备/校验”步骤，并在 README 中写清所需 secrets |
| `copyRelease` 修改后若匹配到多个 APK，可能复制错误文件 | 低 | 仅匹配 `server-release*.apk`，并在无匹配时显式报错 |
| 只发布 app APK 后，维护者失去公开下载 server APK 的入口 | 低 | 保留 server 构建产物为 workflow artifact，供维护者排查使用 |

---

## 4. 核心场景

### 场景: 用户下载 GitHub Release 中的 APK 安装
**模块**: repository_docs
**条件**: 维护者已配置 release 签名 secrets 并推送 tag。
**行为**: workflow 生成已签名的 app release APK，并将其上传到 GitHub Release；用户只看到主控端 APK 资产。
**结果**: 用户下载的 GitHub Release APK 可以被系统正常识别和安装，不再拿到未签名或误导性的 server APK。

### 场景: 维护者未配置签名 secrets 就触发 tag release
**模块**: repository_docs
**条件**: GitHub 仓库缺少签名相关 secrets。
**行为**: workflow 在构建前直接报出缺失配置的错误，不继续发布 release APK。
**结果**: 不会再向 GitHub Release 发布不可安装的 APK；维护者能从日志里快速定位缺失的 secrets。

---

## 5. 技术决策

### fix-release-apk-signing#D001: release 产物缺少签名时直接失败，而不是继续上传不可安装 APK
**日期**: 2026-03-12
**状态**: ✅采纳
**背景**: Android 官方要求 APK 安装前必须完成签名；当前 workflow 直接发布 release APK，用户安装时失败，说明“继续上传未签名产物”比“构建失败”更糟。
**选项分析**:
| 选项 | 优点 | 缺点 |
|------|------|------|
| A: 继续允许未签名 release APK 发布 | 工作流看起来“成功” | 用户下载后无法安装，问题延后到发布后暴露 |
| B: 缺少签名配置时直接失败 | 维护者能立刻看到问题，避免发布坏包 | 需要额外配置 GitHub Secrets |
**决策**: 选择方案 B
**理由**: 对发布链路来说，“早失败”优于“发布后再让用户踩坑”。
**影响**: `easycontrol/app/build.gradle`、`.github/workflows/android-release.yml`、README/知识库发布说明

### fix-release-apk-signing#D002: GitHub Release 只公开发布 app APK，server APK 只保留为 workflow artifact
**日期**: 2026-03-12
**状态**: ✅采纳
**背景**: `server` 模块产物主要作为嵌入式载荷复制进 `app` 的 raw 资源，不是面向终端用户直接安装的主交付物。
**选项分析**:
| 选项 | 优点 | 缺点 |
|------|------|------|
| A: 继续把 app/server APK 一起公开发布 | 资产完整，维护者可直接下载两者 | 用户容易误装 server APK，且发布页噪音更大 |
| B: 公开发布 app APK，仅把 server APK 保留在 workflow artifact | 用户侧更清晰，安装入口单一 | 维护者若要拿 server APK，需要进 Actions 下载 artifact |
**决策**: 选择方案 B
**理由**: 这更符合当前产品使用方式，也能减少“下载错包”的概率。
**影响**: `.github/workflows/android-release.yml`、README、知识库发布说明
