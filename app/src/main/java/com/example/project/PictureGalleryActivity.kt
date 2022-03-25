package com.example.project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PictureGalleryActivity : AppCompatActivity() {
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    private val REQUEST_CAMERA_CODE = 200
    private val REQUEST_GALLERY_CODE = 300
    private lateinit var picBtn: Button
    private lateinit var galleryBtn: Button
    private var purpose : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picturegallery)

        purpose = intent.getStringExtra("purpose")

        picBtn = findViewById(R.id.picBtn)
        galleryBtn = findViewById(R.id.galleryBtn)

        picBtn.setOnClickListener {
            try {
                val toastText : String
                var request = 0

                    intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    request = REQUEST_CAMERA_CODE
                    toastText = "Launching Camera App"

                Toast.makeText(this@PictureGalleryActivity, toastText, Toast.LENGTH_SHORT).show()
                if (intent.resolveActivity(packageManager) != null)
                    startActivityForResult(intent, request)
            } catch (ex: ArrayStoreException) {
                Toast.makeText(this,
                    "Operation failed, please try later", Toast.LENGTH_SHORT).show()
            }
        }

        galleryBtn.setOnClickListener {
            try {
                var request = 0
                val toastText : String

                    intent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    request = REQUEST_GALLERY_CODE
                    toastText = "Launching Gallery"

                Toast.makeText(this@PictureGalleryActivity, toastText, Toast.LENGTH_SHORT).show()
                if (intent.resolveActivity(packageManager) != null)
                    startActivityForResult(intent, request)
            } catch (ex: ArrayStoreException) {
                Toast.makeText(this,
                    "Operation failed, please try later${ex}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == REQUEST_CAMERA_CODE && data != null) {
                val selectedImage = data.extras!!.get("data") as Bitmap
                val intent = Intent(this, LinksActivity::class.java)
                intent.putExtra("image", selectedImage)
                intent.putExtra("type", "camera")
                intent.putExtra("purpose", purpose)
                startActivity(intent)
            } else if (requestCode == REQUEST_GALLERY_CODE && data != null) {
                val selectedImage = data.data
                val intent = Intent(this, LinksActivity::class.java)
                intent.putExtra("image", selectedImage.toString())
                intent.putExtra("type", "gallery")
                intent.putExtra("purpose", purpose)
                startActivity(intent)
            }
        }
    }
}