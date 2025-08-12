package com.example.watermarkcamera

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.watermarkcamera.camera.CameraCallback
import com.example.watermarkcamera.camera.CameraManager
import com.example.watermarkcamera.utils.PermissionManager

/**
 * 主Activity - 水印相机的核心界面
 *
 * 功能职责：
 * 1. 管理应用权限请求和处理
 * 2. 初始化相机预览和拍照功能
 * 3. 处理UI交互事件
 * 4. 管理水印编辑面板
 *
 * 生命周期管理：
 * - onCreate: 初始化UI和权限检查
 * - onResume: 检查权限状态，启动相机
 * - onPause: 暂停相机预览
 * - onDestroy: 释放资源
 */
class MainActivity : AppCompatActivity(), CameraCallback {

    // 标签用于日志输出
    private val TAG = "MainActivity"

    // UI组件
    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button  // 拍照按钮使用Button类型
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var btnFlash: ImageButton

    // 相机管理器
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示，适配全面屏
        enableEdgeToEdge()

        // 设置布局
        setContentView(R.layout.activity_main)

        // 处理系统栏内边距，确保内容不被状态栏和导航栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化UI组件
        initializeViews()

        // 初始化相机管理器
        cameraManager = CameraManager(this).apply {
            setCameraCallback(this@MainActivity)
        }

