package com.example.itcough

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.GoogleService
import com.google.gson.Gson

class FragmentMusic : Fragment() {
    private lateinit var tvDay: TextView
    private lateinit var tvWeek: TextView
    private lateinit var tvMonth: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_music, container, false)

        tvDay = view.findViewById(R.id.tvDay)
        tvWeek = view.findViewById(R.id.tvWeek)
        tvMonth = view.findViewById(R.id.tvMonth)
        getAnalysisData()  // 获取数据

        return view
    }
    private fun getAnalysisData() : Map<String, Int>? {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID
        ))
        Connection.sendJsonPostRequest(
            "get_music_statistics/",
            jsonData,
            onRequestSuccess = { con ->
                val resData = Connection.getJsonObject(con)
                Log.d("myTag", resData.toString())
                requireActivity().runOnUiThread {
                    tvDay.text = "day: ${resData.getString("day")}"
                    tvWeek.text = "week: ${resData.getString("week")}"
                    tvMonth.text = "month: ${resData.getString("month")}"
                }

            }
        )
        return null
    }
}