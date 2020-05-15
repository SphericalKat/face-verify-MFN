@file:Suppress("DEPRECATION")

package dev.smoketrees.face_verify_mfn.activities

import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dev.smoketrees.face_verify_mfn.R
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.properties.Delegates

class CameraActivity : AppCompatActivity() {
    private lateinit var camera: Camera
    private val IMAGE_FORMAT = ImageFormat.NV21
    private val CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT
    private var displayDegree by Delegates.notNull<Int>()
    private var size: Camera.Size? = null
    private lateinit var data: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_camera)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                releaseCamera()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (holder != null) {
                    openCamera(holder)
                }
            }
        })
        camera_fab.setOnClickListener {
            val bitmap = convertBitmap(data, camera)
            MainActivity.selectedImage.setImageBitmap(bitmap)
            if (MainActivity.selectedImage.id == R.id.imageView) {
                MainActivity.bitmap1 = bitmap
            } else {
                MainActivity.bitmap2 = bitmap
            }
            finish()
        }
    }

    @Synchronized
    private fun releaseCamera() {
        try {
            camera.setPreviewCallback(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            camera.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            camera.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openCamera(holder: SurfaceHolder) {
        releaseCamera()
        camera = Camera.open(CAMERA_ID)
        val parameters: Camera.Parameters = camera.parameters
        displayDegree = setCameraDisplayOrientation(CAMERA_ID, camera)

        size = getOptimalSize(
            parameters.supportedPreviewSizes,
            surfaceView.width,
            surfaceView.height
        )
        parameters.setPreviewSize(size!!.width, size!!.height)
        parameters.previewFormat = IMAGE_FORMAT
        camera.parameters = parameters
        try {
            camera.setPreviewDisplay(holder)
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        camera.setPreviewCallback { data, camera ->
            this.data = data
            camera.addCallbackBuffer(data)
        }
        camera.startPreview()
    }

    private fun setCameraDisplayOrientation(
        cameraId: Int,
        camera: Camera
    ): Int {
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = windowManager.defaultDisplay
            .rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var displayDegree: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360
            displayDegree = (360 - displayDegree) % 360 // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(displayDegree)
        return displayDegree
    }

    private fun getOptimalSize(
        sizes: List<Camera.Size>,
        w: Int,
        h: Int
    ): Camera.Size? {
        val aspectTolerance = 0.1
        val targetRatio = h.toDouble() / w
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (abs(ratio - targetRatio) > aspectTolerance) continue
            if (abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = abs(size.height - h).toDouble()
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }

    private fun convertBitmap(
        data: ByteArray,
        camera: Camera
    ): Bitmap? {
        val previewSize = camera.parameters.previewSize
        val yuvimage = YuvImage(
            data,
            camera.parameters.previewFormat,
            previewSize.width,
            previewSize.height,
            null
        )
        val stream = ByteArrayOutputStream()
        yuvimage.compressToJpeg(
            Rect(0, 0, previewSize.width, previewSize.height),
            100,
            stream
        )
        val rawImage = stream.toByteArray()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.size, options)
        val matrix = Matrix()
        matrix.setRotate(-displayDegree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}