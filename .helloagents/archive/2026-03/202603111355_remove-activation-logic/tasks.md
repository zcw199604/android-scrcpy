# 任务清单: remove_activation_logic

> **@status:** completed | 2026-03-11 14:06

```yaml
@feature: remove_activation_logic
@created: 2026-03-11
@status: completed
@mode: R2
```

<!-- LIVE_STATUS_BEGIN -->
状态: completed | 进度: 4/4 (100%) | 更新: 2026-03-11 14:06:09
当前: 全部任务完成，待归档
<!-- LIVE_STATUS_END -->

## 进度概览

| 完成 | 失败 | 跳过 | 总数 |
|------|------|------|------|
| 4 | 0 | 0 | 4 |

---

## 任务列表

### 1. 入口放开与导航清理

- [√] 1.1 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/MainActivity.java` 与 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/UsbActivity.java` 中移除首次启动和 USB 入口的 `isActive` 门控 [降级执行] | depends_on: []
- [√] 1.2 在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/SetActivity.java`、`easycontrol/app/src/main/AndroidManifest.xml` 与 `easycontrol/settings.gradle` 中清理激活页与缺失 cloud 模块入口 [降级执行] | depends_on: [1.1]

### 2. 激活模块与资源清理

- [√] 2.1 删除 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/ActiveActivity.java`，并在 `easycontrol/app/src/main/java/top/saymzx/easycontrol/app/entity/Setting.java` 中移除已废弃的激活状态/激活码访问接口 [降级执行] | depends_on: [1.2]
- [√] 2.2 清理 `easycontrol/app/src/main/res/layout/activity_active.xml`、`easycontrol/app/src/main/res/values/strings.xml` 与 `easycontrol/app/src/main/res/values-en/strings.xml` 中的激活/捐赠资源，并验证仓库内无残留 `ActiveActivity` / `ActiveHelper` / `isActive` / `activeKey` 引用 [降级执行] | depends_on: [2.1]

---

## 执行日志

| 时间 | 任务 | 状态 | 备注 |
|------|------|------|------|
| 2026-03-11 14:06:09 | 1.1 | completed | 主入口与 USB 入口已放开；原生子代理超时后由主代理降级执行 |
| 2026-03-11 14:06:09 | 1.2 | completed | 已移除设置页激活入口、Manifest 注册与 `:cloud` 声明 |
| 2026-03-11 14:06:09 | 2.1 | completed | 已删除 `ActiveActivity` 并清理 `Setting` 激活接口 |
| 2026-03-11 14:06:09 | 2.2 | completed | 已删除激活布局与文案，并完成残留引用检索 |

---

## 执行备注

> 记录执行过程中的重要说明、决策变更、风险提示等
- 设计阶段判定 TASK_COMPLEXITY=moderate。
- 原生子代理与 pkg_keeper / kb_keeper 调用已按规则尝试，但执行阶段出现超时/中断，相关步骤由主代理降级执行并保留结果。
- 本地执行环境缺少 Java / Android SDK，无法完成 Gradle 编译级验证；已改为执行 XML 解析、残留引用检索与 diff 检查。
