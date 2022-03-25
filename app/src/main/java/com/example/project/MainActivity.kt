package com.example.project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    lateinit var onBoardingScreen: SharedPreferences
    private lateinit var fightEngineBtn: Button
    private lateinit var scanPositionBtn: Button
    private lateinit var scanScoreSheetBtn: Button
    private lateinit var generatePuzzleBtn: Button
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    private val REQUEST_CAMERA_CODE = 200
    private val REQUEST_GALLERY_CODE = 300

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

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        fightEngineBtn = findViewById(R.id.fightEngineBtn)
        scanPositionBtn = findViewById(R.id.scanPositionBtn)
        scanScoreSheetBtn = findViewById(R.id.scanScoreSheetBtn)
        generatePuzzleBtn = findViewById(R.id.generatePuzzleBtn)

        scanPositionBtn.setOnClickListener {
            if (checkAndRequestPermissions(this)) {
                val intent = Intent(this, PictureGalleryActivity::class.java)
                intent.putExtra("purpose", "Position")
                startActivity(intent)
            }
        }

        scanScoreSheetBtn.setOnClickListener {
            if (checkAndRequestPermissions(this)) {
                val intent = Intent(this, PictureGalleryActivity::class.java)
                intent.putExtra("purpose", "ScoreSheet")
                startActivity(intent)
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