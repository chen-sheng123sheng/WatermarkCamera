package com.example.watermarkcamera.watermark

import android.content.Context
import android.graphics.*
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * 水印渲染引擎
 * 
 * 设计思路：
 * 1. 负责将水印数据渲染到Bitmap上
 * 2. 支持多种水印类型的渲染
 * 3. 使用Canvas进行高质量的图形绘制
 * 4. 考虑性能优化和内存管理
 * 
 * 学习要点：
 * - Android Canvas和Paint的使用
 * - Bitmap操作和内存管理
 * - 图形绘制的性能优化
 * - 坐标系统和比例计算
 */
class WatermarkRenderer(private val context: Context) {
    
    private val TAG = "WatermarkRenderer"
    
    // 绘制工具
    private val textPaint = Paint().apply {
        isAntiAlias = true          // 抗锯齿
        textAlign = Paint.Align.LEFT // 文字对齐方式
        typeface = Typeface.DEFAULT_BOLD // 粗体字体
    }
    
    private val shadowPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }
    
    /**
     * 在图片上渲染水印
     * 
     * 学习要点：Bitmap的可变性和内存管理
     * - 创建可变的Bitmap副本进行绘制
     * - 使用Canvas进行图形操作
     * - 正确处理资源释放
     * 
     * @param originalBitmap 原始图片
     * @param watermarks 要渲染的水印列表
     * @return 添加水印后的图片
     */
    fun renderWatermarks(originalBitmap: Bitmap, watermarks: List<WatermarkData>): Bitmap {
        Log.d(TAG, "开始渲染水印，水印数量：${watermarks.size}")
        Log.d(TAG, "原始Bitmap尺寸: ${originalBitmap.width} x ${originalBitmap.height}")

        // 创建可变的Bitmap副本
        // 学习要点：为什么需要创建副本？
        // 1. 原始Bitmap可能是不可变的
        // 2. 避免修改原始图片数据
        // 3. 支持撤销操作
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        Log.d(TAG, "可变Bitmap尺寸: ${mutableBitmap.width} x ${mutableBitmap.height}")
        val canvas = Canvas(mutableBitmap)
        
        // 渲染每个启用的水印
        watermarks.filter { it.isEnabled && it.isValid() }.forEach { watermark ->
            try {
                when (watermark.type) {
                    WatermarkType.TEXT -> renderTextWatermark(canvas, mutableBitmap, watermark)
                    WatermarkType.TIMESTAMP -> renderTimestampWatermark(canvas, mutableBitmap, watermark)
                    WatermarkType.LOCATION -> renderLocationWatermark(canvas, mutableBitmap, watermark)
                    WatermarkType.IMAGE -> renderImageWatermark(canvas, mutableBitmap, watermark)
                }
                Log.d(TAG, "成功渲染${watermark.type.displayName}")
            } catch (e: Exception) {
                Log.e(TAG, "渲染${watermark.type.displayName}失败", e)
            }
        }
        
        Log.d(TAG, "水印渲染完成")
        return mutableBitmap
    }
    
    /**
     * 渲染文字水印
     * 
     * 学习要点：文字绘制的技术细节
     * - 字体大小的计算和适配
     * - 文字位置的精确控制
     * - 阴影效果的实现
     * 
     * @param canvas 画布
     * @param bitmap 目标图片
     * @param watermark 水印数据
     */
    private fun renderTextWatermark(canvas: Canvas, bitmap: Bitmap, watermark: WatermarkData) {
        // 计算字体大小
        // 学习要点：比例计算的重要性
        // 使用图片宽度的比例确保在不同分辨率下的一致性
        val textSize = bitmap.width * watermark.size
        
        // 配置文字画笔
        textPaint.apply {
            this.textSize = textSize
            color = watermark.textColor
            alpha = watermark.alpha
        }
        
        // 计算文字位置
        // 学习要点：坐标系统转换
        // 从比例坐标(0.0-1.0)转换为像素坐标
        val x = bitmap.width * watermark.position.x
        val y = bitmap.height * watermark.position.y
        
        // 绘制阴影（如果启用）
        if (watermark.hasShadow) {
            shadowPaint.apply {
                this.textSize = textSize
                color = watermark.shadowColor
                alpha = watermark.alpha / 2 // 阴影透明度为主文字的一半
            }
            
            // 阴影偏移
            val shadowOffset = textSize * 0.02f
            canvas.drawText(watermark.content, x + shadowOffset, y + shadowOffset, shadowPaint)
        }
        
        // 绘制主文字
        canvas.drawText(watermark.content, x, y, textPaint)
    }
    
    /**
     * 渲染时间水印
     * 
     * 学习要点：时间格式化和动态内容生成
     * - SimpleDateFormat的使用
     * - 当前时间的获取
     * - 格式字符串的处理
     * 
     * @param canvas 画布
     * @param bitmap 目标图片
     * @param watermark 水印数据
     */
    private fun renderTimestampWatermark(canvas: Canvas, bitmap: Bitmap, watermark: WatermarkData) {
        // 生成当前时间字符串
        // 学习要点：时间格式化的最佳实践
        val timeFormat = if (watermark.content.isNotBlank()) {
            watermark.content
        } else {
            "yyyy-MM-dd HH:mm:ss" // 默认格式
        }
        
        val currentTime = try {
            SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
        } catch (e: Exception) {
            Log.e(TAG, "时间格式错误：$timeFormat", e)
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        }
        
        // 创建临时水印数据用于渲染
        val timestampWatermark = watermark.copy(content = currentTime)
        renderTextWatermark(canvas, bitmap, timestampWatermark)
    }
    
    /**
     * 渲染位置水印
     * 
     * 注意：这是位置水印的基础实现
     * 完整实现需要GPS权限和位置服务
     * 
     * @param canvas 画布
     * @param bitmap 目标图片
     * @param watermark 水印数据
     */
    private fun renderLocationWatermark(canvas: Canvas, bitmap: Bitmap, watermark: WatermarkData) {
        // 临时实现：显示占位符文字
        // TODO: 在后续版本中集成真实的位置服务
        val locationText = if (watermark.content.isNotBlank()) {
            watermark.content
        } else {
            "位置获取中..." // 占位符
        }
        
        val locationWatermark = watermark.copy(content = locationText)
        renderTextWatermark(canvas, bitmap, locationWatermark)
    }
    
    /**
     * 渲染图片水印
     * 
     * 注意：这是图片水印的基础实现
     * 完整实现需要图片加载和缩放处理
     * 
     * @param canvas 画布
     * @param bitmap 目标图片
     * @param watermark 水印数据
     */
    private fun renderImageWatermark(canvas: Canvas, bitmap: Bitmap, watermark: WatermarkData) {
        // 临时实现：显示占位符文字
        // TODO: 在后续版本中实现真实的图片水印
        val placeholderText = "图片水印"
        val imageWatermark = watermark.copy(
            content = placeholderText,
            type = WatermarkType.TEXT // 临时使用文字渲染
        )
        renderTextWatermark(canvas, bitmap, imageWatermark)
    }
    
    /**
     * 计算文字的边界矩形
     * 用于水印位置调整和碰撞检测
     * 
     * @param text 文字内容
     * @param textSize 字体大小
     * @return 文字边界矩形
     */
    fun getTextBounds(text: String, textSize: Float): Rect {
        textPaint.textSize = textSize
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }
    
    /**
     * 预览水印效果（不修改原图）
     * 用于实时预览功能
     *
     * @param originalBitmap 原始图片
     * @param watermark 要预览的水印
     * @return 预览图片
     */
    fun previewWatermark(originalBitmap: Bitmap, watermark: WatermarkData): Bitmap {
        return renderWatermarks(originalBitmap, listOf(watermark))
    }

    /**
     * 释放资源
     * 在不需要渲染器时调用，释放Paint等资源
     */
    fun release() {
        // Paint对象通常不需要显式释放，但可以在这里清理其他资源
        Log.d(TAG, "WatermarkRenderer资源已释放")
    }
}
