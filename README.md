# 水印相机 (WatermarkCamera)

一个基于Android CameraX开发的现代化水印相机应用，支持实时预览、多种水印类型和高质量照片输出。

## 📱 项目概述

水印相机是一个功能丰富的Android相机应用，旨在为用户提供专业的拍照体验和灵活的水印添加功能。项目采用现代Android开发技术栈，遵循Material Design设计规范。

### 🎯 核心功能

- **📷 实时相机预览** - 基于CameraX的高性能相机预览
- **🖼️ 多种水印类型** - 文字水印、时间水印、位置水印、图片水印
- **⚡ 智能相机控制** - 前后摄像头切换、闪光灯控制、自动对焦
- **🎨 现代化UI设计** - Material Design 3、深色主题、响应式布局
- **🔒 权限管理** - 智能权限请求、用户友好的权限说明
- **📁 相册集成** - 照片自动保存、相册快速访问

## 🏗️ 技术架构

### 核心技术栈

- **开发语言**: Kotlin
- **最低SDK版本**: Android 7.0 (API 24)
- **目标SDK版本**: Android 14 (API 35)
- **相机框架**: CameraX 1.3.1
- **UI框架**: Material Design 3
- **架构模式**: MVVM + Repository Pattern

### 主要依赖

```kotlin
// CameraX - 现代相机开发框架
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.4.2")
implementation("androidx.camera:camera-extensions:1.3.1")

// Android基础库
implementation("androidx.core:core-ktx:1.16.0")
implementation("androidx.appcompat:appcompat:1.7.1")
implementation("com.google.android.material:material:1.12.0")
implementation("androidx.constraintlayout:constraintlayout:2.2.1")
```

## 📂 项目结构

```
app/src/main/
├── java/com/example/watermarkcamera/
│   ├── MainActivity.kt                 # 主Activity
│   ├── camera/
│   │   ├── CameraManager.kt           # 相机管理类
│   │   └── CameraCallback.kt          # 相机回调接口
│   ├── utils/
│   │   └── PermissionManager.kt       # 权限管理工具
│   └── watermark/                     # 水印功能模块 (待开发)
├── res/
│   ├── layout/
│   │   └── activity_main.xml          # 主界面布局
│   ├── drawable/                      # 图标和背景资源
│   ├── values/
│   │   ├── colors.xml                 # 颜色定义
│   │   ├── strings.xml                # 字符串资源
│   │   └── themes.xml                 # 主题样式
│   └── mipmap/                        # 应用图标
└── AndroidManifest.xml                # 应用清单文件
```

## ✅ 已完成功能

### 1. 🎨 UI界面设计
- ✅ 现代化相机界面布局
- ✅ 响应式设计，适配不同屏幕尺寸
- ✅ Material Design 3风格
- ✅ 深色主题支持
- ✅ 沉浸式全屏体验

### 2. 🔐 权限管理系统
- ✅ 完整的权限管理类 (`PermissionManager`)
- ✅ 智能权限检查和请求
- ✅ 用户友好的权限说明对话框
- ✅ 权限被拒绝的优雅处理
- ✅ 支持Android不同版本的权限差异

### 3. 📷 相机预览功能
- ✅ CameraX集成和初始化
- ✅ 实时相机预览 (`PreviewView`)
- ✅ 相机生命周期管理
- ✅ 前后摄像头切换
- ✅ 闪光灯模式控制
- ✅ 相机错误处理和回调

### 4. 🏗️ 架构设计
- ✅ 模块化代码结构
- ✅ 回调接口设计模式
- ✅ 资源文件规范化管理
- ✅ 错误处理和日志系统

## 🚧 待开发功能

### 1. 📸 拍照功能
- ⏳ 基础拍照功能实现
- ⏳ 照片质量设置
- ⏳ 照片保存到相册
- ⏳ 拍照音效和动画

### 2. 🖼️ 水印系统
- ⏳ 文字水印编辑器
- ⏳ 时间水印自动生成
- ⏳ 位置水印 (需要位置权限)
- ⏳ 图片水印支持
- ⏳ 水印位置和大小调整
- ⏳ 水印透明度控制
- ⏳ 水印模板系统

### 3. 🎛️ 高级相机功能
- ⏳ 手动对焦
- ⏳ 曝光控制
- ⏳ 缩放手势支持
- ⏳ 网格线显示
- ⏳ 拍照倒计时

### 4. 📁 相册和分享
- ⏳ 相册浏览功能
- ⏳ 照片编辑功能
- ⏳ 社交分享集成
- ⏳ 批量水印处理

