package com.example.project

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class RecyclerAdapter(val list: ArrayList<String>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    override fun getItemCount() = list.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var width = 0
        var new_line_id = 1
        var dark_color = Color.rgb(0, 0, 0)
        var light_color = Color.rgb(255, 255, 255)
        var current_color = light_color
        var switch_current_color = dark_color
        var switch_colors = true
        var rl: RelativeLayout
        var ids: ArrayList<Int> = ArrayList<Int>()

        init {
            rl = itemView.findViewById(R.id.chessBoard)
        }

        fun bindItems(string: String) {
            setScaledButtonWidth()
            createGrid(string)
            var board = unString(string)
            for (i in 0..7) {
                for (j in 0..7) {
                    val piece: Char = board.get(i).get(j)
                    val index_id = i * 8 + j
                    val btn = itemView.findViewById(ids[index_id]) as Button
                    val drawableString = Utilities.mapDrawableFromIndex(piece.toString())
                    if (drawableString != null) {
                        val resources: Resources = itemView.context.getResources()
                        val resourceId = resources.getIdentifier(drawableString, "drawable", itemView.context.getPackageName())
                        btn.foreground = resources.getDrawable(resourceId)
                    }
                }
            }
            itemView.setOnClickListener{
                val lichess = Intent(Intent.ACTION_VIEW, Uri.parse("https://lichess.org/analysis${string}"))
                itemView.getContext().startActivity(lichess)
            }

            itemView.setOnLongClickListener{
                val clipboard = ContextCompat.getSystemService(itemView.context, ClipboardManager::class.java)
                val clip = ClipData.newPlainText("label", string)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(itemView.context, "Text Copied", Toast.LENGTH_SHORT).show()
                true
            }
        }

        private fun setScaledButtonWidth() {
            val displayMetrics = DisplayMetrics()
            val wm = (itemView.getContext().getSystemService(Context.WINDOW_SERVICE)) as WindowManager
            wm.getDefaultDisplay().getMetrics(displayMetrics)
            width = displayMetrics.widthPixels / 9
        }

        private fun createGrid(string : String) {
            for (i in 1..8) {
                switch_colors = !switch_colors
                val first_lp = RelativeLayout.LayoutParams(width, width)
                val first_btn = Button(itemView.context)
                first_btn.setOnClickListener {
                    val lichess = Intent(Intent.ACTION_VIEW, Uri.parse("https://lichess.org/analysis${string}"))
                    itemView.getContext().startActivity(lichess)
                }
                if (i != 1) {
                    first_lp.addRule(RelativeLayout.BELOW, new_line_id)
                    new_line_id += 8
                }
                first_btn.id = new_line_id
                ids.add(first_btn.id)
                first_btn.setBackgroundColor(getSquareColor())
                rl.addView(first_btn, first_lp)
                for (j in 1..7) {
                    val lp = RelativeLayout.LayoutParams(width, width)
                    val btn = Button(itemView.context)
                    btn.setOnClickListener {
                        val lichess = Intent(Intent.ACTION_VIEW, Uri.parse("https://lichess.org/analysis${string}"))
                        itemView.getContext().startActivity(lichess)
                    }
                    if (i != 1) {
                        lp.addRule(RelativeLayout.BELOW, new_line_id - 8)
                    }
                    lp.addRule(RelativeLayout.RIGHT_OF, new_line_id + j - 1)
                    btn.id = new_line_id + j
                    btn.setBackgroundColor(getSquareColor())
                    ids.add(btn.id)
                    rl.addView(btn, lp)
                }
            }
        }

        private fun getSquareColor(): Int {
            val temp: Int
            if (switch_colors) {
                temp = switch_current_color
                if (switch_current_color == light_color) {
                    switch_current_color = dark_color
                } else {
                    switch_current_color = light_color
                }
            } else {
                temp = current_color
                if (current_color == light_color) {
                    current_color = dark_color
                } else {
                    current_color = light_color
                }
            }
            return temp
        }

        private fun unString(stringBoard: String): List<String> {
            var string = stringBoard.substring(1)
            string = string.replace("2", "11")
            string = string.replace("3", "111")
            string = string.replace("4", "1111")
            string = string.replace("5", "11111")
            string = string.replace("6", "111111")
            string = string.replace("7", "1111111")
            string = string.replace("8", "11111111")
            var equalStr = string.split("/")
            return equalStr
        }
    }

}