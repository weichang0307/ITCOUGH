package com.example.itcough

import android.content.Intent
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
import com.example.itcough.GalleryActivity
import com.example.itcough.ui.theme.ITCOUGHTheme

class File_Activity : ComponentActivity() {
    private lateinit var myMusic: CardView
    private lateinit var myCough: CardView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        myMusic = findViewById(R.id.cardMyMusic)
        myCough = findViewById(R.id.cardMyCough)
        myMusic.setOnClickListener{
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        myCough.setOnClickListener{
            startActivity(Intent(this, CoughActivity::class.java))
        }
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnLeft.setOnClickListener {
            finish()
        }
        btnRight.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

}
