package dev.smoketrees.face_verify_mfn.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import dev.smoketrees.face_verify_mfn.R
import dev.smoketrees.face_verify_mfn.models.mfn.MobileFaceNet
import dev.smoketrees.face_verify_mfn.models.mtcnn.Box
import dev.smoketrees.face_verify_mfn.models.mtcnn.MTCNN
import dev.smoketrees.face_verify_mfn.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mfn: MobileFaceNet
    private lateinit var mtcnn: MTCNN
    private var bitmapCrop1: Bitmap? = null
    private var bitmapCrop2: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runWithPermissions(Manifest.permission.CAMERA) {
            Toast.makeText(this, "Camera permissions granted", Toast.LENGTH_SHORT).show()
        }

        button1.setOnClickListener {
            selectedImage = imageView
            startActivity(Intent(this, CameraActivity::class.java))
        }

        button2.setOnClickListener {
            selectedImage = imageView2
            startActivity(Intent(this, CameraActivity::class.java))
        }

        try {
            mtcnn = MTCNN(assets)
            mfn = MobileFaceNet(assets)
        } catch (e: IOException) {
            Log.e("TAG", "Error initing models", e)
        }

        cropButton.setOnClickListener {
            faceCrop()
        }

        compareButton.setOnClickListener { compareFaces() }
    }

    private fun faceCrop() {
        if (bitmap1 == null || bitmap2 == null) {
            Toast.makeText(this, "You need to take pictures first", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmapTemp1 = bitmap1!!.copy(bitmap1!!.config, false)
        val bitmapTemp2 = bitmap2!!.copy(bitmap2!!.config, false)


        val start = System.currentTimeMillis()
        val boxes1: Vector<Box> = mtcnn.detectFaces(
            bitmapTemp1,
            bitmapTemp1.width / 5
        )

        val end = System.currentTimeMillis()
        resultTextView.text = "Time taken to crop images: ${(end - start)} ms"
        val boxes2: Vector<Box> = mtcnn.detectFaces(
            bitmapTemp2,
            bitmapTemp2.width / 5
        )

        if (boxes1.size == 0 || boxes2.size == 0) {
            Toast.makeText(this, "No faces detected", Toast.LENGTH_LONG).show()
            return
        }


        val box1 = boxes1[0]
        val box2 = boxes2[0]
        box1.toSquareShape()
        box2.toSquareShape()
        box1.limitSquare(bitmapTemp1.width, bitmapTemp1.height)
        box2.limitSquare(bitmapTemp2.width, bitmapTemp2.height)
        val rect1 = box1.transform2Rect()
        val rect2 = box2.transform2Rect()


        bitmapCrop1 = Utils.crop(bitmapTemp1, rect1)
        bitmapCrop2 = Utils.crop(bitmapTemp2, rect2)
        cropView.setImageBitmap(bitmapCrop1)
        cropView2.setImageBitmap(bitmapCrop2)
    }

    private fun compareFaces() {
        if (bitmapCrop1 == null || bitmapCrop2 == null) {
            Toast.makeText(this, "You need to crop faces first", Toast.LENGTH_SHORT).show()
            return
        }

        val start = System.currentTimeMillis()
        val same = mfn.compare(bitmapCrop1, bitmapCrop2)
        val end = System.currentTimeMillis()

        var text = "Match probability: $same"
        if (same > MobileFaceNet.THRESHOLD) {
            text = "$text, Faces match: True"
            resultTextView.setTextColor(resources.getColor(android.R.color.holo_green_light))
        } else {
            text = "$text, Faces match: False"
            resultTextView.setTextColor(resources.getColor(android.R.color.holo_red_light))
        }
        text += "\nTime taken to compare faces: ${(end - start)} ms"
        resultTextView.text = text
    }

    companion object {
        var bitmap1: Bitmap? = null
        var bitmap2: Bitmap? = null
        lateinit var selectedImage: ImageView
    }
}