package com.example.watermarkcamera.watermark

/**
 * 水印类型枚举
 * 
 * 设计思路：
 * 1. 使用枚举定义支持的水印类型，确保类型安全
 * 2. 每个类型包含显示名称，便于UI展示
 * 3. 可扩展设计，便于添加新的水印类型
 * 
 * 学习要点：
 * - 枚举在Android开发中的最佳实践
 * - 类型安全的重要性
 * - 可扩展架构的设计原则
 */
enum class WatermarkType(val displayName: String) {
    /**
     * 文字水印
     * 用户可以输入自定义文字作为水印
     * 支持字体、颜色、大小等属性设置
     */
    TEXT("文字水印"),
    
    /**
     * 时间水印
     * 自动生成当前拍照时间作为水印
     * 支持多种时间格式和显示样式
     */
    TIMESTAMP("时间水印"),
    
    /**
     * 位置水印
     * 基于GPS获取当前地理位置作为水印
     * 需要位置权限，支持地址解析
     */
    LOCATION("位置水印"),
    
    /**
     * 图片水印
     * 用户选择的图片或预设Logo作为水印
     * 支持透明度、缩放等属性调整
     */
    IMAGE("图片水印");
    
    companion object {
        /**
         * 根据显示名称获取水印类型
         * 
         * @param displayName 显示名称
         * @return 对应的水印类型，如果未找到返回null
         */
        fun fromDisplayName(displayName: String): WatermarkType? {
            return values().find { it.displayName == displayName }
        }
        
        /**
         * 获取所有水印类型的显示名称列表
         * 用于UI中的选择器
         * 
         * @return 显示名称列表
         */
        fun getDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}
