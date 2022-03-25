package com.example.project

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class LinksActivity : AppCompatActivity() {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    lateinit var image: InputImage
    lateinit var textInImage : TextView
    lateinit var copyToClipboard : ImageView
    lateinit var lichessBtn : Button
    lateinit var chessBtn : Button
    lateinit var close : ImageView
    lateinit var classifier: Classifier
    lateinit var textInImageLayout : MaterialCardView
    private val labelPath = "full_labels.txt"
    private val modelPath = "mobilenetv2.tflite"
    private lateinit var pyobjectSaver: PyObject
    private val TOTAL_SQUARES = 64
    lateinit var imageStream: InputStream
    var selectedImage: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)

        var intent = intent
        textInImage = findViewById(R.id.textInImage)
        copyToClipboard = findViewById(R.id.copyToClipboard)
        close = findViewById(R.id.close)
        textInImageLayout = findViewById(R.id.textInImageLayout)
        lichessBtn = findViewById(R.id.lichessBtn)
        chessBtn = findViewById(R.id.chessBtn)

        if (intent.getStringExtra("purpose") == "Position") {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform((this)));
            }

            try {
                classifier = Classifier(this, modelPath, labelPath)
            } catch (e: IOException) {
                e.printStackTrace()
            }



            if (intent.getStringExtra("type") == "camera") {
                selectedImage = intent.getParcelableExtra("image")!!
            } else {
                try {
                    imageStream = getContentResolver().openInputStream(Uri.parse((intent.getStringExtra("image"))))!!
                    selectedImage = BitmapFactory.decodeStream(imageStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            val bmp32 = selectedImage!!.copy(Bitmap.Config.RGB_565, true)


            val matrixThread = MatrixThread(bmp32)
            matrixThread.start()
            val predictionThread = PredictionThread()
            predictionThread.start()

        } else {
            if (intent.getStringExtra("type") == "camera") {
                image = InputImage.fromBitmap(intent.getParcelableExtra("image")!!, 0)
                runTextRecognition()
            } else {
                try {
                    image = InputImage.fromFilePath(this, Uri.parse((intent.getStringExtra("image"))))
                    runTextRecognition()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        copyToClipboard.setOnClickListener {
            val textToCopy = textInImage.text
            if (isTextValid(textToCopy.toString())) {
                val clipboard = ContextCompat.getSystemService(applicationContext, ClipboardManager::class.java)
                val clip = ClipData.newPlainText("label", textToCopy)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Couldn't Find any text in here…", Toast.LENGTH_SHORT).show()
            }
        }

        close.setOnClickListener {
            textInImageLayout.visibility = View.GONE
            lichessBtn.visibility = View.VISIBLE
            chessBtn.visibility = View.VISIBLE
        }
    }

    private fun isTextValid(text: String?): Boolean {
        if (text == null)
            return false

        return text.isNotEmpty() and !text.equals("Couldn't Find any text in here…")
    }

    fun getBoardString(results: List<String>): String {
        val s = StringBuilder()
        for (i in results.indices) {
            s.append(Utilities.mapStringToIndex(results[i]))
        }
        return s.toString()
    }


    private fun runTextRecognition() {
        recognizer
            .process(image)
            .addOnSuccessListener { text ->
                textInImageLayout.visibility = View.VISIBLE
                processTextRecognitionResult(text)
            }.addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error In Text Recognition", Toast.LENGTH_SHORT).show()
            }
    }

    private fun processTextRecognitionResult(result: Text) {
        var finalText = ""
        for (block in result.textBlocks) {
            for (line in block.lines) {
                finalText += line.text + " \n"
            }
            finalText += "\n"
        }


        textInImage.text = if (finalText.isNotEmpty()) {
            finalText
        } else {
            "Couldn't Find any text in here…"
        }
    }

    inner class MatrixThread(var image: Bitmap) : Thread() {
        override fun run() {
            super.run()
            runOnUiThread {
                val byteArrayOutputStream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val py = Python.getInstance()
                val pyObject = py.getModule("DetectAllPoints")
                val obj = pyObject.callAttr("getMatrixFromImage", byteArray as Any)
                if (obj.asList().size == 3 && obj.asList()[1] != null) {
                    val bitmap = Utilities.pyimageToBitmap(obj.asList()[1].toString())
                    if (bitmap != null) {
                        pyobjectSaver = obj
                    } else {
                        Toast.makeText(
                            this@LinksActivity,
                            "Nothing found..",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        getApplicationContext(),
                        "Not enough return arguments..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    inner class PredictionThread : Thread() {
        override fun run() {
            super.run()
            runOnUiThread(Runnable {
                try {
                    val results: List<String> = cropSquares()
                    val boardString: String = getBoardString(results)
                    val fen = splitAndReverseString(boardString)
                    val lichess = Intent(Intent.ACTION_VIEW, Uri.parse("https://lichess.org/analysis${fen}"))
                    startActivity(lichess)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            })
        }
    }

    @Throws(InterruptedException::class)
    private fun cropSquares(): List<String> {
        val results: MutableList<String> = ArrayList()
        if (pyobjectSaver != null) {
            val py = Python.getInstance()
            val detector = py.getModule("PiecesDetector")
            val obj_detector = detector.callAttr(
                "cropPieces",
                pyobjectSaver.asList()[0], pyobjectSaver.asList()[2]
            )
            val single_image = py.getModule("PiecesDetector")
            for (i in 0 until TOTAL_SQUARES) {
                val obj_single_piece = single_image.callAttr("getSingleImage", obj_detector, i)
                val bitmap = Utilities.pyimageToBitmap(obj_single_piece.toString())
                val result = classifier.makePrediction(
                    bitmap!!
                )
                val final_result = classifier.getTopProbability(result)

                results.add(final_result.first)
            }
        }
        return results
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        val mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun splitAndReverseString(stringBoard: String): String {
        val len = stringBoard.length
        var fen : String = ""
        val n = 8
        var temp = 0
        val chars = len / n
        val equalStr = arrayOfNulls<String>(n)
        if (len % n != 0) {
        } else {
            var i = 0
            while (i < len) {
                val part = stringBoard.substring(i, i + chars)
                equalStr[temp] = part
                temp++
                i = i + chars
            }
        }
        Collections.reverse(Arrays.asList(*equalStr))
        for (i in equalStr) {
            fen += "/" + i
        }
        return fen
    }
}