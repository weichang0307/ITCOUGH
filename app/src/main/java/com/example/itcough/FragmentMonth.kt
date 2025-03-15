package com.example.itcough

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.GoogleService
import com.google.gson.Gson
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class FragmentMonth : Fragment() {
    private lateinit var scatterChartView: ScatterChartView
    private lateinit var tvDuration: TextView
    private lateinit var tvAmount: TextView
    private var startDate: LocalDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_month, container, false)
        scatterChartView = view.findViewWithTag("chart")
        tvDuration = view.findViewById(R.id.tvDuration)
        tvAmount = view.findViewById(R.id.tvAmount)
        getAnalysisData()  // 获取数据
        val btnLast = view.findViewById<ImageButton>(R.id.btnLast)
        btnLast.setOnClickListener {
            startDate = startDate.minusMonths(1)
            getAnalysisData()
        }
        val btnNext = view.findViewById<ImageButton>(R.id.btnNext)
        btnNext.setOnClickListener {
            startDate = startDate.plusMonths(1)
            getAnalysisData()
        }

        return view
    }
    private fun getAnalysisData() : Map<String, Int>? {
        val jsonData = Gson().toJson(mapOf(
            "userId" to GoogleService.userID,
            "startDate" to startDate.toString(), //yyyy-mm-dd
            "endDate" to startDate.plusMonths(1).toString() //yyyy-mm-dd
        ))
        Connection.sendJsonPostRequest(
            "get_cough_statistics/",
            jsonData,
            onRequestSuccess = { con ->
                val resData = Connection.getJsonObject(con)
                Log.d("myTag", resData.toString())
                requireActivity().runOnUiThread {
                    val jsonArray = resData.getJSONArray("allTime")
                    val data: List<Pair<Int, Float>> = List(jsonArray.length()) { index->
                        val dateString = jsonArray.getString(index).toString()
                        val dates = dateString.split('-').map {it.toInt()}
                        val day = LocalDate.of(dates[0], dates[1], dates[2])
                        val hour = dates[3]
                        val min = dates[4]
                        val time = hour + min.toFloat()/60
                        Pair(ChronoUnit.DAYS.between(startDate, day).toInt() + 1,time.toFloat())
                    }

                    Log.d("myTag", data.toString())
                    tvDuration.text = startDate.toString().substringBeforeLast('-').replace('-','/')
                    tvAmount.text = jsonArray.length().toString()
                    scatterChartView.updateData(data, ScatterChartView.MONTH)
                }
            },
            onRequestFail = {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "Fail to get analysis data", Toast.LENGTH_SHORT).show()
                }
            },
            onConnectionFail = {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "Fail to connect to server", Toast.LENGTH_SHORT).show()
                }
            }
        )
        return null
    }
}