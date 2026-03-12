# repository_docs

## 职责

负责仓库根目录的人类可读文档与图片素材，包括项目简介、使用说明、开源版构建说明、GitHub tag release / GitHub Release 资产与签名要求说明、项目支持/历史发行版差异说明、隐私政策以及截图/提示图片引用。

## 接口定义（可选）

> 该模块不提供代码 API，主要提供文档入口。

### 文档入口
| 文档/资源 | 位置 | 说明 |
|----------|------|------|
| README | `README.md` | 项目简介、开源版构建说明、GitHub tag release 发布/签名说明与使用入口 |
| HOW_TO_USE | `HOW_TO_USE.md` | 连接、投屏、界面操作说明 |
| DONATE | `DONATE.md` | 项目支持说明 / 历史发行版差异（非激活前置） |
| PRIVACY | `PRIVACY.md` | 隐私政策 |
| 图片资源 | `pic/` | 截图、提示图与外链素材 |

## 行为规范

### 新用户接入
**条件**: 首次了解项目或需要使用方式说明。
**行为**: 先阅读 `README.md` 获取概览，再进入 `HOW_TO_USE.md` 查看操作步骤。
**结果**: 用户能够理解项目定位、连接方式与主要功能。

### 开源版说明核实
**条件**: 需要确认当前公开文档是否仍包含激活/订单前置。
**行为**: 以 `README.md` 的“项目支持/构建”段与 `DONATE.md` 的“说明”段为准；当前应理解为“开源源码可直接构建与使用”，`DONATE.md` 仅描述自愿支持与历史发行版差异。
**结果**: 不再把激活、订单号或历史私有能力误判为当前开源仓库前置条件。

### 构建说明核实
**条件**: 需要本地构建或排查开源版与官方版差异。
**行为**: 优先检查 `README.md` 的“构建”段、`easycontrol/settings.gradle`、`easycontrol/app/build.gradle`、`easycontrol/server/build.gradle` 与实际目录结构；本地调试构建应先执行 `:server:copyDebug` 再执行 `:app:assembleDebug`，GitHub tag release 发布则沿用 `:server:copyRelease` → `:app:assembleRelease` 的 release 顺序生成 APK 资产，同时要求提供 `EC_RELEASE_*` 签名参数，而本地 CLI 环境已在 `bash ./gradlew :server:copyDebug` 阶段因 `JAVA_HOME is not set` 失败。
**结果**: 在动手修改构建链路前，先确认当前开源仓库的真实构建边界、发布/调试差异与历史残留差异。

### 发布说明核实
**条件**: 需要确认当前仓库是否支持 GitHub 自动发布 APK。
**行为**: 以 `README.md`、`.github/workflows/android-release.yml` 与 `easycontrol/app/build.gradle` 的发布说明为准；当前 GitHub 发布方式是推送任意 tag 自动打包，或在 Actions 页面手工指定 tag 重新打包，核心命令为 `bash ./gradlew :server:copyRelease :app:assembleRelease`。release 构建前必须提供 GitHub Secrets/`EC_RELEASE_*` 签名参数；GitHub Release 只公开发布主控端 app APK，server 产物仅保留在 workflow artifact。
**结果**: 不会再把当前仓库误判为“只有本地手工构建，没有 GitHub tag release 产物”。

## 依赖关系

```yaml
依赖: 无
被依赖: easycontrol_app, cloud
```
