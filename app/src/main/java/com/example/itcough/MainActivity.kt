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
import com.example.itcough.`object`.GoogleService
import com.google.android.gms.auth.api.signin.GoogleSignInClient

const val REQUEST_CODE = 200
class MainActivity : ComponentActivity() {
    private lateinit var cardFile: CardView
    private lateinit var cardSetting: CardView
    private lateinit var cardCreate: CardView
    private lateinit var cardStart: ImageButton
    private lateinit var btnAccount: ImageButton
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cardFile = findViewById(R.id.cardFile)
        cardStart = findViewById(R.id.cardStart)
        cardCreate = findViewById(R.id.cardCreate)
        cardSetting = findViewById(R.id.cardSettings)
        btnAccount = findViewById(R.id.btnAccount)


        googleSignInClient = GoogleService.getGoogleSignInClient(this)
        GoogleService.updateUserInfo(this)
        updateUserUI()

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            GoogleService.onSignInResult(this, result)
            updateUserUI()
        }

        cardFile.setOnClickListener{
            if (GoogleService.isSignIn(this)){
                startActivity(Intent(this, File_Activity::class.java))
            }else{
                GoogleService.signIn(this, signInLauncher, googleSignInClient)
            }
        }
        cardStart.setOnClickListener{
            if (GoogleService.isSignIn(this)){
                startActivity(Intent(this, RecordPage::class.java))
            }else{
                GoogleService.signIn(this, signInLauncher, googleSignInClient)
            }
        }
        cardCreate.setOnClickListener{
            if (GoogleService.isSignIn(this)){
                startActivity(Intent(this, CreateActivity::class.java))
            }else{
                GoogleService.signIn(this, signInLauncher, googleSignInClient)
            }
        }
        cardSetting.setOnClickListener{
            if (GoogleService.isSignIn(this)){
                startActivity(Intent(this, SettingActivity::class.java))
            }else{
                GoogleService.signIn(this, signInLauncher, googleSignInClient)
            }
        }
        btnAccount.setOnClickListener{
            GoogleService.signIn(this, signInLauncher, googleSignInClient, true)
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
