package com.example.project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    lateinit var onBoardingScreen: SharedPreferences
    private lateinit var scanPositionBtn: Button
    private lateinit var scanScoreSheetBtn: Button
    private lateinit var generatePuzzleBtn: Button
    private lateinit var socialBtn: Button
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    private val REQUEST_CAMERA_CODE = 200
    private val REQUEST_GALLERY_CODE = 300
    private lateinit var database: DatabaseReference
    lateinit var sh : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        onBoardingScreen = getSharedPreferences("onBoardingScreen", MODE_PRIVATE)
        val isFirst = onBoardingScreen.getBoolean("firstTime", true)
        if (isFirst) {
            val editor: SharedPreferences.Editor = onBoardingScreen.edit()
            editor.putBoolean("firstTime", false)
            editor.commit()
            val intent = Intent(this, OnboardActivity::class.java)
            startActivity(intent)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sh = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        database = Firebase.database("https://rookie-3bcea-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        val user : FirebaseUser? = FirebaseAuth.getInstance().currentUser
        Log.d("Info", "signInWithEmail:success ${user}")
        if (user != null) {
            findViewById<FloatingActionButton>(R.id.fab).setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_dashboard_24))
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            if (user == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, UserActivity::class.java)
                intent.putExtra("User", user)
                startActivity(intent)
            }
        }

        scanPositionBtn = findViewById(R.id.scanPositionBtn)
        scanScoreSheetBtn = findViewById(R.id.scanScoreSheetBtn)
        generatePuzzleBtn = findViewById(R.id.generatePuzzleBtn)
        socialBtn = findViewById(R.id.socialBtn)

        socialBtn.setOnClickListener {
            val intent = Intent(this, SocialActivity::class.java)
            startActivity(intent)
        }

        scanPositionBtn.setOnClickListener {
            if (checkAndRequestPermissions(this)) {
                val intent = Intent(this, PictureGalleryActivity::class.java)
                val sharedPrefEditor = sh.edit()
                sharedPrefEditor.apply {
                    clear()
                    putString("purpose", "Position")
                    apply()
                }
                intent.putExtra("purpose", "Position")
                startActivity(intent)
            }
        }

        scanScoreSheetBtn.setOnClickListener {
            if (checkAndRequestPermissions(this)) {
                val intent = Intent(this, PictureGalleryActivity::class.java)
                val sharedPrefEditor = sh.edit()
                sharedPrefEditor.apply {
                    clear()
                    putString("purpose", "ScoreSheet")
                    apply()
                }
                intent.putExtra("purpose", "ScoreSheet")
                startActivity(intent)
            }
        }

        findViewById<ImageView>(R.id.help).setOnClickListener {
            var intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        generatePuzzleBtn.setOnClickListener {
            var link = ""
            database.child("links").get().addOnCompleteListener {
                var randNum = Random.nextInt(0,it.result.children.count())
                link = it.result.children.elementAt(randNum).value.toString()
                if (link == "") {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                } else {
                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                }
            }
        }
    }

    fun checkAndRequestPermissions(context: Activity?): Boolean {
        val ExtstorePermission = ContextCompat.checkSelfPermission(
            context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        val cameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        if (ExtstorePermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "App Requires Access to Camara.",
                        Toast.LENGTH_SHORT).show()
                } else if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext,
                        "App Requires Access to Your Storage.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}