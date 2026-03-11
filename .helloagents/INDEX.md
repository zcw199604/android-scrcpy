# android-scrcpy（Easycontrol）知识库

> 本文件是项目知识库的入口点。

## 快速导航

| 需要了解 | 读取文件 |
|---------|---------|
| 项目概况、技术栈、当前约束 | [context.md](context.md) |
| 模块索引 | [modules/_index.md](modules/_index.md) |
| Android 主控端实现 | [modules/easycontrol_app.md](modules/easycontrol_app.md) |
| 嵌入式被控端 server | [modules/easycontrol_server.md](modules/easycontrol_server.md) |
| 文档与构建说明 | [modules/repository_docs.md](modules/repository_docs.md) |
| 历史 cloud / 激活残留说明 | [modules/cloud.md](modules/cloud.md) |
| 项目变更历史 | [CHANGELOG.md](CHANGELOG.md) |
| 历史方案索引 | [archive/_index.md](archive/_index.md) |
| 当前待执行方案 | [plan/](plan/) |
| 历史会话记录 | [sessions/](sessions/) |

## 模块关键词索引

| 模块 | 关键词 | 摘要 |
|------|--------|------|
| repository_docs | README, HOW_TO_USE, DONATE, PRIVACY, pic | 仓库根目录说明文档、使用指引与素材资源。 |
| easycontrol_app | Android, ADB, USB, Client, Device, 悬浮窗 | 主控端 Android App，负责设备发现、连接、解码与界面交互，当前已去除首次进入激活门控。 |
| easycontrol_server | server, socket, video, audio, control, scrcpy | 运行在被控端的嵌入式 server，负责媒体采集与控制指令处理。 |
| cloud | 激活, cloud, 历史残留, 商业版 | 记录已从当前开源构建中移除的 cloud / 激活残留背景。 |

## 知识库状态

```yaml
kb_version: 2.3.0
最后更新: 2026-03-11 15:16 UTC
模块数量: 4
待执行方案: 0
```

## 读取指引

```yaml
启动任务:
  1. 先读取本文件获取导航
  2. 再读取 context.md 获取项目上下文与约束
  3. 根据任务范围进入对应模块文档

任务相关:
  - 涉及 Android 主控端: 读取 modules/easycontrol_app.md
  - 涉及被控端采集/控制协议: 读取 modules/easycontrol_server.md
  - 涉及构建/开源版本差异: 读取 modules/cloud.md 与 modules/repository_docs.md
  - 需要历史方案: 检查 archive/_index.md 与 plan/
```
