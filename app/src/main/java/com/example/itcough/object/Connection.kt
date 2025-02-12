package com.example.itcough.`object`

import android.util.Log
import com.example.itcough.model.AudioRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Connection {
    const val SAVE_COUGH_AUDIO_PATH = "create_cough_audio/"
    const val GET_COUGH_AUDIO_LIST_PATH = "get_coughs/"
    const val GENERATE_MUSIC_PATH = "generate/"
    const val SAVE_GENERATED_MUSIC_PATH = "save_music/"
    const val CLEAN_TEMPER_PATH = "clean_temper/"
    const val SET_USER_INFO_PATH = "set_user_info/"
    const val SIGN_UP_PATH = "sign_up/"
    const val GET_USER_INFO_PATH = "get_user_info/"
    fun sendJsonPostRequest(
        path: String,
        jsonData: String,
        onRequestSuccess: (connection: HttpURLConnection) -> Unit = {},
        onRequestFail: (connection: HttpURLConnection) -> Unit = {},
        onConnectionFail: (e: Exception) -> Unit = {}
    ) {
        Thread {
            val urlString = "${Global.URL}/$path"
            val url = URL(urlString)
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 500000
                    doInput = true
                    doOutput = true // 允許輸出
                    setRequestProperty("Content-Type", "application/json")
                }
                // 創建要傳輸的 JSON 數據
                val outputStream = connection.outputStream
                outputStream.write(jsonData.toByteArray())
                outputStream.flush()
                outputStream.close()
                // 读取响应
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Connection", "jsonPostRequest to $urlString was successful")
                    onRequestSuccess(connection)
                } else {
                    Log.d("Connection", "jsonPostRequest to $urlString failed with response code: $responseCode")
                    onRequestFail(connection)
                }
            } catch (e: Exception) {
                Log.e("Connection", "Error sending jsonPostRequest request to $urlString", e)
                onConnectionFail(e)
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
    fun sendAudioWithJsonPostRequest(
        path: String,
        jsonData: String,
        dataArray: ByteArray,
        onRequestSuccess: (connection: HttpURLConnection) -> Unit = {},
        onRequestFail: (connection: HttpURLConnection) -> Unit = {},
        onConnectionFail: (e: Exception) -> Unit = {}
    ) {
        Thread {
            val urlString = "${Global.URL}/$path"
            val url = URL(urlString)
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                val boundary = "Boundary-" + System.currentTimeMillis()
                connection.apply {
                    doOutput = true
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                }
                connection.outputStream.use { outputStream ->
                    val writer = outputStream.bufferedWriter()

                    // 添加 JSON 數據部分
                    writer.write("--$boundary\r\n")
                    writer.write("Content-Disposition: form-data; name=\"metadata\"\r\n\r\n")
                    writer.write(jsonData)
                    writer.write("\r\n")

                    // 添加文件部分
                    writer.write("--$boundary\r\n")
                    writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n")
                    writer.write("Content-Type: audio/wav\r\n\r\n")
                    writer.flush()

                    outputStream.write(dataArray)  // 寫入音檔數據
                    outputStream.flush()

                    writer.write("\r\n--$boundary--\r\n")
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Connection", "jsonPostRequest to $urlString was successful")
                    onRequestSuccess(connection)
                } else {
                    Log.d("Connection", "jsonPostRequest to $urlString failed with response code: $responseCode")
                    onRequestFail(connection)
                }
            } catch (e: Exception) {
                Log.e("Connection", "Error sending jsonPostRequest request to $urlString", e)
                onConnectionFail(e)
            } finally {
                connection?.disconnect()
            }
        }.start()

    }
    fun jsonToMap(jsonString: String): Map<String, String> {
        val jsonObject = JSONObject(jsonString)
        val map = mutableMapOf<String, String>()
        val iterator = jsonObject.keys()

        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = jsonObject.get(key).toString()
        }
        return map
    }
    fun getJsonObject(connection: HttpURLConnection): JSONObject {
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(response)
        return jsonObject
    }
    fun parseAudioRecords(jsonString: String): List<AudioRecord> {
        val gson = Gson()
        val listType = object : TypeToken<List<AudioRecord>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}