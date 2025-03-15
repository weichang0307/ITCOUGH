package com.example.itcough

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.viewpager2.widget.ViewPager2
import com.example.itcough.ui.theme.ITCOUGHTheme
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)



        // 设置 ViewPager 适配器
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        val topbar = findViewById<View>(R.id.topBarLayout)
        topbar.findViewById<ImageButton>(R.id.btnLeft).setOnClickListener{
            finish()
        }
        val btnRight = topbar.findViewById<ImageButton>(R.id.btnRight)
        btnRight.setOnClickListener{
            viewPager.adapter = ViewPagerAdapter(this)
        }
        btnRight.setBackgroundResource(R.drawable.ic_renew)

        // 绑定 TabLayout 和 ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "day"
                1 -> "week"
                2 -> "month"
                else -> "Tab"
            }
        }.attach()
    }
}
