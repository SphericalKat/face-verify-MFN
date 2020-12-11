package dev.smoketrees.face_verify_mfn.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import co.potatoproject.faceverify.models.mtcnn.MTCNN
import co.potatoproject.faceverify.utils.FaceUtils
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import dev.smoketrees.face_verify_mfn.R
import dev.smoketrees.face_verify_mfn.databinding.ActivityMainBinding
import dev.smoketrees.face_verify_mfn.ml.MobileFaceNet
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var mfn: MobileFaceNet
    private lateinit var mtcnn: MTCNN
    private lateinit var binding: ActivityMainBinding
    private var bitmapCrop1: Bitmap? = null
    private var bitmapCrop2: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bottomNavigationView = binding.bottomNavView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        runWithPermissions(Manifest.permission.CAMERA) {
            Toast.makeText(this, "Camera permissions granted", Toast.LENGTH_SHORT).show()
        }

        binding.button1.setOnClickListener {
            selectedImage = binding.imageView
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.button2.setOnClickListener {
            selectedImage = binding.imageView2
            startActivity(Intent(this, CameraActivity::class.java))
        }

        try {
            mtcnn = MTCNN(assets)
            mfn = MobileFaceNet(assets)
        } catch (e: IOException) {
            Log.e("TAG", "Error initing models", e)
        }

        binding.cropButton.setOnClickListener {
            faceCrop()
        }

        binding.compareButton.setOnClickListener { compareFaces() }
    }

    private fun faceCrop() {
        if (bitmap1 == null || bitmap2 == null) {
            Toast.makeText(this, "You need to take pictures first", Toast.LENGTH_SHORT).show()
            return
        }

        bitmapCrop1 = FaceUtils.cropBitmapWithFace(bitmap1!!, mtcnn)
        bitmapCrop2 = FaceUtils.cropBitmapWithFace(bitmap2!!, mtcnn)

        binding.cropView.setImageBitmap(bitmapCrop1)
        binding.cropView2.setImageBitmap(bitmapCrop2)
    }

    private fun compareFaces() {
        if (bitmapCrop1 == null || bitmapCrop2 == null) {
            Toast.makeText(this, "You need to crop faces first", Toast.LENGTH_SHORT).show()
            return
        }

        val start = System.currentTimeMillis()
        val same = mfn.compare(bitmapCrop1, bitmapCrop2)
        mfn.generateEmbedding(bitmapCrop1!!)
        val end = System.currentTimeMillis()

        var text = "Match probability: $same"
        if (same > MobileFaceNet.THRESHOLD) {
            text = "$text, Faces match: True"
            binding.resultTextView.setTextColor(resources.getColor(android.R.color.holo_green_light))
        } else {
            text = "$text, Faces match: False"
            binding.resultTextView.setTextColor(resources.getColor(android.R.color.holo_red_light))
        }
        text += "\nTime taken to compare faces: ${(end - start)} ms"
        binding.resultTextView.text = text
    }

    companion object {
        var bitmap1: Bitmap? = null
        var bitmap2: Bitmap? = null
        lateinit var selectedImage: ImageView
    }
}