package com.example.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.util.*

object Utilities {
    private var piecesMap: Map<String, String>? = null
    private var drawableMap: Map<String, String?>? = null
    fun pyimageToBitmap(obj: String?): Bitmap? {
        if (obj == null) return null
        val encodeByte =
            Base64.decode(obj, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    }

    fun mapStringToIndex(s: String): String? {
        return piecesMap!![s]
    }

    fun mapDrawableFromIndex(s: String): String? {
        return drawableMap!![s]
    }

    init {
        val map: MutableMap<String, String> = HashMap()
        map["white_bishop"] = "B"
        map["white_queen"] = "Q"
        map["white_king"] = "K"
        map["white_knight"] = "N"
        map["white_rook"] = "R"
        map["white_pawn"] = "P"
        map["black_queen"] = "q"
        map["black_rook"] = "r"
        map["black_king"] = "k"
        map["black_knight"] = "n"
        map["black_pawn"] = "p"
        map["black_bishop"] = "b"
        map["empty"] = "1"
        piecesMap = Collections.unmodifiableMap(map)
    }

    init {
        val map: MutableMap<String, String?> = HashMap()
        map["B"] = "wb_foreground"
        map["Q"] = "wq_foreground"
        map["K"] = "wk_foreground"
        map["N"] = "wn_foreground"
        map["R"] = "wr_foreground"
        map["P"] = "wp_foreground"
        map["q"] = "bq_foreground"
        map["r"] = "br_foreground"
        map["k"] = "bk_foreground"
        map["n"] = "bn_foreground"
        map["p"] = "bp_foreground"
        map["b"] = "bb_foreground"
        map["1"] = null
        drawableMap = Collections.unmodifiableMap(map)
    }
}