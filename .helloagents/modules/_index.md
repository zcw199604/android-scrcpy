# 模块索引

> 通过此文件快速定位模块文档。

## 模块清单

| 模块 | 职责 | 状态 | 文档 |
|------|------|------|------|
| repository_docs | 维护仓库根目录说明文档、使用指引与图片素材索引 | ✅ | [repository_docs.md](./repository_docs.md) |
| easycontrol_app | 主控端 Android App：设备管理、ADB 连接、解码、窗口交互与配置持久化 | ✅ | [easycontrol_app.md](./easycontrol_app.md) |
| easycontrol_server | 被控端嵌入式 server：媒体采集、socket 通信、控制指令执行 | ✅ | [easycontrol_server.md](./easycontrol_server.md) |
| cloud | 历史 cloud / 激活残留说明，当前已不参与开源构建 | 📝 | [cloud.md](./cloud.md) |

## 模块依赖关系

```text
repository_docs ──> easycontrol_app
easycontrol_server ──(copy task / raw 载荷)──> easycontrol_app
cloud ──(历史商业版说明，当前构建已移除)──> repository_docs
```

## 状态说明
- ✅ 稳定/已有实现
- 🚧 开发中/实现未闭环
- 📝 仅文档占位或待补充
