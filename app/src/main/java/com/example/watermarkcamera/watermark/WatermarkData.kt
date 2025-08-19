package com.example.watermarkcamera.watermark

import android.graphics.Color
import android.graphics.PointF

/**
 * 水印数据模型
 * 
 * 设计思路：
 * 1. 使用data class确保数据的不可变性和线程安全
 * 2. 包含所有水印类型的通用属性
 * 3. 使用合理的默认值，简化水印创建
 * 4. 支持序列化，便于保存和加载配置
 * 
 * 学习要点：
 * - Kotlin data class的优势和使用场景
 * - 不可变数据模型的设计原则
 * - Android图形系统的坐标和颜色处理
 */
data class WatermarkData(
    /**
     * 水印类型
     */
    val type: WatermarkType,
    
    /**
     * 水印内容
     * - 文字水印：用户输入的文字
     * - 时间水印：时间格式字符串
     * - 位置水印：位置信息
     * - 图片水印：图片路径或资源ID
     */
    val content: String = "",
    
    /**
     * 水印位置（相对于图片的比例坐标）
     * 使用比例坐标(0.0-1.0)而不是绝对像素，确保在不同分辨率下的一致性
     * x: 0.0=左边缘, 1.0=右边缘
     * y: 0.0=顶部, 1.0=底部
     */
    val position: PointF = PointF(0.1f, 0.9f), // 默认左下角
    
    /**
     * 水印大小（相对于图片宽度的比例）
     * 使用比例大小确保在不同分辨率下的适配
     * 0.05 = 图片宽度的5%
     */
    val size: Float = 0.05f,
    
    /**
     * 水印透明度 (0-255)
     * 0: 完全透明
     * 255: 完全不透明
     */
    val alpha: Int = 200,
    
    /**
     * 水印旋转角度 (度)
     * 0: 不旋转
     * 正值: 顺时针旋转
     * 负值: 逆时针旋转
     */
    val rotation: Float = 0f,
    
    /**
     * 文字颜色 (仅文字和时间水印使用)
     * 使用Android Color类的颜色值
     */
    val textColor: Int = Color.WHITE,
    
    /**
     * 是否启用阴影效果
     * 增强水印在复杂背景下的可读性
     */
    val hasShadow: Boolean = true,
    
    /**
     * 阴影颜色
     */
    val shadowColor: Int = Color.BLACK,
    
    /**
     * 是否启用此水印
     * 允许用户临时禁用某个水印而不删除配置
     */
    val isEnabled: Boolean = true
) {
    
    companion object {
        /**
         * 创建默认的文字水印
         * 
         * @param text 水印文字
         * @return 文字水印数据
         */
        fun createTextWatermark(text: String = "水印相机"): WatermarkData {
            return WatermarkData(
                type = WatermarkType.TEXT,
                content = text,
                position = PointF(0.05f, 0.95f), // 左下角
                size = 0.04f,
                textColor = Color.WHITE,
                hasShadow = true
            )
        }
        
        /**
         * 创建默认的时间水印
         * 
         * @param format 时间格式
         * @return 时间水印数据
         */
        fun createTimestampWatermark(format: String = "yyyy-MM-dd HH:mm:ss"): WatermarkData {
            return WatermarkData(
                type = WatermarkType.TIMESTAMP,
                content = format,
                position = PointF(0.05f, 0.05f), // 左上角
                size = 0.03f,
                textColor = Color.WHITE,
                hasShadow = true
            )
        }
        
        /**
         * 创建默认的位置水印
         * 
         * @return 位置水印数据
         */
        fun createLocationWatermark(): WatermarkData {
            return WatermarkData(
                type = WatermarkType.LOCATION,
                content = "", // 运行时获取
                position = PointF(0.95f, 0.95f), // 右下角
                size = 0.03f,
                textColor = Color.WHITE,
                hasShadow = true
            )
        }
        
        /**
         * 创建默认的图片水印
         * 
         * @param imagePath 图片路径
         * @return 图片水印数据
         */
        fun createImageWatermark(imagePath: String): WatermarkData {
            return WatermarkData(
                type = WatermarkType.IMAGE,
                content = imagePath,
                position = PointF(0.95f, 0.05f), // 右上角
                size = 0.1f, // 图片水印通常比文字大
                alpha = 180 // 图片水印通常更透明
            )
        }
    }
    
    /**
     * 验证水印数据的有效性
     * 
     * @return 如果数据有效返回true，否则返回false
     */
    fun isValid(): Boolean {
        return when {
            // 位置坐标必须在有效范围内
            position.x < 0f || position.x > 1f -> false
            position.y < 0f || position.y > 1f -> false
            
            // 大小必须为正值且不能太大
            size <= 0f || size > 0.5f -> false
            
            // 透明度必须在有效范围内
            alpha < 0 || alpha > 255 -> false
            
            // 文字和时间水印必须有内容
            (type == WatermarkType.TEXT || type == WatermarkType.TIMESTAMP) && content.isBlank() -> false
            
            // 图片水印必须有有效路径
            type == WatermarkType.IMAGE && content.isBlank() -> false
            
            else -> true
        }
    }
    
    /**
     * 复制水印数据并修改位置
     * 
     * @param newPosition 新位置
     * @return 新的水印数据
     */
    fun withPosition(newPosition: PointF): WatermarkData {
        return copy(position = newPosition)
    }
    
    /**
     * 复制水印数据并修改大小
     * 
     * @param newSize 新大小
     * @return 新的水印数据
     */
    fun withSize(newSize: Float): WatermarkData {
        return copy(size = newSize)
    }
    
    /**
     * 复制水印数据并修改透明度
     * 
     * @param newAlpha 新透明度
     * @return 新的水印数据
     */
    fun withAlpha(newAlpha: Int): WatermarkData {
        return copy(alpha = newAlpha)
    }
}
