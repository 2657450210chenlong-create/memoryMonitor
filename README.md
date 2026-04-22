# Android Memory Monitor

一个可复用的 Android 内存监控 SDK，拆分为 3 个独立模块：

- `memory-monitor-core`
- `memory-monitor-core-popup`
- `memory-monitor-core-service`

## Module 结构

- `memory-monitor-core`: 采样、状态聚合、全局单例提供器。
- `memory-monitor-core-popup`: 应用内 `PopupWindow` 展示层。
- `memory-monitor-core-service`: 前台服务通知栏展示层（含进度条）。
- `app`: Demo 应用，演示 3 个模块联动。

## 快速接入（JitPack）

### 1) 添加仓库

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### 2) 添加依赖

```kotlin
dependencies {
    implementation("com.github.<your_github_owner>:memory-monitor-core:<tag>")
    implementation("com.github.<your_github_owner>:memory-monitor-core-popup:<tag>")
    implementation("com.github.<your_github_owner>:memory-monitor-core-service:<tag>")
}
```

## Core API

```kotlin
val monitor = MemoryMonitors.create()
monitor.initialize(
    MemoryMonitorConfig(
        sampleIntervalMs = 100L,
        maxSamples = 600
    )
)

MemoryMonitorProvider.register(monitor)
monitor.start()
```

```kotlin
interface MemoryMonitor {
    fun initialize(config: MemoryMonitorConfig)
    fun start()
    fun stop()
    fun clear()
    fun isRunning(): Boolean
    fun latestSample(): MemorySample?
    fun getHistory(): List<MemorySample>
    fun observeSamples(): Flow<MemorySample>
    fun currentState(): MemoryMonitorState
    fun observeState(): Flow<MemoryMonitorState>
}
```

```kotlin
object MemoryMonitorProvider {
    fun register(monitor: MemoryMonitor)
    fun get(): MemoryMonitor
    fun isRegistered(): Boolean
}
```

## Popup API

```kotlin
MemoryMonitorPopup.show(
    host = activity,
    config = PopupConfig(
        position = PopupPosition.TOP_END, // 默认右上角
        xOffsetPx = 0,
        yOffsetPx = 0
    )
)
```

自定义布局：

```kotlin
MemoryMonitorPopup.show(
    host = activity,
    config = PopupConfig(
        customLayoutResId = R.layout.my_popup_layout,
        customBinder = { view, state ->
            // bind state to your view
        }
    )
)
```

隐藏：

```kotlin
MemoryMonitorPopup.dismiss()
```

## Service API

```kotlin
MemoryMonitorForegroundController.start(
    context = context,
    config = ServiceConfig(
        throttleMs = 1000L
    )
)
```

```kotlin
MemoryMonitorForegroundController.stop(context)
```

通知栏进度条语义：

- `progress = currentUsedBytes / maxMemoryBytes`（0-100）
- 默认每 1 秒刷新一次
- 不设置通知点击跳转

## 发布

项目内置：

- `jitpack.yml`（发布 3 个模块到本地 Maven）
- `.github/workflows/release.yml`（手动输入版本号发布）

首次推送示例：

```bash
git remote add origin https://github.com/<your_github_owner>/android-memory-monitor.git
git add .
git commit -m "feat: add popup and service extension modules"
git push -u origin main
```
