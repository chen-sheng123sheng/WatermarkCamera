package com.example.watermarkcamera

import android.app.Application
import android.util.Log
import com.example.watermarkcamera.camera.CameraManager

/**
 * 水印相机应用程序类
 * 
 * 职责：
 * 1. 管理全局的相机资源，避免Activity重建时重复初始化
 * 2. 提供应用级别的单例服务
 * 3. 处理应用生命周期相关的资源管理
 * 
 * 学习要点：
 * - Application类在应用启动时创建，直到应用进程结束才销毁
 * - 适合管理需要跨Activity共享的资源
 * - 避免Activity重建导致的资源重复创建问题
 */
class WatermarkCameraApplication : Application() {
    
    companion object {
        private const val TAG = "WatermarkCameraApp"
        
        // 全局Application实例
        private lateinit var instance: WatermarkCameraApplication
        
        /**
         * 获取Application实例
         */
        fun getInstance(): WatermarkCameraApplication = instance
    }
    
    // 全局相机管理器
    private var globalCameraManager: CameraManager? = null
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Log.d(TAG, "WatermarkCameraApplication 创建")
        
        // 在Application中不立即初始化相机，而是在需要时懒加载
        // 因为相机需要Activity上下文来绑定生命周期
    }
    
    /**
     * 获取或创建相机管理器
     * 使用懒加载模式，避免不必要的资源创建
     *
     * @param context Activity上下文，用于相机生命周期绑定
     * @return 相机管理器实例
     */
    fun getCameraManager(context: android.content.Context): CameraManager {
        if (globalCameraManager == null) {
            Log.d(TAG, "创建全局相机管理器")
            globalCameraManager = CameraManager(context)
        } else {
            Log.d(TAG, "复用现有的全局相机管理器")
        }
        return globalCameraManager!!
    }

    /**
     * 检查相机管理器是否已创建
     */
    fun isCameraManagerCreated(): Boolean {
        return globalCameraManager != null
    }
    
    /**
     * 释放相机资源
     * 在应用退出或需要释放资源时调用
     */
    fun releaseCameraManager() {
        globalCameraManager?.let {
            Log.d(TAG, "释放全局相机管理器")
            it.release()
            globalCameraManager = null
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "WatermarkCameraApplication 终止")
        releaseCameraManager()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "系统内存不足")
        // 在内存不足时可以考虑释放一些非关键资源
    }
}
