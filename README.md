# 易控(Easycontrol)

## 注意
 Gitee和GitHub代码将保持同步，请自行选择。
- [Gitee地址](https://gitee.com/mingzhixianweb/easycontrol)
- [Github地址](https://github.com/mingzhixian/Easycontrol)

## 简介
本软件基于开源项目 Scrcpy，对其进行了大量魔改，实现了安卓客户端，并扩展为“安卓端控制安卓端”的使用模式。

## 功能特色
- 使用简单
- 支持音频传输
- 多设备连接
- 支持有线连接
- 多设备剪切板同步
- 多设备共享主控端物理键盘(需配合微信输入法或QQ输入法等输入中文)
- 启动迅速
- 低延迟
- 支持分辨率自适应
- 良好的旋转支持
- 支持小窗显示与全屏显示

## 使用说明
- [点击此处查看](https://gitee.com/mingzhixianweb/easycontrol/blob/master/HOW_TO_USE.md)

## 软件下载
- [点击此处查看](https://gitee.com/mingzhixianweb/easycontrol/releases)

## 项目支持
当前仓库中的开源源码可以自行构建与使用，不再以内置“激活/订单号”作为前置条件。
如果这个项目帮到了你，且你愿意支持持续维护，可以参考 [DONATE.md](./DONATE.md) 中的自愿支持说明。

## 截图
<center class="half">
 <img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/1.jpg" width="150"/><img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/2.jpg" width="150"/><img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/3.jpg" width="150"/>
 <img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/4.jpg" width="150"/><img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/5.jpg" width="150"/><img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/6.jpg" width="150"/>
 <img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/7.jpg" width="150"/><img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/screenshot/8.jpg" width="150"/>
</center>

## 构建
如果您想要自己构建当前开源仓库，请注意以下几项：
- 请遵循本项目的开源协议。
- 当前源码树已经移除了此前的激活门控相关代码，**不需要**再手工注释“激活模块”才能继续构建。
- 构建前请准备可用的 Java、Android SDK 以及对应的 Gradle/Android Studio 环境。
- `app` 模块在运行时会使用 `R.raw.easycontrol_server`，该资源由 `server` 模块构建后复制生成，因此建议按以下顺序构建：

```bash
cd easycontrol
bash ./gradlew :server:copyDebug
bash ./gradlew :app:assembleDebug
```

如果你希望一次执行，也可以直接尝试：

```bash
cd easycontrol
bash ./gradlew :server:copyDebug :app:assembleDebug
```

其中：
- `:server:copyDebug` 会先构建 `server` 模块，并把产物复制为 `app/src/main/res/raw/easycontrol_server.jar`
- `:app:assembleDebug` 会继续打包主控端 APK

如果你是维护者并希望通过 GitHub 发布 tag release，当前发布链路会在 GitHub 上按 release 顺序执行：

```bash
cd easycontrol
bash ./gradlew :server:copyRelease :app:assembleRelease
```

仓库中的 `.github/workflows/android-release.yml` 会在推送任意 tag 时自动执行，也支持在 GitHub Actions 页面手工指定 tag 重新打包。该流程会把 release APK 作为当前 tag 对应的 GitHub Release 资产上传；本地调试与日常开发仍以上述 debug 构建命令为准。

## 反馈
请在Github或Gitee提出Issue，或进入易控反馈群反馈BUG或建议。

<img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/other/qq_issue.webp" width="200px">
<img src="https://gitee.com/mingzhixianweb/easycontrol/raw/master/pic/other/wechat_issue.png" width="200px">

## 附加
- ADB协议说明(官方的文档写的真烂，感谢cstyan大佬) [点击前往](https://github.com/cstyan/adbDocumentation)
- Scrcpy官方地址 [点击前往](https://github.com/Genymobile/scrcpy)
- 易控车机版(第三方用户专为车机进行了调整优化) [点击前往](https://github.com/eiyooooo/Easycontrol_For_Car)
