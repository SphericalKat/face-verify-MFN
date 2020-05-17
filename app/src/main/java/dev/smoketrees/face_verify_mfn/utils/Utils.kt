package dev.smoketrees.face_verify_mfn.utils

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import dev.smoketrees.face_verify_mfn.models.mtcnn.Box
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object Utils {

    @Throws(IOException::class)
    fun loadModelFile(
        assetManager: AssetManager,
        modelPath: String?
    ): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath!!)
        val inputStream =
            FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    fun normalizeImage(bitmap: Bitmap): Array<Array<FloatArray>> {
        val h = bitmap.height
        val w = bitmap.width
        val floatValues =
            Array(
                h
            ) { Array(w) { FloatArray(3) } }
        val imageMean = 127.5f
        val imageStd = 128f
        val pixels = IntArray(h * w)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, w, h)
        for (i in 0 until h) {
            for (j in 0 until w) {
                val `val` = pixels[i * w + j]
                val r = ((`val` shr 16 and 0xFF) - imageMean) / imageStd
                val g = ((`val` shr 8 and 0xFF) - imageMean) / imageStd
                val b = ((`val` and 0xFF) - imageMean) / imageStd
                val arr = floatArrayOf(r, g, b)
                floatValues[i][j] = arr
            }
        }
        return floatValues
    }

    fun bitmapResize(bitmap: Bitmap, scale: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(
            bitmap, 0, 0, width, height, matrix, true
        )
    }

    fun transposeImage(`in`: Array<Array<FloatArray>>): Array<Array<FloatArray>> {
        val h = `in`.size
        val w: Int = `in`[0].size
        val channel: Int = `in`[0][0].size
        val out =
            Array(
                w
            ) { Array(h) { FloatArray(channel) } }
        for (i in 0 until h) {
            for (j in 0 until w) {
                out[j][i] = `in`[i][j]
            }
        }
        return out
    }

    fun transposeBatch(`in`: Array<Array<Array<FloatArray>>>): Array<Array<Array<FloatArray>>> {
        val batch = `in`.size
        val h: Int = `in`[0].size
        val w: Int = `in`[0][0].size
        val channel: Int = `in`[0][0][0].size
        val out =
            Array(
                batch
            ) {
                Array(
                    w
                ) { Array(h) { FloatArray(channel) } }
            }
        for (i in 0 until batch) {
            for (j in 0 until h) {
                for (k in 0 until w) {
                    out[i][k][j] = `in`[i][j][k]
                }
            }
        }
        return out
    }

    fun cropAndResize(
        bitmap: Bitmap?,
        box: Box,
        size: Int
    ): Array<Array<FloatArray>> {
        // crop and resize
        val matrix = Matrix()
        val scaleW: Float = 1.0f * size / box.width()
        val scaleH: Float = 1.0f * size / box.height()
        matrix.postScale(scaleW, scaleH)
        val rect: Rect = box.transform2Rect()
        val cropped = Bitmap.createBitmap(
            bitmap!!, rect.left, rect.top, box.width(), box.height(), matrix, true
        )
        return normalizeImage(cropped)
    }

    fun crop(bitmap: Bitmap?, rect: Rect): Bitmap {
        return Bitmap.createBitmap(
            bitmap!!,
            rect.left,
            rect.top,
            rect.right - rect.left,
            rect.bottom - rect.top
        )
    }

    fun l2Normalize(
        embeddings: Array<FloatArray>,
        epsilon: Double
    ) {
        for (i in embeddings.indices) {
            var squareSum = 0f
            for (element in embeddings[i]) {
                squareSum += element.toDouble().pow(2.0).toFloat()
            }
            val xInvNorm = sqrt(
                max(
                    squareSum.toDouble(),
                    epsilon
                )
            ).toFloat()
            for (j in embeddings[i].indices) {
                embeddings[i][j] = embeddings[i][j] / xInvNorm
            }
        }
    }
}