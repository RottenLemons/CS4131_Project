package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        auth = FirebaseAuth.getInstance()
        val user : FirebaseUser? = FirebaseAuth.getInstance().currentUser

         findViewById<Button>(R.id.logOut).setOnClickListener {

             val builder = AlertDialog.Builder(this)
             builder.setTitle("Log Out")
             builder.setMessage("Do you want to log out?")
             builder.setIcon(android.R.drawable.ic_dialog_alert)

             builder.setPositiveButton("Yes"){_, _ ->
                 auth.signOut()
                 val intent = Intent(this, MainActivity::class.java)
                 startActivity(intent)
             }
             builder.setNegativeButton("No"){_, _ ->
             }

             val alertDialog: AlertDialog = builder.create()
             alertDialog.setCancelable(false)
             alertDialog.show()
        }

        if (user != null) {
            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

            var list : ArrayList<String> = ArrayList<String>(10)
            database = Firebase.database("https://rookie-3bcea-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
            database.child("${user.uid}").get().addOnCompleteListener {
                for (i in it.result.children) {
                    list.add(i.value.toString())
                }

                if (!list.isEmpty()) {
                    val layoutManager = LinearLayoutManager(this)
                    recyclerView.layoutManager = layoutManager
                    val adapter = RecyclerAdapter(list)
                    recyclerView.adapter = adapter
                } else {
                    findViewById<TextView>(R.id.notFound).visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.imgNotFound).visibility = View.VISIBLE
                }
            }
        }
    }
}