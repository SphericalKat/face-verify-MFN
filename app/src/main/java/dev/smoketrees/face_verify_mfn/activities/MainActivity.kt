package dev.smoketrees.face_verify_mfn.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import dev.smoketrees.face_verify_mfn.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runWithPermissions(Manifest.permission.CAMERA) {
            Toast.makeText(this, "Camera permissions granted", Toast.LENGTH_SHORT).show()
        }


        selectedImage = imageView
        startActivity(Intent(this, CameraActivity::class.java))
    }

    companion object {
        var bitmap1: Bitmap? = null
        var bitmap2: Bitmap? = null
        lateinit var selectedImage: ImageView
    }
}