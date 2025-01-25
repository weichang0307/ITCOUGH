package com.example.itcough.model

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.print.PrintAttributes.Margins
import android.util.Log
import com.example.itcough.Global
import com.example.itcough.MyApplication
import com.example.itcough.YamnetModel
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.MappedByteBuffer

object ContinuousAudioRecorder {
    private const val SAMPLE_RATE = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    private const val FRAME_SIZE = 31200 //SAMPLE_RATE * 2 * 0.975
    private const val MARGIN_SIZE_FRAMES = 2
    private const val MAX_COUGH_BUFFER_SIZE_FRAMES = 10
    private const val MARGIN_SIZE = FRAME_SIZE * MARGIN_SIZE_FRAMES
    private const val MAX_COUGH_BUFFER_SIZE = FRAME_SIZE * MAX_COUGH_BUFFER_SIZE_FRAMES
    private const val COUGH_THRESHOLD = 0.2


    private var audioRecord: AudioRecord? = null

    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    public var isRecording = false
    public var yamnetModel: YamnetModel? = null

    private var marginFrameAmount = 0

    val coughBuffer = ByteArrayOutputStream()
    val marginBuffer = ByteArray(MARGIN_SIZE)



    @SuppressLint("MissingPermission")
    fun startRecording() {

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()


        // Continuous recording and upload every 1 second
        coroutineScope.launch {
            val buffer = ByteArray(FRAME_SIZE)
            var bytesRead: Int
            isRecording = true
            while (isRecording) {
                bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                Thread {
                    if (bytesRead < FRAME_SIZE){
                        if (coughBuffer.size() > 0){
                            coughBuffer.write(buffer, 0, bytesRead)
                            saveCoughAudio()
                        }
                    }else{
                        if (yamnetModel != null){
                            val output = yamnetModel?.runInference(byteArrayToFloatArray(buffer))
                            val confidence = output?.get(42)
                            Log.d("myTag","分類ID: cough, 分數: $confidence")
                            if (confidence != null && confidence > COUGH_THRESHOLD){
                                if (coughBuffer.size() == 0){
                                    coughBuffer.write(marginBuffer, 0, marginFrameAmount * FRAME_SIZE)
                                }
                                marginFrameAmount = 0
                                coughBuffer.write(buffer, 0, FRAME_SIZE)
                                if (coughBuffer.size()>MAX_COUGH_BUFFER_SIZE) {
                                    saveCoughAudio()
                                }
                            }else{
                                if (coughBuffer.size() > 0) {
                                    if (marginFrameAmount < MARGIN_SIZE_FRAMES) {
                                        coughBuffer.write(buffer, 0, FRAME_SIZE)
                                        System.arraycopy(buffer, 0, marginBuffer, marginFrameAmount * FRAME_SIZE, FRAME_SIZE)
                                        marginFrameAmount++
                                    }else{
                                        saveCoughAudio()
                                        System.arraycopy(marginBuffer, FRAME_SIZE, marginBuffer, 0, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE)
                                        System.arraycopy(buffer, 0, marginBuffer, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE, FRAME_SIZE)
                                        marginFrameAmount = MARGIN_SIZE_FRAMES
                                    }
                                }else{
                                    System.arraycopy(marginBuffer, FRAME_SIZE, marginBuffer, 0, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE)
                                    System.arraycopy(buffer, 0, marginBuffer, (MARGIN_SIZE_FRAMES - 1) * FRAME_SIZE, FRAME_SIZE)
                                    marginFrameAmount = MARGIN_SIZE_FRAMES
                                }


                            }


                        }


                    }
                }.start()
            }

        }
    }
    private fun saveCoughAudio() { 5555
        val dataArray = coughBuffer.toByteArray()
        coughBuffer.reset()
        sendCreateCoughAudioRequest(dataArray)
    }
    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        yamnetModel?.close()

    }

    private fun sendCreateCoughAudioRequest(dataArray: ByteArray) {
        try {
            val url = URL("${Global.url}/create_cough_audio/")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 5000
                readTimeout = 20000
                doOutput = true
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/octet-stream")
            }
            connection.outputStream.use { outputStream ->
                outputStream.write(dataArray)
                outputStream.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("myTag", "Upload successful")
                Log.d("myTag", "cough file saved")
            } else {
                Log.d("myTag", "Upload failed with response code: $responseCode")
            }
        } catch (e: IOException) {
            Log.e("myTag", "Error uploading data", e)
        }
    }
    private fun byteArrayToFloatArray(buffer: ByteArray): FloatArray {
        val floatArray = FloatArray(buffer.size / 2) // 每兩個字節組成一個浮點數
        for (i in floatArray.indices) {
            val low = buffer[i * 2].toInt() and 0xFF
            val high = buffer[i * 2 + 1].toInt() shl 8
            val sample = high or low // 合成 16 位有符號整數
            floatArray[i] = sample / 32768.0f // 轉換為範圍 [-1.0, 1.0]
        }
        return floatArray
    }
}
