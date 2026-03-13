# 变更提案: version-v102

## 需求背景
用户要求在当前项目基础上重新发布为 `v1.0.2`，并同步修改 Android 安装版本号。当前仓库状态为：
- 已发布 `v1.0.1` tag；
- app 安装版本为 `versionCode 10001` / `versionName "1.0.1"`；
- 本次还包含一笔刚整理提交的链接修正，需要通过新的补丁版本统一发布。

因此，本次需要把 app 安装版本提升到 `1.0.2`，并在对应提交上创建并推送 annotated tag `v1.0.2`。

## 变更内容
1. 将 app 模块安装版本调整为 `versionCode 10002`、`versionName "1.0.2"`。
2. 同步更新知识库中的版本记录与归档索引。
3. 验证构建产物中的安装版本确已变为 `1.0.2`。
4. 推送 `main` 并创建/推送 annotated tag `v1.0.2`。

## 影响范围
- **模块:** `easycontrol_app`, `repository_docs`
- **文件:**
  - `easycontrol/app/build.gradle`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`
  - `.helloagents/archive/2026-03/202603131102_version-v101/task.md`

## 验收标准
1. `easycontrol/app/build.gradle` 中版本已更新为 `10002 / 1.0.2`。
2. `./gradlew :app:assembleDebug` 构建通过。
3. 构建出的 APK 元数据中可见 `versionCode='10002'`、`versionName='1.0.2'`。
4. 本地与远端都存在 `v1.0.2` tag，且指向本次版本更新提交。
