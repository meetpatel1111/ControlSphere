package com.controlsphere.tvremote.data.voice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

/**
 * Service for capturing TV screen images for computer use analysis
 * Uses MediaProjection API to capture screenshots
 */
@Singleton
class ScreenCaptureService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handler: Handler? = null
    
    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.IDLE)
    val captureState: Flow<CaptureState> = _captureState.asStateFlow()
    
    private val _latestScreenshot = MutableStateFlow<Bitmap?>(null)
    val latestScreenshot: Flow<Bitmap?> = _latestScreenshot.asStateFlow()
    
    /**
     * Initialize screen capture with MediaProjection
     */
    suspend fun initializeCapture(mediaProjection: MediaProjection): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            this@ScreenCaptureService.mediaProjection = mediaProjection
            
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            display.getMetrics(displayMetrics)
            
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            val density = displayMetrics.densityDpi
            
            // Create ImageReader for screen capture
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            
            // Set up image listener
            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null) {
                    try {
                        val bitmap = imageToBitmap(image)
                        _latestScreenshot.value = bitmap
                    } finally {
                        image.close()
                    }
                }
            }, Handler(Looper.getMainLooper()))
            
            // Create virtual display
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "ControlSphere Capture",
                width,
                height,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )
            
            _captureState.value = CaptureState.ACTIVE
            Result.success(Unit)
        } catch (e: Exception) {
            _captureState.value = CaptureState.ERROR(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
    
    /**
     * Capture current screen
     */
    suspend fun captureScreen(): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            if (mediaProjection == null || virtualDisplay == null) {
                return@withContext Result.failure(Exception("Screen capture not initialized"))
            }
            
            // Request new frame
            val image = imageReader?.acquireLatestImage()
            if (image != null) {
                try {
                    val bitmap = imageToBitmap(image)
                    _latestScreenshot.value = bitmap
                    Result.success(bitmap)
                } finally {
                    image.close()
                }
            } else {
                Result.failure(Exception("Failed to capture screen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Start continuous screen capture
     */
    suspend fun startContinuousCapture(intervalMs: Long = 1000): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (mediaProjection == null) {
                return@withContext Result.failure(Exception("Screen capture not initialized"))
            }
            
            // In a real implementation, this would set up periodic capture
            // For now, we simulate with a single capture
            captureScreen()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Stop screen capture
     */
    fun stopCapture() {
        try {
            virtualDisplay?.release()
            imageReader?.close()
            mediaProjection?.stop()
            
            virtualDisplay = null
            imageReader = null
            mediaProjection = null
            handler = null
            
            _captureState.value = CaptureState.IDLE
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Check if capture is active
     */
    fun isActive(): Boolean {
        return _captureState.value is CaptureState.ACTIVE
    }
    
    /**
     * Get screen dimensions
     */
    fun getScreenDimensions(): Pair<Int, Int> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }
    
    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        // Crop to actual image size
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }
}

/**
 * Screen capture state enumeration
 */
sealed class CaptureState {
    object IDLE : CaptureState()
    object ACTIVE : CaptureState()
    data class ERROR(val message: String) : CaptureState()
    object PERMISSION_REQUIRED : CaptureState()
}

/**
 * Screen capture configuration
 */
data class ScreenCaptureConfig(
    val width: Int,
    val height: Int,
    val density: Int,
    val frameRate: Int = 30,
    val format: Int = PixelFormat.RGBA_8888
)
