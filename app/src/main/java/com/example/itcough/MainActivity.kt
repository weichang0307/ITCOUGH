package com.example.itcough

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

const val REQUEST_CODE = 200
class MainActivity : ComponentActivity() {
    private lateinit var cardFile: CardView
    private lateinit var cardSetting: CardView
    private lateinit var cardCreate: CardView
    private lateinit var cardStart: ImageButton
    private lateinit var btnAccount: ImageButton
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cardFile = findViewById(R.id.cardFile)
        cardStart = findViewById(R.id.cardStart)
        cardCreate = findViewById(R.id.cardCreate)
        cardSetting = findViewById(R.id.cardSettings)
        btnAccount = findViewById(R.id.btnAccount)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // 替換為您的 OAuth 客戶端 ID
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUserInfo(account)


        cardFile.setOnClickListener{
            if (Global.userID != null){
                startActivity(Intent(this, File_Activity::class.java))
            }else{
                signIn()
            }
        }
        cardStart.setOnClickListener{
            if (Global.userID != null){
                startActivity(Intent(this, RecordPage::class.java))
            }else{
                signIn()
            }
        }
        cardCreate.setOnClickListener{
            if (Global.userID != null){
                startActivity(Intent(this, CreateActivity::class.java))
            }else{
                signIn()
            }
        }
        cardSetting.setOnClickListener{
            if (Global.userID != null){
                startActivity(Intent(this, SettingActivity::class.java))
            }else{
                signIn()
            }
        }
        btnAccount.setOnClickListener{
            if (Global.userID != null) signOut()
            signIn()
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        }





    }

    override fun onRestart() {
        super.onRestart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUserInfo(account)
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

    private fun updateUserInfo(account: GoogleSignInAccount?) {
        Global.userEmail = account?.email
        Global.userID = account?.id
        Global.userPhotoUrl = account?.photoUrl
        Global.userName = account?.displayName
        Glide.with(this)
            .load(Global.userPhotoUrl)
            .placeholder(R.drawable.ic_person) // 預設圖片
            .circleCrop()
            .into(btnAccount)
        if (Global.userID != null) sendCreateUserRequest()
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
                val jsonPayload = """{"userId": "${Global.userID}"}"""
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
