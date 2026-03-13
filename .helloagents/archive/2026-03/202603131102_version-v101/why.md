# 变更提案: version-v101

## 需求背景
用户要求把当前版本提升为 `v1.0.1`，并且同时修改：
1. Git 发布 tag；
2. Android 安装版本（`versionCode` / `versionName`）。

当前仓库状态：
- 现有最新发布 tag 为 `v1.0.0`
- `easycontrol/app/build.gradle` 中安装版本仍为 `versionCode 10000` / `versionName "1.0.0"`

因此，本次需要把应用安装版本提升到 `1.0.1`，并在对应提交上创建并推送 `v1.0.1` annotated tag。

## 变更内容
1. 将 app 模块版本调整为 `versionCode 10001`、`versionName "1.0.1"`。
2. 同步更新知识库中的版本记录与归档索引。
3. 验证构建产物中的安装版本确已变为 `1.0.1`。
4. 创建并推送 annotated tag `v1.0.1`。

## 影响范围
- **模块:** `easycontrol_app`, `repository_docs`
- **文件:**
  - `easycontrol/app/build.gradle`
  - `.helloagents/CHANGELOG.md`
  - `.helloagents/archive/_index.md`

## 验收标准
1. `easycontrol/app/build.gradle` 中版本已更新为 `10001 / 1.0.1`。
2. `./gradlew :app:assembleDebug` 构建通过。
3. 构建出的 APK 元数据中可见 `versionCode='10001'`、`versionName='1.0.1'`。
4. 本地与远端都存在 `v1.0.1` tag，且指向本次版本更新提交。
