# 任务清单: cleanup_docs_build_validation

> **@status:** completed | 2026-03-11 14:22

```yaml
@feature: cleanup_docs_build_validation
@created: 2026-03-11
@status: completed
@mode: R2
```

<!-- LIVE_STATUS_BEGIN -->
状态: completed | 进度: 4/4 (100%) | 更新: 2026-03-11 14:20:00
当前: 全部任务完成，待归档
<!-- LIVE_STATUS_END -->

## 进度概览

| 完成 | 失败 | 跳过 | 总数 |
|------|------|------|------|
| 4 | 0 | 0 | 4 |

---

## 任务列表

### 1. 文档清理

- [√] 1.1 更新 `README.md`，移除过时的激活/注释构建说明，并补充基于当前脚本的构建顺序说明 | depends_on: []
- [√] 1.2 更新 `DONATE.md`，改为自愿支持/历史说明，不再把捐赠与激活绑定 | depends_on: [1.1]

### 2. 知识库与验证

- [√] 2.1 同步 `.helloagents/context.md`、`.helloagents/modules/repository_docs.md` 与 `.helloagents/modules/cloud.md`，使其与清理后的公开文档一致 [降级执行] | depends_on: [1.2]
- [√] 2.2 尝试执行最小本地构建验证，记录命令、结果与阻塞点，并将结论写入方案包与最终报告 | depends_on: [2.1]

---

## 执行日志

| 时间 | 任务 | 状态 | 备注 |
|------|------|------|------|
| 2026-03-11 14:20:00 | 1.1 | completed | README 已改为“项目支持 + 当前构建顺序”说明 |
| 2026-03-11 14:20:00 | 1.2 | completed | DONATE 已改为自愿支持说明，不再包含订单号激活流程 |
| 2026-03-11 14:20:00 | 2.1 | completed | 知识库文档已同步；kb_keeper 子代理已尝试调用，主代理执行最小修正 |
| 2026-03-11 14:20:00 | 2.2 | completed | 已执行 `bash ./gradlew :server:copyDebug`；因 `JAVA_HOME is not set` 失败，已记录为环境阻塞 |

---

## 执行备注

> 记录执行过程中的重要说明、决策变更、风险提示等
- 设计阶段判定 TASK_COMPLEXITY=moderate。
- 文档中已移除“需要激活/手工注释激活代码才能构建”的过时表述。
- 当前本地验证止步于 Java 环境缺失，尚未进入 Android SDK、`server:copyDebug` 或 `app:assembleDebug` 的实际执行阶段。
- 从构建脚本和代码关系可知，后续应先执行 `bash ./gradlew :server:copyDebug`，生成 `app/src/main/res/raw/easycontrol_server.jar`，再执行 `bash ./gradlew :app:assembleDebug`。