        // 检查并请求必要权限
        checkAndRequestPermissions()
    }

    /**
     * 初始化UI组件
     */
    private fun initializeViews() {
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnFlash = findViewById(R.id.btnFlash)

        // 设置按钮点击事件
        setupClickListeners()
    }

    /**
     * 设置按钮点击事件
     */
    private fun setupClickListeners() {
        // 拍照按钮
        btnCapture.setOnClickListener {
            // TODO: 在下一个任务中实现拍照功能
            Toast.makeText(this, "拍照功能即将实现", Toast.LENGTH_SHORT).show()
        }

        // 切换相机按钮
        btnSwitchCamera.setOnClickListener {
            cameraManager.switchCamera(this)
        }

        // 闪光灯按钮
        btnFlash.setOnClickListener {
            cameraManager.toggleFlashMode()
        }
    }

    /**
     * 检查并请求应用所需的权限
     *
     * 权限策略：
     * 1. 首先检查是否已有所有权限
     * 2. 如果缺少权限，显示说明对话框
     * 3. 用户确认后请求权限
     */
    private fun checkAndRequestPermissions() {
        if (PermissionManager.hasAllPermissions(this)) {
            // 所有权限都已授予，可以初始化相机
            initializeCamera()
        } else {
            // 缺少权限，需要请求
            showPermissionExplanationDialog()
        }
    }

    /**
     * 显示权限说明对话框
     *
     * 用户体验考虑：
     * 1. 解释为什么需要这些权限
     * 2. 让用户了解权限的用途
     * 3. 提供明确的操作选择
     */
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.permission_required_message))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                // 用户同意，请求权限
                PermissionManager.requestAllPermissions(this)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // 用户拒绝，显示提示并关闭应用
                showPermissionDeniedMessage()
            }
            .setCancelable(false) // 防止用户点击外部关闭对话框
            .show()
    }

    /**
     * 显示权限被拒绝的提示信息
     */
    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            getString(R.string.permission_denied_message),
            Toast.LENGTH_LONG
        ).show()

        // 延迟关闭应用，让用户看到提示信息
        findViewById<android.view.View>(android.R.id.content).postDelayed({
            finish()
        }, 2000)
    }

    /**
     * 初始化相机功能
     *
     * 注意：只有在获得所有必要权限后才调用此方法
     */
    private fun initializeCamera() {
        Log.d(TAG, "开始初始化相机")
        cameraManager.initializeCamera(previewView, this)
    }

    /**
     * 处理权限请求结果
     *
     * 系统回调方法，当用户对权限请求做出响应时调用
     *
     * @param requestCode 请求码，用于识别是哪个权限请求
     * @param permissions 请求的权限数组
     * @param grantResults 授权结果数组
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 使用PermissionManager处理权限结果
        val result = PermissionManager.handlePermissionResult(requestCode, permissions, grantResults)

        when (result.requestCode) {
            PermissionManager.ALL_PERMISSIONS_REQUEST_CODE -> {
                if (result.isGranted) {
                    // 所有权限都已授予
                    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                    initializeCamera()
                } else {
                    // 有权限被拒绝
                    handlePermissionDenied()
                }
            }

            PermissionManager.CAMERA_PERMISSION_REQUEST_CODE -> {
                if (result.isGranted) {
                    // 相机权限已授予，检查是否还需要其他权限
                    if (PermissionManager.hasAllPermissions(this)) {
                        initializeCamera()
                    } else {
                        // 还需要其他权限
                        PermissionManager.requestAllPermissions(this)
                    }
                } else {
                    handlePermissionDenied()
                }
            }
        }
    }

    /**
     * 处理权限被拒绝的情况
     *
     * 策略：
     * 1. 检查是否应该显示权限说明
     * 2. 如果用户选择"不再询问"，引导用户到设置页面
     * 3. 否则显示重新请求的选项
     */
    private fun handlePermissionDenied() {
        val deniedPermissions = PermissionManager.getDeniedPermissions(this)

        // 检查是否有权限被永久拒绝（用户选择了"不再询问"）
        val permanentlyDenied = deniedPermissions.any { permission ->
            !PermissionManager.shouldShowRequestPermissionRationale(this, permission)
        }

        if (permanentlyDenied) {
            // 有权限被永久拒绝，引导用户到设置页面
            showGoToSettingsDialog()
        } else {
            // 权限被临时拒绝，可以重新请求
            showPermissionExplanationDialog()
        }
    }

    /**
     * 显示引导用户到设置页面的对话框
     */
    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_permanently_denied_title))
            .setMessage(getString(R.string.permission_permanently_denied_message))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                // TODO: 打开应用设置页面
                Toast.makeText(this, "请在设置中手动开启权限", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                showPermissionDeniedMessage()
            }
            .setCancelable(false)
            .show()
    }

    // ========== CameraCallback接口实现 ==========

    /**
     * 相机初始化完成回调
     */
    override fun onCameraInitialized() {
        Log.d(TAG, "相机初始化完成")
        runOnUiThread {
            Toast.makeText(this, "相机已就绪", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 相机错误回调
     */
    override fun onCameraError(error: String) {
        Log.e(TAG, "相机错误: $error")
        runOnUiThread {
            Toast.makeText(this, "相机错误: $error", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 相机切换完成回调
     */
    override fun onCameraSwitched(isBackCamera: Boolean) {
        val cameraType = if (isBackCamera) "后置" else "前置"
        Log.d(TAG, "已切换到${cameraType}相机")
        runOnUiThread {
            Toast.makeText(this, "已切换到${cameraType}相机", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 闪光灯模式改变回调
     */
    override fun onFlashModeChanged(flashMode: Int) {
        val flashModeText = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> "关闭"
            ImageCapture.FLASH_MODE_AUTO -> "自动"
            ImageCapture.FLASH_MODE_ON -> "开启"
            else -> "未知"
        }

        Log.d(TAG, "闪光灯模式: $flashModeText")
        runOnUiThread {
            // 更新闪光灯按钮图标
            updateFlashButtonIcon(flashMode)
            Toast.makeText(this, "闪光灯: $flashModeText", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 更新闪光灯按钮图标
     */
    private fun updateFlashButtonIcon(flashMode: Int) {
        val iconRes = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_flash_auto // 临时使用，后续可添加专门的图标
            ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_auto // 临时使用，后续可添加专门的图标
            else -> R.drawable.ic_flash_auto
        }
        btnFlash.setImageResource(iconRes)
    }

    // ========== 生命周期管理 ==========

    /**
     * Activity销毁时释放相机资源
     */
    override fun onDestroy() {
        super.onDestroy()
        if (::cameraManager.isInitialized) {
            cameraManager.release()
        }
        Log.d(TAG, "Activity销毁，相机资源已释放")
    }
}