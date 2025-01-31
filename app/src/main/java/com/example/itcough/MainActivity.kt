package com.example.itcough

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.itcough.ui.theme.ITCOUGHTheme
const val REQUEST_CODE = 200
class MainActivity : ComponentActivity() {
    private lateinit var cardFile: CardView
    private lateinit var cardCreate: CardView
    private lateinit var cardStart: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cardFile = findViewById(R.id.cardFile)
        cardStart = findViewById(R.id.cardStart)
        cardCreate = findViewById(R.id.cardCreate)

        cardFile.setOnClickListener{
            startActivity(Intent(this, File_Activity::class.java))
        }
        cardStart.setOnClickListener{
            startActivity(Intent(this, RecordPage::class.java))
        }
        cardCreate.setOnClickListener{
            startActivity(Intent(this, CreateActivity::class.java))
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
}
