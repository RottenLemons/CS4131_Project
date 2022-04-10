package com.example.project

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import no.bakkenbaeck.chessboardeditor.view.board.ChessBoardView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class LinksActivity : AppCompatActivity() {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    lateinit var image: InputImage
    lateinit var textInImage : TextView
    lateinit var copyToClipboard : ImageView
    lateinit var classifier: Classifier
    lateinit var scrollView : ScrollView
    private val labelPath = "full_labels.txt"
    private val modelPath = "mobilenetv2.tflite"
    private lateinit var pyobjectSaver: PyObject
    private val TOTAL_SQUARES = 64
    lateinit var imageStream: InputStream
    var selectedImage: Bitmap? = null
    private lateinit var database: DatabaseReference
    private var user : FirebaseUser? = null
    private lateinit var chessBoardView: ChessBoardView
    private lateinit var lichessBtn : Button
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    lateinit var sh : SharedPreferences
    lateinit var notificationManager : NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        createNotificationChannel("com.example.notifydemo.news",
            "Notification Demo News","Example News Channel")
        sh = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        database = Firebase.database("https://rookie-3bcea-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        var intent = intent
        textInImage = findViewById(R.id.textInImage)
        copyToClipboard = findViewById(R.id.copyToClipboard)
        scrollView = findViewById(R.id.scrollView)
        user = FirebaseAuth.getInstance().currentUser
        chessBoardView = findViewById(R.id.chessBoardView)
        lichessBtn = findViewById(R.id.lichessBtn)

        lichessBtn.setOnClickListener {
            val fen = chessBoardView.getFen()
            if (user != null) {
                val uid = user!!.uid
                var list : ArrayList<String> = ArrayList<String>(10)
                database.child("$uid").get().addOnCompleteListener {
                    for (i in it.result.children) {
                        list.add(i.value.toString())
                    }
                    list.add("/" + fen)
                    database.child("$uid").setValue(list)
                }
            }

            val lichess = Intent(Intent.ACTION_VIEW, Uri.parse("https://lichess.org/analysis/${fen}"))
            startActivity(lichess)
        }

        if (sh.getString("purpose", "Position") == "Position") {

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
            scheduler.schedule(matrixThread, 3, TimeUnit.SECONDS)

        } else {
            findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
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
                scrollView.visibility = View.VISIBLE
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
                        val predictionThread = PredictionThread()
                        predictionThread.start()
                    } else {
                        Toast.makeText(
                            getApplicationContext(),
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
                    val fen = string(boardString)
                    chessBoardView.setFen(fen)
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
        } else {
            Toast.makeText(this, "Image does not seem complete", Toast.LENGTH_SHORT).show()
        }
        return results
    }

    private fun string(stringBoard: String): String {
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
        equalStr.reverse()
        for (i in equalStr) {
            fen += "/" + i
        }
        findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
        findViewById<ConstraintLayout>(R.id.boardView).visibility = View.VISIBLE
        val notificationID = 101
        val channelID = "com.example.notifydemo.news"
        val notification = Notification.Builder(this, channelID)
            .setContentTitle("Completion Achieved")
            .setContentText("Position Scan is Completed!!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setChannelId(channelID).build()
        notificationManager.notify(notificationID, notification)
        return fen
    }

    private fun createNotificationChannel(id: String, name: String, description: String) {
        val importance = NotificationManager.IMPORTANCE_LOW
        // val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager.createNotificationChannel(channel)
    }
}