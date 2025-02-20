package com.example.itcough.`object`

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.itcough.R
import com.google.gson.Gson
import java.lang.Exception
import java.net.HttpURLConnection
import kotlin.contracts.contract

object Account {
    var isSignUp: Boolean = false
    var userName: String? = null
    var userAge: Int? = null
    var userGender: String? = null
    var userEducation: String? = null
    var userMusicProficiency: Int? = null
    fun sendGetUserInfoRequest (
        userId: String,
        onRequestSuccess: (connection: HttpURLConnection) -> Unit = {},
        onRequestFail: (connection: HttpURLConnection) -> Unit = {},
        onConnectionFail: (exception: Exception) -> Unit = {}
    ) {
        val jsonData = Gson().toJson(mapOf(
            "userId" to userId
        ))
        Connection.sendJsonPostRequest(
            Connection.GET_USER_INFO_PATH,
            jsonData,
            onRequestSuccess = {connection ->
                val jsonObject = Connection.getJsonObject(connection)
                if (!jsonObject.getBoolean("isSignUp")) {
                    isSignUp = false
                }else{
                    val name = jsonObject.getString("name")
                    val age = jsonObject.getString("age").toInt()
                    val gender = jsonObject.getString("gender")
                    val education = jsonObject.getString("education")
                    val musicProficiency = jsonObject.getString("musicProficiency").toInt() - 1
                    isSignUp = true
                    userName = name
                    userAge = age
                    userGender = gender
                    userEducation = education
                    userMusicProficiency = musicProficiency
                }
                onRequestSuccess(connection)

            },
            onRequestFail = {connection ->
                isSignUp = false
                userName = null
                userAge = null
                userGender = null
                userEducation = null
                userMusicProficiency = null
                onRequestFail(connection)
            },
            onConnectionFail = {exception ->
                isSignUp = false
                userName = null
                userAge = null
                userGender = null
                userEducation = null
                userMusicProficiency = null
                onConnectionFail(exception)
            }

        )
    }
    fun sendSetUserInfoRequest (
        userId: String,
        userData: Map<String, String>,
        onRequestSuccess: (connection: HttpURLConnection) -> Unit = {},
        onRequestFail: (connection: HttpURLConnection) -> Unit = {},
        onConnectionFail: (exception: Exception) -> Unit = {}
    ) {
        val jsonData = Gson().toJson(userData)
        Connection.sendJsonPostRequest(
            Connection.SET_USER_INFO_PATH,
            jsonData,
            onRequestSuccess = onRequestSuccess,
            onRequestFail = onRequestFail,
            onConnectionFail = onConnectionFail
        )
    }
    fun isSignIn (context: Context): Boolean {
        return GoogleService.isSignIn(context) && isSignUp
    }
}