### 5. ⚙️ 设置和配置
- ⏳ 应用设置页面
- ⏳ 照片质量配置
- ⏳ 默认水印设置
- ⏳ 主题切换功能

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 11 或更高版本
- Android SDK API 35
- 支持相机的Android设备 (API 24+)

### 构建步骤

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd WatermarkCamera
   ```

2. **打开项目**
   - 使用Android Studio打开项目
   - 等待Gradle同步完成

3. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击运行按钮或使用快捷键 `Shift + F10`

### 权限说明

应用需要以下权限：
- **相机权限** (`CAMERA`) - 用于拍照和预览
- **存储权限** (`WRITE_EXTERNAL_STORAGE`) - 保存照片 (Android 9及以下)

## 🔧 开发指南

### 添加新功能

1. **创建功能模块**
   ```kotlin
   // 在对应包下创建新的类文件
   package com.example.watermarkcamera.watermark
   
   class WatermarkManager {
       // 功能实现
   }
   ```

2. **更新UI布局**
   ```xml
   <!-- 在activity_main.xml中添加新的UI元素 -->
   ```

3. **集成到MainActivity**
   ```kotlin
   // 在MainActivity中集成新功能
   private lateinit var watermarkManager: WatermarkManager
   ```

### 代码规范

- 使用Kotlin编程语言
- 遵循Android官方代码规范
- 添加详细的注释说明
- 使用有意义的变量和方法名
- 实现适当的错误处理

## 📝 更新日志

### v0.1.0 (当前版本)
- ✅ 完成基础UI界面设计
- ✅ 实现权限管理系统
- ✅ 集成CameraX相机预览
- ✅ 添加相机控制功能

### 计划中的版本

#### v0.2.0
- 📸 实现基础拍照功能
- 🖼️ 添加简单文字水印

#### v0.3.0
- ⏰ 时间水印功能
- 📍 位置水印功能
- 🎨 水印编辑器

#### v1.0.0
- 🚀 完整功能发布
- 📱 应用商店上架准备

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！

### 贡献流程

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者: [您的姓名]
- 邮箱: [您的邮箱]
- 项目地址: [GitHub链接]

## 🎓 学习要点

这个项目涵盖了Android开发的多个重要概念：

### 核心技术学习

1. **CameraX框架**
   - Use Cases设计模式 (Preview, ImageCapture, ImageAnalysis)
   - 生命周期感知的相机管理
   - 相机配置和控制
   - 异步编程和回调机制

2. **权限管理**
   - 运行时权限请求
   - 权限状态处理
   - 用户体验优化

3. **Material Design**
   - 现代UI设计原则
   - 响应式布局
   - 主题和样式系统

4. **架构设计**
   - 模块化代码组织
   - 接口和回调设计
   - 错误处理策略

5. **Android存储系统**
   - 文件存储策略选择
   - Scoped Storage概念
   - MediaStore API使用

### 开发技巧

- **类型安全**: 避免ClassCastException等常见错误
- **资源管理**: 正确的生命周期管理和资源释放
- **用户体验**: 友好的错误提示和加载状态
- **代码质量**: 清晰的注释和规范的命名
- **线程安全**: 主线程与后台线程的正确使用
- **内存管理**: 避免内存泄漏和过度消耗

### 🧠 深度技术探讨

#### 1. Android线程模型与UI更新

```kotlin
// 为什么需要切换到主线程？
// Android UI工具包不是线程安全的，只有主线程可以更新UI

// ❌ 错误：在后台线程更新UI
override fun onImageSaved(output: ImageCapture.OutputFileResults) {
    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()  // 崩溃！
}

// ✅ 正确：切换到主线程
override fun onImageSaved(output: ImageCapture.OutputFileResults) {
    ContextCompat.getMainExecutor(context).execute {
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()  // 安全
    }
}
```

**关键概念：**
- 主线程（UI线程）：负责UI更新和用户交互
- 后台线程：负责耗时操作（网络、文件IO、图像处理）
- 违反线程规则会抛出`CalledFromWrongThreadException`

#### 2. 内存管理最佳实践

```kotlin
// 潜在内存泄漏点及解决方案

// 问题1：回调持有Activity引用
class CameraManager {
    private var callback: CameraCallback? = null

    fun release() {
        callback = null  // 及时清理引用
        cameraExecutor.shutdown()
    }
}

