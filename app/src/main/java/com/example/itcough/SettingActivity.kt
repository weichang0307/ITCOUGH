package com.example.itcough

import android.annotation.SuppressLint
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.itcough.`object`.Global
import com.example.itcough.`object`.GoogleService
import com.google.android.gms.tasks.Task
import java.net.HttpURLConnection
import java.net.URL

class SettingActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnAccount: ImageButton
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        btnAccount = findViewById(R.id.btnAccount)
        tvName =findViewById(R.id.tvName)
        tvEmail =findViewById(R.id.tvEmail)

        // 配置 Google 登入
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 替換為您的 OAuth 客戶端 ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 檢查是否有已登入的帳戶
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUserInfo(account)

        btnAccount.setOnClickListener{
            if (GoogleService.userID != null) signOut()
            signIn()
        }

    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Log.d("GoogleSignIn", "使用者已登出")
            updateUserInfo(null)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GoogleSignIn", "登入成功: ${account?.email}")

            updateUserInfo(account)



            // 這裡你可以將頭像 URL 顯示在 UI 中，例如 ImageView

        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "登入失敗: ${e.statusCode}")
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateUserInfo(account: GoogleSignInAccount?) {
        GoogleService.userEmail = account?.email
        GoogleService.userID = account?.id
        GoogleService.userPhotoUrl = account?.photoUrl
        GoogleService.userName = account?.displayName
        Glide.with(this)
            .load(GoogleService.userPhotoUrl)
            .placeholder(R.drawable.ic_person) // 預設圖片
            .circleCrop()
            .into(btnAccount)
        if (GoogleService.userID != null) sendCreateUserRequest()
        tvName.text = "name: " + GoogleService.userName
        tvEmail.text = "email: " + GoogleService.userEmail
        Log.d("GoogleSignIn", "已登入的用戶: ${account?.email}")
    }
    private fun sendCreateUserRequest() {
        Thread {
            val urlString = "${Global.URL}/create_user"
            val url = URL(urlString)
            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 5000
                    doInput = true
                    doOutput = true // 允許輸出
                    setRequestProperty("Content-Type", "application/json") // 設置請求類型為 JSON
                }

                // 創建要傳輸的 JSON 數據
                val jsonPayload = """{"userId": "${GoogleService.userID}"}"""
                val outputStream = connection.outputStream
                outputStream.write(jsonPayload.toByteArray())
                outputStream.flush()
                outputStream.close()

                // 讀取響應
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("myTest", "Request was successful")
                } else {
                    Log.d("myTag", "Request failed with response code: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("myTag", "Error sending POST request", e)
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
