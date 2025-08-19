package com.example.watermarkcamera.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 设备物理方向检测器
 * 
 * 功能：
 * 1. 使用重力传感器检测设备的物理方向
 * 2. 不依赖Activity的屏幕方向设置
 * 3. 适用于锁定屏幕方向的相机应用
 * 
 * 学习要点：
 * - 重力传感器能检测设备在三维空间中的方向
 * - 通过重力向量计算设备的倾斜角度
 * - 适合需要检测物理方向但UI保持固定的场景
 */
class DeviceOrientationDetector(
    private val context: Context,
    private val callback: OrientationCallback
) : SensorEventListener {
    
    companion object {
        private const val TAG = "DeviceOrientation"
        
        // 方向常量
        const val ORIENTATION_PORTRAIT = 0          // 竖屏
        const val ORIENTATION_LANDSCAPE_LEFT = 1    // 横屏（左转）
        const val ORIENTATION_PORTRAIT_UPSIDE = 2   // 倒立竖屏
        const val ORIENTATION_LANDSCAPE_RIGHT = 3   // 横屏（右转）
        
        // 角度阈值，避免频繁切换
        private const val ANGLE_THRESHOLD = 30f
    }
    
    interface OrientationCallback {
        fun onOrientationChanged(orientation: Int, angle: Float)
    }
    
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private var currentOrientation = ORIENTATION_PORTRAIT
    private var isListening = false
    
    /**
     * 开始监听设备方向变化
     */
    fun startListening() {
        if (gravitySensor != null && !isListening) {
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
            Log.d(TAG, "开始监听设备方向")
        } else {
            Log.w(TAG, "重力传感器不可用或已在监听")
        }
    }
    
    /**
     * 停止监听设备方向变化
     */
    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            Log.d(TAG, "停止监听设备方向")
        }
    }
    
    /**
     * 获取当前设备方向
     */
    fun getCurrentOrientation(): Int = currentOrientation
    
    /**
     * 判断当前是否为竖屏方向（包括正常竖屏和倒立竖屏）
     */
    fun isPortrait(): Boolean {
        return currentOrientation == ORIENTATION_PORTRAIT || 
               currentOrientation == ORIENTATION_PORTRAIT_UPSIDE
    }
    
    /**
     * 判断当前是否为横屏方向
     */
    fun isLandscape(): Boolean {
        return currentOrientation == ORIENTATION_LANDSCAPE_LEFT || 
               currentOrientation == ORIENTATION_LANDSCAPE_RIGHT
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            val x = event.values[0]  // X轴重力分量
            val y = event.values[1]  // Y轴重力分量
            val z = event.values[2]  // Z轴重力分量
            
            // 计算设备倾斜角度
            val angle = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
            
            // 根据角度确定方向
            val newOrientation = when {
                angle >= -45 && angle < 45 -> ORIENTATION_PORTRAIT
                angle >= 45 && angle < 135 -> ORIENTATION_LANDSCAPE_LEFT
                angle >= 135 || angle < -135 -> ORIENTATION_PORTRAIT_UPSIDE
                angle >= -135 && angle < -45 -> ORIENTATION_LANDSCAPE_RIGHT
                else -> currentOrientation
            }
            
            // 只有方向真正改变时才通知
            if (newOrientation != currentOrientation) {
                val oldOrientation = currentOrientation
                currentOrientation = newOrientation
                
                val orientationName = getOrientationName(newOrientation)
                Log.d(TAG, "设备方向变化: ${getOrientationName(oldOrientation)} -> $orientationName (角度: ${angle.toInt()}°)")
                
                callback.onOrientationChanged(newOrientation, angle)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 传感器精度变化，通常不需要处理
    }
    
    /**
     * 获取方向名称，用于日志显示
     */
    private fun getOrientationName(orientation: Int): String {
        return when (orientation) {
            ORIENTATION_PORTRAIT -> "竖屏"
            ORIENTATION_LANDSCAPE_LEFT -> "横屏左"
            ORIENTATION_PORTRAIT_UPSIDE -> "倒立竖屏"
            ORIENTATION_LANDSCAPE_RIGHT -> "横屏右"
            else -> "未知"
        }
    }
    
    /**
     * 获取简化的方向描述
     */
    fun getSimpleOrientationName(): String {
        return if (isPortrait()) "竖屏" else "横屏"
    }
}
