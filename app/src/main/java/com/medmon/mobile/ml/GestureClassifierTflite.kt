package com.medmon.mobile.ml

import android.content.Context
import android.util.Log
import com.medmon.mobile.model.ImuWindow
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class GestureClassifierTflite(
    context: Context,
    private val modelFileName: String = "gesture_model.tflite"
) {

    private val labels = listOf("G1", "G2", "G3", "G4", "G5", "T")

    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context), Interpreter.Options())
        Log.d("GestureClassifier", "TFLite model loaded: $modelFileName")
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFileName)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    /**
     * Recebe uma janela ImuWindow (75 x 6) e retorna (label, confidence)
     */
    fun classify(window: ImuWindow): Pair<String, Float> {
        // Preparar input [1, 75, 6]
        val timeSteps = 75
        val numFeatures = 6

        val input = Array(1) { Array(timeSteps) { FloatArray(numFeatures) } }

        val n = minOf(window.data.size, timeSteps)
        for (i in 0 until n) {
            val row = window.data[i]
            // Garantir tamanho 6
            val accX = row.getOrNull(0) ?: 0f
            val accY = row.getOrNull(1) ?: 0f
            val accZ = row.getOrNull(2) ?: 0f
            val gyrX = row.getOrNull(3) ?: 0f
            val gyrY = row.getOrNull(4) ?: 0f
            val gyrZ = row.getOrNull(5) ?: 0f

            input[0][i][0] = accX
            input[0][i][1] = accY
            input[0][i][2] = accZ
            input[0][i][3] = gyrX
            input[0][i][4] = gyrY
            input[0][i][5] = gyrZ
        }

        // Se vier menos de 75 amostras, o resto fica em zero (já está)

        // Output [1, numClasses]
        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(input, output)

        val scores = output[0]
        var maxIdx = 0
        var maxScore = scores[0]
        for (i in 1 until scores.size) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxIdx = i
            }
        }

        val label = labels.getOrElse(maxIdx) { "T" } // default pra algo neutro
        return label to maxScore
    }
}
