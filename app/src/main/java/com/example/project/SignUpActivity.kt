package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class SignUpActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var showPasswordToggle: CheckBox
    private lateinit var password: EditText
    private lateinit var login: TextView
    private lateinit var username: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        button = findViewById(R.id.signUpButton)
        password = findViewById(R.id.password2)
        username = findViewById(R.id.username2)
        showPasswordToggle = findViewById(R.id.showPasswordToggle2)
        login = findViewById(R.id.loginTextView)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        showPasswordToggle.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                password.transformationMethod = PasswordTransformationMethod()
            } else {
                password.transformationMethod = null
            }
        }

        password.setOnFocusChangeListener { v, focus ->
            if (!focus) hideKeyboard(v)
        }

        username.setOnFocusChangeListener { v, focus ->
            if (!focus) hideKeyboard(v)
        }

        button.setOnClickListener { v ->
            hideKeyboard(v)
            val enteredUsername = username.text.toString()
            val enteredPassword = password.text.toString()
            if (!enteredUsername.matches("[A-Za-z0-9_]{1,30}".toRegex())) {
                Snackbar.make(
                    v,
                    "Username must only be alphanumeric with '_', and be between 1 and 30 characters.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (!enteredPassword.matches("[A-Za-z0-9_$#@%!&]{8,}".toRegex())) {
                Snackbar.make(
                    v,
                    "Password can only be alphanumeric with '_\$#@%!&', and must be 8 characters or longer.",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        password.text.clear()
        username.text.clear()
        showPasswordToggle.isChecked = false
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}