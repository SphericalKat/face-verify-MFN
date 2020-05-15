package dev.smoketrees.face_verify_mfn.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import dev.smoketrees.face_verify_mfn.models.mtcnn.Box
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object Utils {

    fun readFromAssets(context: Context, filename: String?): Bitmap? {
        val bitmap: Bitmap
        val asm = context.assets
        try {
            val `is` = asm.open(filename!!)
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return bitmap
    }

    fun rectExtend(
        bitmap: Bitmap,
        rect: Rect,
        marginX: Int,
        marginY: Int
    ) {
        rect.left = Math.max(0, rect.left - marginX / 2)
        rect.right = Math.min(bitmap.width - 1, rect.right + marginX / 2)
        rect.top = Math.max(0, rect.top - marginY / 2)
        rect.bottom = Math.min(bitmap.height - 1, rect.bottom + marginY / 2)
    }

    fun rectExtend(bitmap: Bitmap, rect: Rect) {
        val width = rect.right - rect.left
        val height = rect.bottom - rect.top
        val margin = (height - width) / 2
        rect.left = Math.max(0, rect.left - margin)
        rect.right = Math.min(bitmap.width - 1, rect.right + margin)
    }

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
        for (i in 0 until h) { // 注意是先高后宽
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

    /**
     * 缩放图片
     * @param bitmap
     * @param scale
     * @return
     */
    fun bitmapResize(bitmap: Bitmap, scale: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(
            bitmap, 0, 0, width, height, matrix, true
        )
    }

    /**
     * 图片矩阵宽高转置
     * @param in
     * @return
     */
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

    /**
     * 4维图片batch矩阵宽高转置
     * @param in
     * @return
     */
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
                squareSum += Math.pow(element.toDouble(), 2.0).toFloat()
            }
            val xInvNorm = Math.sqrt(
                Math.max(
                    squareSum.toDouble(),
                    epsilon
                )
            ).toFloat()
            for (j in embeddings[i].indices) {
                embeddings[i][j] = embeddings[i][j] / xInvNorm
            }
        }
    }

    fun convertGreyImg(bitmap: Bitmap): Array<IntArray> {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(h * w)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val result =
            Array(h) { IntArray(w) }
        val alpha = 0xFF shl 24
        for (i in 0 until h) {
            for (j in 0 until w) {
                val `val` = pixels[w * i + j]
                val red = `val` shr 16 and 0xFF
                val green = `val` shr 8 and 0xFF
                val blue = `val` and 0xFF
                var grey =
                    (red.toFloat() * 0.3 + green.toFloat() * 0.59 + blue.toFloat() * 0.11).toInt()
                grey = alpha or (grey shl 16) or (grey shl 8) or grey
                result[i][j] = grey
            }
        }
        return result
    }
}