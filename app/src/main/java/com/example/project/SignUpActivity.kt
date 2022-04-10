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

class SignUpActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var showPasswordToggle: CheckBox
    private lateinit var passwordTV: EditText
    private lateinit var login: TextView
    private lateinit var username: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth
        button = findViewById(R.id.signUpButton)
        passwordTV = findViewById(R.id.password2)
        username = findViewById(R.id.username2)
        showPasswordToggle = findViewById(R.id.showPasswordToggle2)
        login = findViewById(R.id.loginTextView)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        showPasswordToggle.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                passwordTV.transformationMethod = PasswordTransformationMethod()
            } else {
                passwordTV.transformationMethod = null
            }
        }

        passwordTV.setOnFocusChangeListener { v, focus ->
            if (!focus) hideKeyboard(v)
        }

        username.setOnFocusChangeListener { v, focus ->
            if (!focus) hideKeyboard(v)
        }

        button.setOnClickListener { v ->
            hideKeyboard(v)
            val email = username.text.toString().trim()
            val password = passwordTV.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Info", "createUserWithEmail:success ${auth.currentUser}")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Info", "createUserWithEmail:${email}, ${password}", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        onResume()
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        passwordTV.text.clear()
        username.text.clear()
        showPasswordToggle.isChecked = false
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}