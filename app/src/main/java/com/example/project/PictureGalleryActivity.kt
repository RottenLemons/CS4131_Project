package com.example.project

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class PictureGalleryActivity : AppCompatActivity() {
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    private val REQUEST_CAMERA_CODE = 200
    private val REQUEST_GALLERY_CODE = 300
    private lateinit var picBtn: Button
    private lateinit var galleryBtn: Button
    private var purpose : String? = null
    private var user : FirebaseUser? = null
    lateinit var sh : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picturegallery)

        sh = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        purpose = intent.getStringExtra("purpose")
        user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            findViewById<FloatingActionButton>(R.id.fab2).setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_dashboard_24))
        }

        picBtn = findViewById(R.id.picBtn)
        galleryBtn = findViewById(R.id.galleryBtn)

        findViewById<FloatingActionButton>(R.id.fab2).setOnClickListener {
            if (user == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, UserActivity::class.java)
                intent.putExtra("User", user)
                startActivity(intent)
            }
        }


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
                intent.putExtra("purpose", purpose)
                startActivity(intent)
            }
        }
    }
}