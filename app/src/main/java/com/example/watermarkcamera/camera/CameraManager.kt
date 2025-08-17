package com.example.watermarkcamera.camera

import android.content.ContentValues
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 相机管理类 - 封装CameraX的核心功能
 * 
 * 职责说明：
 * 1. 管理相机的生命周期和配置
 * 2. 提供预览、拍照、录像等功能
 * 3. 处理相机切换（前置/后置）
 * 4. 管理闪光灯和对焦功能
 * 5. 提供相机状态回调
 * 
 * CameraX架构说明：
 * - Use Cases: 定义相机的使用场景（预览、拍照、录像等）
 * - CameraProvider: 管理相机的生命周期
 * - CameraSelector: 选择要使用的相机（前置/后置）
 * - Preview: 预览用例，将相机画面显示到PreviewView
 * - ImageCapture: 拍照用例，捕获静态图像
 * - ImageAnalysis: 图像分析用例，实时分析相机画面
 */
class CameraManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CameraManager"
    }
    
    // CameraX组件
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    
    // 相机配置
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashMode = ImageCapture.FLASH_MODE_AUTO
    
    // 线程执行器 - 用于相机操作的后台线程
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    // 回调接口
    private var cameraCallback: CameraCallback? = null

    // 反馈效果组件
    // 学习要点：Android多媒体反馈系统
    // SoundPool：用于播放短音效，比MediaPlayer更适合拍照音效
    // Vibrator：用于触觉反馈，增强用户体验
    private var soundPool: SoundPool? = null
    private var shutterSoundId: Int = 0
    private var vibrator: Vibrator? = null
    
    /**
     * 设置相机回调
     */
    fun setCameraCallback(callback: CameraCallback) {
        this.cameraCallback = callback
    }

    /**
     * 初始化反馈效果系统
     *
     * 学习要点：Android多媒体反馈系统的设计原理
     * 1. SoundPool vs MediaPlayer的选择
     * 2. 音效资源的加载和管理
     * 3. 触觉反馈的适配和优化
     * 4. 系统资源的合理使用
     */
    private fun initializeFeedbackSystem(context: Context) {
        // 初始化音效系统
        // 学习要点：SoundPool的优势
        // - 专为短音效设计，延迟低
        // - 支持同时播放多个音效
        // - 内存占用小，适合频繁播放
        // - 自动处理音频焦点和音量控制

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)           // 媒体播放用途
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)  // 音效类型
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)                    // 最大同时播放流数：1个就够了
            .setAudioAttributes(audioAttributes)  // 音频属性配置
            .build()

        // 加载拍照音效
        // 暂时跳过音效加载，避免因为缺少音效文件导致的问题
        // 后续可以添加自定义音效文件到res/raw/目录
        try {
            // 暂时设置为0，表示没有音效
            shutterSoundId = 0
            Log.d(TAG, "音效系统初始化完成（暂无音效文件）")
        } catch (e: Exception) {
            Log.e(TAG, "音效系统初始化失败", e)
        }

        // 初始化震动系统
        // 学习要点：Vibrator API的使用
        // - 获取系统震动服务
        // - 检查设备是否支持震动
        // - 适配不同Android版本的震动API
        vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)

        if (vibrator?.hasVibrator() == true) {
            Log.d(TAG, "触觉反馈系统初始化完成")
        } else {
            Log.d(TAG, "设备不支持震动功能")
            vibrator = null
        }
    }
    
    /**
     * 初始化相机
     * 
     * @param previewView 预览视图
     * @param lifecycleOwner 生命周期拥有者（通常是Activity或Fragment）
     */
    fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        // 初始化反馈效果系统
        initializeFeedbackSystem(context)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                // 获取CameraProvider实例
                cameraProvider = cameraProviderFuture.get()
                
                // 构建预览用例
                preview = Preview.Builder()
                    .build()
                    .also {
                        // 将预览连接到PreviewView
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // 构建拍照用例
                // 详细配置说明：为水印相机优化的拍照参数
                imageCapture = ImageCapture.Builder()
                    // 闪光灯模式：使用当前设置的闪光灯模式
                    .setFlashMode(flashMode)

                    // 捕获模式：选择最大化质量模式
                    // 原因：水印相机用户通常希望照片质量好，因为要添加水印作为纪念
                    // 虽然会稍微增加处理时间，但画质提升明显
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)

                    // JPEG质量：设置为90%
                    // 原因：90%是质量和文件大小的最佳平衡点
                    // - 比85%质量明显更好，适合添加水印后的二次处理
                    // - 比95%文件小很多，节省存储空间和传输时间
                    // - 对于社交分享来说，90%完全够用
                    .setJpegQuality(90)

                    // 目标旋转：设置为当前屏幕方向
                    // 原因：确保照片方向正确，避免拍出来的照片需要手动旋转
                    // Surface.ROTATION_0 表示自然方向，CameraX会自动处理设备旋转
                    .setTargetRotation(Surface.ROTATION_0)

                    .build()
                
                // 绑定用例到相机
                bindCameraUseCases(lifecycleOwner)
                
                cameraCallback?.onCameraInitialized()
                Log.d(TAG, "相机初始化成功")
                
            } catch (exc: Exception) {
                Log.e(TAG, "相机初始化失败", exc)
                cameraCallback?.onCameraError("相机初始化失败: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * 绑定相机用例
     * 
     * @param lifecycleOwner 生命周期拥有者
     */
    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner) {
        val cameraProvider = this.cameraProvider ?: run {
            Log.e(TAG, "CameraProvider未初始化")
            return
        }
        
        try {
            // 解绑之前的用例
            cameraProvider.unbindAll()
            
            // 绑定用例到相机
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            
            Log.d(TAG, "相机用例绑定成功")
            
        } catch (exc: Exception) {
            Log.e(TAG, "相机用例绑定失败", exc)
            cameraCallback?.onCameraError("相机绑定失败: ${exc.message}")
        }
    }
    
    /**
     * 切换相机（前置/后置）
     * 
     * @param lifecycleOwner 生命周期拥有者
     */
    fun switchCamera(lifecycleOwner: LifecycleOwner) {
        // 切换相机选择器
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        // 重新绑定用例
        bindCameraUseCases(lifecycleOwner)
        
        val cameraType = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) "后置" else "前置"
        Log.d(TAG, "切换到${cameraType}相机")
        cameraCallback?.onCameraSwitched(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
    }
    
    /**
     * 切换闪光灯模式
     */
    fun toggleFlashMode() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
            else -> ImageCapture.FLASH_MODE_AUTO
        }
        
        // 更新ImageCapture的闪光灯设置
        imageCapture?.flashMode = flashMode
        
        val flashModeText = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> "关闭"
            ImageCapture.FLASH_MODE_AUTO -> "自动"
            ImageCapture.FLASH_MODE_ON -> "开启"
            else -> "未知"
        }
        
        Log.d(TAG, "闪光灯模式: $flashModeText")
        cameraCallback?.onFlashModeChanged(flashMode)
    }
    
    /**
     * 获取当前闪光灯模式
     */
    fun getCurrentFlashMode(): Int = flashMode
    
    /**
     * 获取当前相机类型
     */
    fun isBackCamera(): Boolean = cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
    
    /**
     * 获取ImageCapture实例（用于拍照）
     */
    fun getImageCapture(): ImageCapture? = imageCapture
    
    /**
     * 获取Camera实例（用于控制相机）
     */
    fun getCamera(): Camera? = camera
    
    /**
     * 拍照方法 - 水印相机的核心功能
     *
     * 设计思路：
     * 1. 异步执行，不阻塞UI线程
     * 2. 自动生成文件名，包含时间戳
     * 3. 保存到应用专用目录，确保权限安全
     * 4. 通过CameraCallback通知UI层结果
     *
     * @param context 上下文，用于获取文件目录
     */
    fun takePicture(context: Context) {
        // 检查ImageCapture是否已初始化
        val imageCapture = this.imageCapture
        if (imageCapture == null) {
            Log.e(TAG, "拍照失败：ImageCapture未初始化")
            cameraCallback?.onPhotoError("相机未准备就绪，请稍后再试")
            return
        }

        // 创建文件名：使用时间戳确保唯一性
        // 格式：WatermarkCamera_yyyyMMdd_HHmmss.jpg
        // 例如：WatermarkCamera_20231215_143022.jpg
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "WatermarkCamera_${timeStamp}.jpg"

        // 修复方案：使用文件路径方式，避免CameraX与MediaStore的冲突
        // 学习要点：CameraX与MediaStore的兼容性问题
        // 问题：直接使用MediaStore Uri会导致CameraX内部重复插入ContentResolver
        // 解决：先保存到临时文件，成功后手动添加到MediaStore

        // 创建临时文件路径
        val outputDirectory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: 使用应用私有目录作为临时存储
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp")
        } else {
            // Android 9-: 直接保存到目标目录
            val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            File(dcimDir, "WatermarkCamera")
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        val outputFile = File(outputDirectory, fileName)
        Log.d(TAG, "准备保存到临时文件：${outputFile.absolutePath}")

        // 创建文件输出配置
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        // 播放拍照反馈效果
        // 学习要点：反馈时机的重要性
        // 在拍照开始时立即播放反馈，让用户知道操作已响应
        // 这比等待拍照完成再反馈的用户体验更好
        playShutterFeedback()

        // 执行拍照
        // 学习要点：CameraX异步编程模式
        // - imageCapture.takePicture()立即返回，不阻塞当前线程
        // - 实际拍照在CameraX的后台线程执行
        // - 完成后在cameraExecutor线程调用回调
        // - 我们需要切换回主线程更新UI
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "文件保存成功，开始添加到MediaStore")

                    // 手动添加文件到MediaStore
                    // 学习要点：解决CameraX与MediaStore冲突的方法
                    try {
                        val contentResolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                            put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // Android 10+: 使用相对路径
                                put(MediaStore.Images.Media.RELATIVE_PATH,
                                    "${Environment.DIRECTORY_DCIM}/WatermarkCamera")
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                            } else {
                                // Android 9-: 文件已经在正确位置，直接设置路径
                                put(MediaStore.Images.Media.DATA, outputFile.absolutePath)
                            }
                        }

                        // 插入MediaStore记录
                        val mediaUri = contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )

                        if (mediaUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Android 10+: 复制文件到MediaStore位置
                            contentResolver.openOutputStream(mediaUri)?.use { outputStream ->
                                outputFile.inputStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }

                            // 清除IS_PENDING标志
                            val updateValues = ContentValues().apply {
                                put(MediaStore.Images.Media.IS_PENDING, 0)
                            }
                            contentResolver.update(mediaUri, updateValues, null, null)

                            // 删除临时文件
                            outputFile.delete()

                            Log.d(TAG, "文件已添加到MediaStore: $mediaUri")
                        }

                        val displayPath = "相册/WatermarkCamera/$fileName"
                        Log.d(TAG, "拍照成功，保存到：$displayPath")

                        // 切换到主线程执行回调
                        ContextCompat.getMainExecutor(context).execute {
                            cameraCallback?.onPhotoSaved(displayPath)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "添加到MediaStore失败", e)
                        // 即使MediaStore失败，文件也已保存，告知用户
                        val filePath = outputFile.absolutePath
                        ContextCompat.getMainExecutor(context).execute {
                            cameraCallback?.onPhotoSaved("文件已保存：$filePath")
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "拍照失败", exception)

                    // 根据错误类型提供用户友好的错误信息
                    val errorMessage = when (exception.imageCaptureError) {
                        ImageCapture.ERROR_CAMERA_CLOSED -> "相机已关闭，请重新打开应用"
                        ImageCapture.ERROR_CAPTURE_FAILED -> "拍照失败，请重试"
                        ImageCapture.ERROR_FILE_IO -> "保存照片失败，请检查存储空间"
                        ImageCapture.ERROR_INVALID_CAMERA -> "相机配置错误，请重启应用"
                        else -> "拍照失败：${exception.message}"
                    }

                    // 切换到主线程执行错误回调
                    ContextCompat.getMainExecutor(context).execute {
                        cameraCallback?.onPhotoError(errorMessage)
                    }
                }
            }
        )

        Log.d(TAG, "开始拍照：$fileName")
    }

    /**
     * 播放拍照反馈效果
     *
     * 学习要点：多种反馈方式的协调使用
     * 1. 音效反馈：给用户听觉确认
     * 2. 触觉反馈：给用户触觉确认
     * 3. 时机控制：在拍照开始时播放，给用户即时反馈
     * 4. 用户体验：让用户明确知道拍照操作已执行
     */
    private fun playShutterFeedback() {
        // 播放拍照音效
        // 学习要点：SoundPool的播放参数
        if (shutterSoundId != 0) {
            soundPool?.play(
                shutterSoundId,    // 音效ID
                1.0f,              // 左声道音量 (0.0-1.0)
                1.0f,              // 右声道音量 (0.0-1.0)
                1,                 // 优先级 (0=最低优先级)
                0,                 // 循环次数 (0=不循环, -1=无限循环)
                1.0f               // 播放速率 (0.5-2.0, 1.0=正常速度)
            )
            Log.d(TAG, "播放拍照音效")
        }

        // 播放触觉反馈
        // 学习要点：Vibrator API的版本适配
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 使用VibrationEffect
                // 创建一次性震动效果：震动50毫秒，强度为默认
                val vibrationEffect = VibrationEffect.createOneShot(
                    50,                                    // 震动时长(毫秒)
                    VibrationEffect.DEFAULT_AMPLITUDE     // 震动强度
                )
                vib.vibrate(vibrationEffect)
            } else {
                // Android 7.1及以下使用传统API
                @Suppress("DEPRECATION")
                vib.vibrate(50)  // 震动50毫秒
            }
            Log.d(TAG, "播放触觉反馈")
        }
    }

    /**
     * 释放相机资源和反馈系统资源
     *
     * 学习要点：Android资源管理的重要性
     * 1. 及时释放系统资源，避免内存泄漏
     * 2. SoundPool需要手动释放，否则会占用音频资源
     * 3. 线程池需要正确关闭，避免线程泄漏
     * 4. 良好的资源管理是Android应用稳定性的基础
     */
    fun release() {
        // 释放相机资源
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()

        // 释放音效资源
        // 学习要点：SoundPool资源释放
        soundPool?.release()
        soundPool = null

        // 清理震动引用
        vibrator = null

        Log.d(TAG, "相机和反馈系统资源已释放")
    }
}

/**
 * 相机回调接口
 * 用于向UI层报告相机状态变化和拍照结果
 */
interface CameraCallback {
    /**
     * 相机初始化完成
     */
    fun onCameraInitialized()

    /**
     * 相机发生错误
     * @param error 错误信息
     */
    fun onCameraError(error: String)

    /**
     * 相机切换完成
     * @param isBackCamera true表示后置相机，false表示前置相机
     */
    fun onCameraSwitched(isBackCamera: Boolean)

    /**
     * 闪光灯模式改变
     * @param flashMode 新的闪光灯模式
     */
    fun onFlashModeChanged(flashMode: Int)

    /**
     * 拍照成功
     * @param filePath 保存的文件路径
     */
    fun onPhotoSaved(filePath: String)

    /**
     * 拍照失败
     * @param error 错误信息
     */
    fun onPhotoError(error: String)
}
