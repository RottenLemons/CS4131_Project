package com.example.project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3

class OnboardActivity : AppCompatActivity() {
    private val fragmentList = ArrayList<Fragment>()
    private lateinit var viewPager: ViewPager2
    private lateinit var indicator: CircleIndicator3
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 101
    private val REQUEST_CAMERA_CODE = 200
    private val REQUEST_GALLERY_CODE = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)

        checkAndRequestPermissions(this)
        castView()
        fragmentList.add(FirstFragment())
        fragmentList.add(SecondFragment())
        viewPager.adapter = ViewPager2FragmentAdapter(this, fragmentList)
        viewPager.orientation= ViewPager2.ORIENTATION_HORIZONTAL
        indicator.setViewPager(viewPager)
    }

    private fun castView() {
        viewPager = findViewById(R.id.view_pager2)
        indicator = findViewById(R.id.indicator)
    }

    fun checkAndRequestPermissions(context: Activity?): Boolean {
        val ExtstorePermission = ContextCompat.checkSelfPermission(
            context!!, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        if (ExtstorePermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }
}