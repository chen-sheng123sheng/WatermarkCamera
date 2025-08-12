package com.example.watermarkcamera.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
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
    
    /**
     * 设置相机回调
     */
    fun setCameraCallback(callback: CameraCallback) {
        this.cameraCallback = callback
    }
    
    /**
     * 初始化相机
     * 
     * @param previewView 预览视图
     * @param lifecycleOwner 生命周期拥有者（通常是Activity或Fragment）
     */
    fun initializeCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
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
                imageCapture = ImageCapture.Builder()
                    .setFlashMode(flashMode)
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
     * 释放相机资源
     */
    fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        Log.d(TAG, "相机资源已释放")
    }
}

/**
 * 相机回调接口
 * 用于向UI层报告相机状态变化
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
}
