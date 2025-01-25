package com.example.itcough
import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class YamnetModel(context: Context) {

    private val interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context, "yamnet.tflite"))
    }

    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }


    fun runInference(audioData: FloatArray): FloatArray {
        // 檢查輸入數據長度是否為 15600
        require(audioData.size == 15600) { "輸入數據長度必須為 15600，而不是 ${audioData.size}" }

        // 準備輸入數據：將 FloatArray 轉換為 ByteBuffer
        val inputBuffer = ByteBuffer.allocateDirect(audioData.size * 4).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().put(audioData)
        }

        // 準備輸出緩衝區：YAMNet 的輸出是 521 個分類的分數
        val outputBuffer = Array(1) { FloatArray(521) }

        // 運行推論
        interpreter.run(inputBuffer, outputBuffer)

        // 返回第一個輸出的結果
        return outputBuffer[0]
    }

    fun close() {
        interpreter.close()
    }
}
