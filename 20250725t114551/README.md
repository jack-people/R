# 🤖 Android模拟器AI监控系统

让AI实时观察和分析您的Android应用运行效果的智能监控系统。

## ✨ 功能特性

### 🎯 核心功能
- **实时屏幕监控**: 捕获Android模拟器屏幕变化，检测UI元素变动
- **音频监控**: 实时录制和分析应用产生的音效
- **日志分析**: 监控系统日志，自动识别错误和警告
- **AI智能分析**: 使用人工智能理解和描述应用运行状态

### 🧠 AI能力
- **视觉理解**: "我看到按钮颜色从灰色变为蓝色"
- **音频感知**: "听到了点击音效和背景音乐"
- **行为分析**: "检测到加载动画和进度条"
- **智能建议**: 提供UI优化和性能改进建议

### 🌐 Web界面
- **实时监控面板**: 直观显示所有监控数据
- **AI分析展示**: 实时显示AI的观察和分析结果
- **交互式控制**: 一键启动/停止监控
- **响应式设计**: 支持桌面和移动设备

## 🚀 快速开始

### 环境要求
- Python 3.7+
- Android SDK (包含ADB工具)
- Android模拟器或真实设备
- 现代Web浏览器

### 安装步骤

1. **克隆项目**
```bash
git clone <项目地址>
cd android-ai-monitor
```

2. **安装依赖**
```bash
pip install -r requirements.txt
```

3. **启动Android模拟器**
   - 打开Android Studio
   - 启动AVD Manager
   - 运行一个Android模拟器

4. **启动监控系统**
```bash
python start_monitor.py
```

5. **打开Web界面**
   - 在浏览器中访问: http://localhost:5000
   - 点击"开始监控"按钮

## 📱 使用指南

### 基本操作

1. **启动监控**
   - 确保Android模拟器正在运行
   - 在Web界面点击"🚀 开始监控"
   - 系统将自动检测并连接到模拟器

2. **观察AI分析**
   - AI会实时分析屏幕变化、音频和日志
   - 在"AI实时分析"面板查看智能描述
   - 观察屏幕活动、音频状态和系统状态

3. **监控应用**
   - 在模拟器中运行您的Android应用
   - AI会描述应用的视觉和音频效果
   - 查看详细的UI变化和交互反馈

### 高级功能

#### 自定义设备ID
如果有多个Android设备，可以指定特定设备：
```python
monitor = AndroidEmulatorMonitor(device_id="emulator-5554")
```

#### 调整监控参数
在`android_monitor.py`中可以调整：
- 屏幕捕获帧率: `screen_capture_fps`
- 音频采样率: `audio_sample_rate`
- 数据保留数量等

## 🏗️ 系统架构

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Android设备    │────│   监控核心模块    │────│   Web用户界面   │
│                 │    │                  │    │                 │
│ • 屏幕截图      │    │ • 数据采集       │    │ • 实时显示      │
│ • 音频录制      │    │ • AI分析         │    │ • 交互控制      │
│ • 日志输出      │    │ • 智能理解       │    │ • 状态监控      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### 核心组件

1. **AndroidEmulatorMonitor**: 主监控类
2. **ScreenRecorder**: 屏幕录制模块
3. **AudioRecorder**: 音频录制模块
4. **LogMonitor**: 日志监控模块
5. **AIAnalyzer**: AI分析引擎
6. **Web Interface**: Flask + SocketIO Web界面

## 🔧 技术栈

### 后端技术
- **Python**: 核心开发语言
- **OpenCV**: 图像处理和计算机视觉
- **PyAudio**: 音频录制和处理
- **Flask**: Web框架
- **SocketIO**: 实时通信
- **ADB**: Android调试桥接

### 前端技术
- **HTML5/CSS3**: 现代Web界面
- **JavaScript**: 交互逻辑
- **WebSocket**: 实时数据传输
- **响应式设计**: 多设备支持

## 🎨 界面预览

### 主监控面板
- 🤖 AI实时分析区域
- 📱 屏幕监控显示
- 🔊 音频可视化
- 📋 系统日志面板

### AI分析示例
```
AI观察到:
• 登录按钮从灰色变为蓝色
• 出现了"正在登录..."的加载动画
• 听到了按钮点击的音效
• 系统运行正常，无错误日志
```

## 🛠️ 开发指南

### 扩展AI分析能力
在`AIAnalyzer`类中添加新的分析方法：

```python
def _analyze_custom_feature(self, data):
    """自定义分析功能"""
    # 实现您的分析逻辑
    return analysis_result
```

### 添加新的监控模块
继承基础监控类并实现您的功能：

```python
class CustomMonitor:
    def __init__(self, monitor):
        self.monitor = monitor
    
    def start(self):
        # 实现监控逻辑
        pass
```

## 🐛 故障排除

### 常见问题

1. **ADB未找到**
   - 确保Android SDK已安装
   - 将ADB路径添加到系统PATH

2. **设备连接失败**
   - 检查模拟器是否正在运行
   - 运行`adb devices`确认设备列表

3. **音频录制失败**
   - 检查系统音频权限
   - 确保没有其他应用占用音频设备

4. **Web界面无法访问**
   - 检查端口5000是否被占用
   - 确认防火墙设置

### 调试模式
启用详细日志输出：
```bash
python start_monitor.py --debug
```

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

### 开发环境设置
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 🙏 致谢

感谢以下开源项目的支持：
- OpenCV - 计算机视觉库
- Flask - Web框架
- PyAudio - 音频处理
- Socket.IO - 实时通信

---

**让AI成为您的Android开发助手！** 🚀