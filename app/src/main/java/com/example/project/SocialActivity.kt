package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SocialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view2)

        var list : ArrayList<String> = ArrayList<String>(10)
        val database = Firebase.database("https://rookie-3bcea-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        database.get().addOnCompleteListener {
            for (i in it.result.children) {
                for (j in i.children) {
                    if ("https://" !in j.value.toString()) {
                        list.add(j.value.toString())
                    }
                }
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