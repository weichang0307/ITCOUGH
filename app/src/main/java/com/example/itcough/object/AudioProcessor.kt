package com.example.itcough.`object`

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

object AudioProcessor {

    fun convertToPCM(context: Context, audioUri: Uri, sampleRate: Int = 16000, channel: Int = 1, sampleWidth: Int = 2): ByteArray? {
        return try {
            // 取得 FFmpeg 可存取的臨時音檔路徑
            val inputPath = getRealPathFromURI(context, audioUri) ?: return null
            val outputFile = File(context.cacheDir, "converted_audio.pcm")

            // FFmpeg 指令：轉換為 16-bit PCM, 16000 Hz, 單聲道
            val command = "-y -i \"$inputPath\" -ac $channel -ar $sampleRate -f s${8 * sampleWidth}le \"${outputFile.absolutePath}\""

            // 執行 FFmpeg 轉換
            val session = FFmpegKit.execute(command)

            // 確保轉換成功，讀取檔案內容回傳 `ByteArray`
            if (session.returnCode.isValueSuccess && outputFile.exists()) {
                return outputFile.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 獲取 URI 對應的真實檔案路徑
    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        val file = File(context.cacheDir, "temp_audio_file")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}
