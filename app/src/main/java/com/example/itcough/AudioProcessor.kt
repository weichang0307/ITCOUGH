package com.example.itcough

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.RandomAccessFile

class AudioProcessor(private val context: Context) {

    private val sampleRate = 16000 // 16kHz
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )

    // 錄音方法，確保返回的數據長度為 15600，並保存為 WAV 文件
    fun startRecording(): FloatArray {
        val shortBuffer = ShortArray(bufferSize / 2)
        val floatBuffer = FloatArray(15600) // 固定長度為 15600 的浮點數數組

        audioRecord.startRecording()
        var totalReadSize = 0

        // 準備保存 WAV 文件
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "recording.wav"
        )
        val fileOutputStream = FileOutputStream(file)

        // 寫入 WAV 文件的初始頭
        writeWavHeader(fileOutputStream, sampleRate, 1, 16)

        try {
            while (totalReadSize < 15600) {
                val readSize = audioRecord.read(shortBuffer, 0, shortBuffer.size)
                for (i in 0 until readSize) {
                    if (totalReadSize < 15600) {
                        floatBuffer[totalReadSize] = shortBuffer[i] / 32768.0f

                        // 將短整數 PCM 數據寫入文件
                        fileOutputStream.write(shortBuffer[i].toByte().toInt())
                        fileOutputStream.write((shortBuffer[i].toInt() shr 8).toByte().toInt())

                        totalReadSize++
                    }
                }
            }
        } finally {
            fileOutputStream.close()
        }

        // 更新 WAV 文件頭
        updateWavHeader(file, totalReadSize * 2) // 每個樣本 2 字節

        return floatBuffer
    }

    fun stopRecording() {
        audioRecord.stop()
        audioRecord.release()
    }

    // 寫入 WAV 文件頭
    private fun writeWavHeader(
        outputStream: FileOutputStream,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray()) // Chunk ID
        header.putInt(0) // Chunk Size (稍後更新)
        header.put("WAVE".toByteArray()) // Format
        header.put("fmt ".toByteArray()) // Subchunk1 ID
        header.putInt(16) // Subchunk1 Size (PCM格式固定為16)
        header.putShort(1) // Audio Format (1表示PCM)
        header.putShort(channels.toShort()) // Num Channels
        header.putInt(sampleRate) // Sample Rate
        header.putInt(byteRate) // Byte Rate
        header.putShort((channels * bitsPerSample / 8).toShort()) // Block Align
        header.putShort(bitsPerSample.toShort()) // Bits Per Sample
        header.put("data".toByteArray()) // Subchunk2 ID
        header.putInt(0) // Subchunk2 Size (稍後更新)

        outputStream.write(header.array())
    }

    // 更新 WAV 文件頭
    private fun updateWavHeader(file: File, dataSize: Int) {
        val randomAccessFile = RandomAccessFile(file, "rw")
        try {
            // 更新 Chunk Size
            randomAccessFile.seek(4)
            randomAccessFile.writeInt(36 + dataSize)

            // 更新 Subchunk2 Size
            randomAccessFile.seek(40)
            randomAccessFile.writeInt(dataSize)
        } finally {
            randomAccessFile.close()
        }
    }
}
