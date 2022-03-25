package com.example.project

import org.tensorflow.lite.DataType

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

import android.app.Activity
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class Classifier internal constructor(
    private val activity: Activity,
    modelPath: String,
    labelPath: String
) {
    private val interpreter: Interpreter
    private val labelList: List<String>
    private var imageSizeX = 0
    private var imageSizeY = 0
    private val MAX_RESULTS = 3f
    private val THRESHOLD = 0.5f
    private var inputImageBuffer: TensorImage? = null
    fun getTopProbability(labelProb: Map<String, Float>): Pair<String, Float> {
        val pq = PriorityQueue<Pair<String, Float>>(
            1,
            object : Comparator<Pair<String, Float>> {
                override fun compare(lhs: Pair<String, Float>?, rhs: Pair<String, Float>?): Int {
                    return java.lang.Float.compare(rhs?.second!!, lhs?.second!!)
                }
            })
        for ((key, value) in labelProb) {
            pq.add(Pair(key, value))
        }
        return pq.poll()
    }

    fun makePrediction(bitmap: Bitmap): Map<String, Float> {
        val imageTensorIndex = 0
        val imageShape: IntArray = interpreter.getInputTensor(imageTensorIndex).shape()
        imageSizeY = imageShape[1]
        imageSizeX = imageShape[2]
        val imageDataType: DataType = interpreter.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0
        val probabilityShape: IntArray = interpreter.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType: DataType =
            interpreter.getOutputTensor(probabilityTensorIndex).dataType()
        inputImageBuffer = loadImage(bitmap, imageDataType)
        val outputProbabilityBuffer: TensorBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)

        // output buffer for probs
        val probabilityProcessor: TensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(PROB_MEAN, PROB_STD))
            .build()
        interpreter.run(inputImageBuffer!!.getBuffer(), outputProbabilityBuffer.getBuffer().rewind())
        return TensorLabel(labelList, probabilityProcessor.process(outputProbabilityBuffer))
            .getMapWithFloatValue()
    }

    private fun loadImage(bitmap: Bitmap, imageDataType: DataType): TensorImage {
        inputImageBuffer = TensorImage(imageDataType)
        inputImageBuffer!!.load(bitmap)

        //crete readable image for the interpreter
        val cropSize = Math.min(bitmap.width, bitmap.height)
        val imageProcessor: ImageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(IMAGE_MEAN, IMAGE_STD))
            .build()
        return imageProcessor.process(inputImageBuffer)
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class)
    private fun loadLabelList(labelPath: String): List<String> {
        return FileUtil.loadLabels(activity, labelPath)
    }

    companion object {
        private const val IMAGE_MEAN = 0.0f
        private const val IMAGE_STD = 255.0f
        private const val PROB_MEAN = 0.0f
        private const val PROB_STD = 1.0f
    }

    init {
        val options: Interpreter.Options = Interpreter.Options()
        interpreter = Interpreter(loadModelFile(activity.assets, modelPath), options)
        labelList = loadLabelList(labelPath)
    }
}