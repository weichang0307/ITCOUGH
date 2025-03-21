package com.example.itcough

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.itcough.`object`.Account
import com.example.itcough.`object`.GoogleService
import com.google.android.gms.auth.api.signin.GoogleSignInClient

const val REQUEST_CODE = 200
class MainActivity : ComponentActivity() {
    private lateinit var cardFile: CardView
    private lateinit var cardSetting: CardView
    private lateinit var cardCreate: CardView
    private lateinit var cardAnalysis: CardView
    private lateinit var cardStart: ImageButton
    private lateinit var btnAccount: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cardFile = findViewById(R.id.cardFile)
        cardStart = findViewById(R.id.cardStart)
        cardCreate = findViewById(R.id.cardCreate)
        cardSetting = findViewById(R.id.cardSettings)
        cardAnalysis = findViewById(R.id.cardAnalysis)
        btnAccount = findViewById(R.id.btnAccount)
        GoogleService.updateUserInfo(this)
        if (GoogleService.isSignIn(this)) {
            Account.sendGetUserInfoRequest(GoogleService.userID.toString())
            updateUserUI()
        }


        cardFile.setOnClickListener{
            if (Account.isSignIn(this)){
                startActivity(Intent(this, File_Activity::class.java))
            }else{
                startActivity(Intent(this, SetUpActivity::class.java))
            }
        }
        cardStart.setOnClickListener{
            if (Account.isSignIn(this)){
                startActivity(Intent(this, RecordPage::class.java))
            }else{
                startActivity(Intent(this, SetUpActivity::class.java))
            }
        }
        cardCreate.setOnClickListener{
            if (Account.isSignIn(this)){
                startActivity(Intent(this, CreateActivity::class.java))
            }else{
                startActivity(Intent(this, SetUpActivity::class.java))
            }
        }
        cardSetting.setOnClickListener{
            if (Account.isSignIn(this)){
                startActivity(Intent(this, SetUpActivity::class.java))
            }else{
                startActivity(Intent(this, SetUpActivity::class.java))
            }
        }
        cardAnalysis.setOnClickListener{
            if (Account.isSignIn(this)){
                startActivity(Intent(this, AnalysisActivity::class.java))
            }else{
                startActivity(Intent(this, SetUpActivity::class.java))
            }
        }
        btnAccount.setOnClickListener{
            startActivity(Intent(this, SetUpActivity::class.java))
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
        GoogleService.updateUserInfo(this)
        updateUserUI()
    }

    private fun updateUserUI() {
        Glide.with(this) // 使用 AppCompatActivity 或 FragmentActivity 作為上下文
            .load(GoogleService.userPhotoUrl ?: R.drawable.ic_person) // 處理空值
            .placeholder(R.drawable.ic_person) // 預設佔位符
            .circleCrop() // 圓形裁切
            .into(btnAccount)
    }


}
