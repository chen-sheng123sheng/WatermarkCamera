package com.example.watermarkcamera.watermark

import android.content.Context
import android.graphics.Bitmap
import android.util.Log

/**
 * 水印管理器
 * 
 * 设计思路：
 * 1. 统一管理所有水印相关操作
 * 2. 提供简单易用的API接口
 * 3. 处理水印的创建、编辑、应用和保存
 * 4. 管理水印配置的持久化
 * 
 * 学习要点：
 * - 管理器模式的设计原则
 * - 单一职责原则的应用
 * - API设计的用户友好性
 * - 状态管理和数据持久化
 */
class WatermarkManager(private val context: Context) {
    
    private val TAG = "WatermarkManager"
    
    // 水印渲染引擎
    private val renderer = WatermarkRenderer(context)
    
    // 当前活动的水印列表
    private val activeWatermarks = mutableListOf<WatermarkData>()
    
    // 默认水印配置
    private var defaultWatermarks: List<WatermarkData> = emptyList()
    
    init {
        // 初始化默认水印
        initializeDefaultWatermarks()
        Log.d(TAG, "WatermarkManager初始化完成")
    }
    
    /**
     * 初始化默认水印配置
     * 
     * 学习要点：合理的默认配置设计
     * - 提供开箱即用的体验
     * - 展示应用的主要功能
     * - 避免空白状态的困扰
     */
    private fun initializeDefaultWatermarks() {
        defaultWatermarks = listOf(
            // 默认文字水印：应用名称
            WatermarkData.createTextWatermark("水印相机"),
            
            // 默认时间水印：拍照时间
            WatermarkData.createTimestampWatermark("yyyy-MM-dd HH:mm")
        )
        
        // 将默认水印添加到活动列表
        activeWatermarks.addAll(defaultWatermarks)
        
        Log.d(TAG, "默认水印配置已加载，数量：${defaultWatermarks.size}")
    }
    
    /**
     * 应用水印到图片
     * 
     * 这是水印管理器的核心功能
     * 
     * @param originalBitmap 原始图片
     * @return 添加水印后的图片
     */
    fun applyWatermarks(originalBitmap: Bitmap): Bitmap {
        Log.d(TAG, "开始应用水印到图片")
        
        if (activeWatermarks.isEmpty()) {
            Log.d(TAG, "没有活动水印，返回原图")
            return originalBitmap
        }
        
        return try {
            val result = renderer.renderWatermarks(originalBitmap, activeWatermarks)
            Log.d(TAG, "水印应用成功")
            result
        } catch (e: Exception) {
            Log.e(TAG, "水印应用失败", e)
            // 如果水印应用失败，返回原图
            originalBitmap
        }
    }
    
    /**
     * 添加水印
     * 
     * @param watermark 要添加的水印
     */
    fun addWatermark(watermark: WatermarkData) {
        if (watermark.isValid()) {
            activeWatermarks.add(watermark)
            Log.d(TAG, "添加水印：${watermark.type.displayName}")
        } else {
            Log.w(TAG, "水印数据无效，添加失败：${watermark.type.displayName}")
        }
    }
    
    /**
     * 移除水印
     * 
     * @param index 要移除的水印索引
     */
    fun removeWatermark(index: Int) {
        if (index in 0 until activeWatermarks.size) {
            val removed = activeWatermarks.removeAt(index)
            Log.d(TAG, "移除水印：${removed.type.displayName}")
        } else {
            Log.w(TAG, "无效的水印索引：$index")
        }
    }
    
    /**
     * 更新水印
     * 
     * @param index 要更新的水印索引
     * @param watermark 新的水印数据
     */
    fun updateWatermark(index: Int, watermark: WatermarkData) {
        if (index in 0 until activeWatermarks.size && watermark.isValid()) {
            activeWatermarks[index] = watermark
            Log.d(TAG, "更新水印：${watermark.type.displayName}")
        } else {
            Log.w(TAG, "更新水印失败，索引：$index")
        }
    }
    
    /**
     * 获取所有活动水印
     * 
     * @return 水印列表的副本（防止外部修改）
     */
    fun getActiveWatermarks(): List<WatermarkData> {
        return activeWatermarks.toList()
    }
    
    /**
     * 获取指定类型的水印
     * 
     * @param type 水印类型
     * @return 指定类型的水印列表
     */
    fun getWatermarksByType(type: WatermarkType): List<WatermarkData> {
        return activeWatermarks.filter { it.type == type }
    }
    
    /**
     * 启用/禁用水印
     * 
     * @param index 水印索引
     * @param enabled 是否启用
     */
    fun setWatermarkEnabled(index: Int, enabled: Boolean) {
        if (index in 0 until activeWatermarks.size) {
            activeWatermarks[index] = activeWatermarks[index].copy(isEnabled = enabled)
            Log.d(TAG, "水印${if (enabled) "启用" else "禁用"}：${activeWatermarks[index].type.displayName}")
        }
    }
    
    /**
     * 清除所有水印
     */
    fun clearAllWatermarks() {
        activeWatermarks.clear()
        Log.d(TAG, "已清除所有水印")
    }
    
    /**
     * 重置为默认水印
     */
    fun resetToDefault() {
        activeWatermarks.clear()
        activeWatermarks.addAll(defaultWatermarks)
        Log.d(TAG, "已重置为默认水印配置")
    }
    
    /**
     * 预览单个水印效果
     * 
     * @param originalBitmap 原始图片
     * @param watermark 要预览的水印
     * @return 预览图片
     */
    fun previewWatermark(originalBitmap: Bitmap, watermark: WatermarkData): Bitmap {
        return renderer.previewWatermark(originalBitmap, watermark)
    }
    
    /**
     * 检查是否有启用的水印
     * 
     * @return 如果有启用的水印返回true
     */
    fun hasEnabledWatermarks(): Boolean {
        return activeWatermarks.any { it.isEnabled }
    }
    
    /**
     * 获取启用的水印数量
     * 
     * @return 启用的水印数量
     */
    fun getEnabledWatermarkCount(): Int {
        return activeWatermarks.count { it.isEnabled }
    }
    
    /**
     * 创建快速文字水印
     * 便捷方法，用于快速添加文字水印
     * 
     * @param text 水印文字
     * @param addToActive 是否添加到活动列表
     * @return 创建的水印数据
     */
    fun createQuickTextWatermark(text: String, addToActive: Boolean = true): WatermarkData {
        val watermark = WatermarkData.createTextWatermark(text)
        if (addToActive) {
            addWatermark(watermark)
        }
        return watermark
    }
    
    /**
     * 创建快速时间水印
     * 便捷方法，用于快速添加时间水印
     * 
     * @param format 时间格式
     * @param addToActive 是否添加到活动列表
     * @return 创建的水印数据
     */
    fun createQuickTimestampWatermark(format: String = "yyyy-MM-dd HH:mm", addToActive: Boolean = true): WatermarkData {
        val watermark = WatermarkData.createTimestampWatermark(format)
        if (addToActive) {
            addWatermark(watermark)
        }
        return watermark
    }
    
    /**
     * 释放资源
     * 在不需要管理器时调用
     */
    fun release() {
        renderer.release()
        activeWatermarks.clear()
        Log.d(TAG, "WatermarkManager资源已释放")
    }
}
