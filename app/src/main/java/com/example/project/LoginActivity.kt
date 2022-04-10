package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class LoginActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var showPasswordToggle: CheckBox
    private lateinit var password: EditText
    private lateinit var signUp: TextView
    private lateinit var username: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        button = findViewById(R.id.loginButton)
        password = findViewById(R.id.password)
        username = findViewById(R.id.username)
        showPasswordToggle = findViewById(R.id.showPasswordToggle)
        signUp = findViewById(R.id.signUpTextView)

        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
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
            val enteredUsername = username.text.toString().trim()
            val enteredPassword = password.text.toString().trim()
            auth.signInWithEmailAndPassword(enteredUsername, enteredPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Info", "signInWithEmail:success ${auth.currentUser}")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w("Info", "${task.exception}")
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        onResume()
                    }
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