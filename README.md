# BloodPressureTracker (血压记录)

一个简洁的 Android 血压记录管理应用，支持记录追踪、数据看板、CSV 导入导出和定时提醒。

## 功能

- **血压记录**：记录收缩压（高压）、舒张压（低压）和心率，支持自定义日期时间和备注
- **血压分级**：自动根据最新医学标准判断血压等级（正常/偏高/高血压1级/高血压2级/危险），并显示对应配色
- **数据看板**：趋势折线图 + 分布柱状图，支持最近一周/一月/三月/全部时间范围切换
- **CSV 导入导出**：通过系统文件管理器导出/导入 CSV 数据，便于备份和跨平台使用
- **定时提醒**：可设置每日提醒时间，Doze 模式下也能精确触发，点击通知自动打开应用，当天已有记录自动跳过
- **开机自启**：设备重启后自动恢复闹钟设置
- **纯本地存储**：SQLite 本地数据库，无需网络权限，数据安全无忧

## 血压分级标准

依据《中国高血压防治指南(2024年修订版)》：

| 分类 | 收缩压 (mmHg) | 舒张压 (mmHg) | 标签颜色 |
|------|--------------|--------------|---------|
| 正常血压 | < 120 | 和 < 80 | 绿色 |
| 正常高值 | 120–139 | 和/或 80–89 | 黄色 |
| 1级高血压(轻度) | 140–159 | 和/或 90–99 | 橙色 |
| 2级高血压(中度) | 160–179 | 和/或 100–109 | 红色 |
| 3级高血压(重度) | ≥ 180 | 和/或 ≥ 110 | 深红色 |

> 注：当收缩压和舒张压分属于不同级别时，以较高的分级为准。可在应用内「设置 → 血压标准参考」查看完整标准。

## 技术栈

- **语言**：Java
- **最低 SDK**：Android 7.0 (API 24)
- **目标 SDK**：Android 9 (API 28)
- **构建工具**：Gradle 4.4.1 + AGP 3.1.4
- **UI 框架**：Material Components + AndroidX
- **数据库**：SQLite (SQLiteOpenHelper)
- **图表**：自定义 View 绘制（Canvas）

## 项目结构

```
app/src/main/java/com/example/bptracker/
├── BloodPressureRecord.java    # 数据模型
├── BPLevel.java                # 血压分级逻辑
├── DatabaseHelper.java         # SQLite 数据库操作
├── MainActivity.java           # 主界面（底部导航容器）
├── AddEditActivity.java        # 新增/编辑记录页面
├── FragmentRecord.java         # 记录列表 Fragment
├── FragmentDashboard.java      # 看板 Fragment
├── FragmentSettings.java       # 设置 Fragment
├── RecordAdapter.java          # 记录列表 RecyclerView 适配器
├── BPChartView.java            # 趋势折线图自定义 View
├── BPDistributionView.java     # 分布柱状图自定义 View
├── CSVHelper.java              # CSV 导入导出工具
├── NotificationHelper.java     # 通知管理（闹钟、渠道）
├── NotificationReceiver.java   # 通知广播接收器
├── BootReceiver.java           # 开机自启动接收器
└── ...
```

## 构建

```bash
# 设置环境变量
export JAVA_HOME=/path/to/jdk8
export ANDROID_SDK_ROOT=/path/to/android-sdk
export ANDROID_HOME=$ANDROID_SDK_ROOT

# 安装必要 SDK 组件
sdkmanager "platforms;android-28" "build-tools;28.0.3" "platform-tools"

# 构建 Debug APK
gradle assembleDebug

# 构建 Release APK（需先生成签名密钥）
gradle assembleRelease
```

> 注意：构建前需将 `app/build.gradle` 中的 `signingConfigs.release` 改为你自己的签名配置。

## 版本历史

所有 APK 文件位于 `apk/` 目录下，每个版本包含 Debug 版和 Release 签名版。

| 版本 | 文件名 | 大小 | 更新内容 |
|------|--------|------|----------|
| v1.0 | `BloodPressureTracker-v1.apk` / `-v1-release.apk` | 2.1 MB / 1.8 MB | 初始版本：血压记录、编辑删除、SQLite 存储 |
| v2.0 | `BloodPressureTracker-v2.apk` / `-v2-release.apk` | 2.2 MB / 1.9 MB | 新增底部导航栏（记录/看板/设置）、血压分级配色、趋势图+分布图、CSV 导入导出、定时通知 |
| v3.0 | `BloodPressureTracker-v3.apk` / `-v3-release.apk` | 2.2 MB / 1.9 MB | 修复记录页背景异常、输入框文字遮挡、导入导出改用 SAF 文件选择器、看板按钮文字颜色 |
| v4.0 | `BloodPressureTracker-v4.apk` / `-v4-release.apk` | 2.2 MB / 1.9 MB | 修复定时通知不生效（setExactAndAllowWhileIdle + 通知渠道 + 开机自启） |
| v5.0 | `BloodPressureTracker-v5.apk` / `-v5-release.apk` | 2.2 MB / 1.9 MB | 点击通知自动打开应用 |
| v6.0 | `BloodPressureTracker-v6.apk` / `-v6-release.apk` | 2.2 MB / 1.9 MB | 升级为中国2024指南标准（正常/正常高值/1-3级），新增血压标准参考页面 |
| v6.2 | `BloodPressureTracker-v6.2.apk` / `-v6.2-release.apk` | 2.2 MB / 1.9 MB | 看板时间按钮改为横向滚动+自适应宽度，默认选中最近一周 |
| v7.0 | `BloodPressureTracker-v7.0.apk` / `-v7.0-release.apk` | 2.2 MB / 1.9 MB | 新增语音输入血压功能，使用系统内置SpeechRecognizer，完全免费 |

## 安装

1. 从 `apk/` 目录下载对应版本的 APK 文件
2. 传输到 Android 手机（Android 7.0+）
3. 在手机设置中开启"允许安装未知来源应用"
4. 点击 APK 安装即可

## License

MIT