// 问题2：大对象未及时释放
// ImageProxy包含大量图像数据，CameraX会自动处理
// 但在ImageAnalysis中需要手动调用imageProxy.close()
```

**内存管理原则：**
- 及时清理回调引用，避免Activity泄漏
- 大对象使用完毕后立即释放
- 使用弱引用处理长期持有的引用
- 在生命周期结束时清理所有资源

#### 3. 用户体验设计模式

```kotlin
// 防止重复点击的多种策略

// 策略1：按钮状态控制（当前使用）
btnCapture.isEnabled = false  // 简单有效

// 策略2：时间间隔控制
private var lastClickTime = 0L
if (System.currentTimeMillis() - lastClickTime < 2000) return

// 策略3：状态标志
private var isCapturing = false
if (isCapturing) return

// 策略4：视觉进度指示
progressBar.visibility = View.VISIBLE
btnCapture.visibility = View.GONE
```

**用户体验原则：**
- 立即反馈：用户操作后立即给出响应
- 状态明确：让用户知道当前发生了什么
- 错误友好：提供清晰的错误信息和解决建议
- 防误操作：避免用户意外的重复操作

#### 4. 可扩展架构设计

```kotlin
// 为连拍功能设计的架构扩展

class CameraManager {
    // 支持单拍和连拍的统一接口
    fun takePicture(mode: CaptureMode = CaptureMode.SINGLE) {
        when (mode) {
            CaptureMode.SINGLE -> takeSinglePicture()
            CaptureMode.BURST -> startBurstCapture()
        }
    }

    // 连拍状态管理
    private fun startBurstCapture() {
        // 批量处理逻辑
        // 减少UI更新频率
        // 优化内存使用
    }
}
```

**架构设计原则：**
- 单一职责：每个类只负责一个功能
- 开闭原则：对扩展开放，对修改关闭
- 接口隔离：提供最小必要的接口
- 依赖倒置：依赖抽象而不是具体实现

## 🐛 常见问题

### Q: 应用启动时崩溃，提示ClassCastException
**A**: 检查布局文件中的View类型与代码中的声明是否一致。例如：
```kotlin
// 错误：布局中是Button，代码中声明为ImageButton
private lateinit var btnCapture: ImageButton

// 正确：保持类型一致
private lateinit var btnCapture: Button
```

### Q: 相机预览显示黑屏
**A**: 检查以下几点：
1. 相机权限是否已授予
2. CameraX是否正确初始化
3. PreviewView是否正确绑定到Preview用例

### Q: 在某些设备上相机功能异常
**A**: CameraX会自动处理设备兼容性，但建议：
1. 测试不同品牌的设备
2. 检查设备的相机硬件支持
3. 添加适当的错误处理

### Q: 拍照时应用崩溃，提示CalledFromWrongThreadException
**A**: 这是线程安全问题，UI更新必须在主线程进行：
```kotlin
// ❌ 错误：在后台线程更新UI
override fun onImageSaved(output: ImageCapture.OutputFileResults) {
    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()  // 崩溃！
}

// ✅ 正确：切换到主线程
override fun onImageSaved(output: ImageCapture.OutputFileResults) {
    ContextCompat.getMainExecutor(context).execute {
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
    }
}
```

### Q: 应用使用一段时间后变慢或崩溃
**A**: 可能是内存泄漏问题：
1. 检查是否及时清理回调引用
2. 确保在Activity销毁时释放相机资源
3. 使用Android Studio的Memory Profiler检查内存使用
```kotlin
override fun onDestroy() {
    super.onDestroy()
    cameraManager.release()  // 释放相机资源
}
```

### Q: 用户快速点击拍照按钮导致多张照片
**A**: 实现防重复点击机制：
```kotlin
btnCapture.setOnClickListener {
    btnCapture.isEnabled = false  // 禁用按钮
    // 拍照完成后在回调中重新启用
}
```

### Q: 拍照保存失败，提示存储空间不足
**A**: 检查存储策略和错误处理：
1. 使用应用私有目录避免权限问题
2. 检查可用存储空间
3. 提供用户友好的错误提示
```kotlin
val errorMessage = when (exception.imageCaptureError) {
    ImageCapture.ERROR_FILE_IO -> "保存照片失败，请检查存储空间"
    // 其他错误类型...
}
```

## 📚 参考资料

- [CameraX官方文档](https://developer.android.com/training/camerax)
- [Material Design指南](https://material.io/design)
- [Android权限最佳实践](https://developer.android.com/training/permissions)
- [Kotlin Android开发](https://developer.android.com/kotlin)
- [项目错误记录与解决方案](ERRORS_AND_SOLUTIONS.md)

---

**注意**: 这是一个学习项目，主要用于Android开发技术的学习和实践。项目持续更新中，欢迎Star和Fork！
