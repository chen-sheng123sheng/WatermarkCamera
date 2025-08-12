package com.example.watermarkcamera.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限管理工具类
 * 
 * 功能说明：
 * 1. 统一管理应用所需的所有权限
 * 2. 提供权限检查和请求的便捷方法
 * 3. 处理不同Android版本的权限差异
 * 4. 支持批量权限请求和单个权限检查
 * 
 * 使用场景：
 * - 相机功能需要CAMERA权限
 * - 保存照片需要存储权限（Android 10以下）
 * - 获取位置信息需要位置权限（如果添加位置水印功能）
 */
class PermissionManager {
    
    companion object {
        // 权限请求码 - 用于识别不同的权限请求
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val STORAGE_PERMISSION_REQUEST_CODE = 1002
        const val ALL_PERMISSIONS_REQUEST_CODE = 1003
        
        /**
         * 应用所需的所有权限列表
         * 
         * 权限说明：
         * - CAMERA: 访问摄像头，拍照和预览必需
         * - WRITE_EXTERNAL_STORAGE: 保存照片到外部存储（Android 10以下）
         * - READ_EXTERNAL_STORAGE: 读取相册照片（如果需要）
         */
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            // Android 10 (API 29) 以下需要存储权限
            // Android 10+ 使用Scoped Storage，不需要存储权限
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        
        /**
         * 检查单个权限是否已授予
         * 
         * @param context 上下文
         * @param permission 要检查的权限
         * @return true表示已授权，false表示未授权
         */
        fun hasPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context, 
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * 检查相机权限是否已授予
         * 
         * @param context 上下文
         * @return true表示已授权，false表示未授权
         */
        fun hasCameraPermission(context: Context): Boolean {
            return hasPermission(context, Manifest.permission.CAMERA)
        }
        
        /**
         * 检查存储权限是否已授予
         * 注意：Android 10+ 不需要存储权限
         * 
         * @param context 上下文
         * @return true表示已授权或不需要权限，false表示未授权
         */
        fun hasStoragePermission(context: Context): Boolean {
            // Android 10+ 使用Scoped Storage，不需要存储权限
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                return true
            }
            
            return hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                   hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        /**
         * 检查所有必需权限是否已授予
         * 
         * @param context 上下文
         * @return true表示所有权限都已授权，false表示有权限未授权
         */
        fun hasAllPermissions(context: Context): Boolean {
            return REQUIRED_PERMISSIONS.all { permission ->
                hasPermission(context, permission)
            }
        }
        
        /**
         * 获取未授权的权限列表
         * 
         * @param context 上下文
         * @return 未授权的权限数组
         */
        fun getDeniedPermissions(context: Context): Array<String> {
            return REQUIRED_PERMISSIONS.filter { permission ->
                !hasPermission(context, permission)
            }.toTypedArray()
        }
        
        /**
         * 请求相机权限
         * 
         * @param activity Activity实例
         */
        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        
        /**
         * 请求存储权限
         * 注意：Android 10+ 不需要请求存储权限
         * 
         * @param activity Activity实例
         */
        fun requestStoragePermission(activity: Activity) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
        
        /**
         * 请求所有必需权限
         * 
         * @param activity Activity实例
         */
        fun requestAllPermissions(activity: Activity) {
            val deniedPermissions = getDeniedPermissions(activity)
            if (deniedPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    activity,
                    deniedPermissions,
                    ALL_PERMISSIONS_REQUEST_CODE
                )
            }
        }
        
        /**
         * 检查是否应该显示权限说明
         * 当用户之前拒绝过权限时，系统建议显示说明
         * 
         * @param activity Activity实例
         * @param permission 权限名称
         * @return true表示应该显示说明，false表示不需要
         */
        fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
        
        /**
         * 处理权限请求结果
         * 
         * @param requestCode 请求码
         * @param permissions 权限数组
         * @param grantResults 授权结果数组
         * @return 权限处理结果
         */
        fun handlePermissionResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ): PermissionResult {
            when (requestCode) {
                CAMERA_PERMISSION_REQUEST_CODE -> {
                    val granted = grantResults.isNotEmpty() && 
                                 grantResults[0] == PackageManager.PERMISSION_GRANTED
                    return PermissionResult(
                        requestCode = requestCode,
                        isGranted = granted,
                        permission = Manifest.permission.CAMERA
                    )
                }
                
                STORAGE_PERMISSION_REQUEST_CODE -> {
                    val allGranted = grantResults.isNotEmpty() && 
                                   grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                    return PermissionResult(
                        requestCode = requestCode,
                        isGranted = allGranted,
                        permission = "STORAGE_PERMISSIONS"
                    )
                }
                
                ALL_PERMISSIONS_REQUEST_CODE -> {
                    val allGranted = grantResults.isNotEmpty() && 
                                   grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                    return PermissionResult(
                        requestCode = requestCode,
                        isGranted = allGranted,
                        permission = "ALL_PERMISSIONS"
                    )
                }
                
                else -> {
                    return PermissionResult(
                        requestCode = requestCode,
                        isGranted = false,
                        permission = "UNKNOWN"
                    )
                }
            }
        }
    }
}

/**
 * 权限请求结果数据类
 * 
 * @param requestCode 请求码
 * @param isGranted 是否已授权
 * @param permission 权限名称
 */
data class PermissionResult(
    val requestCode: Int,
    val isGranted: Boolean,
    val permission: String
)
