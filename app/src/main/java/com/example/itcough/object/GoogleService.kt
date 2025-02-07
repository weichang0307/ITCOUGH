package com.example.itcough.`object`

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat.getString
import com.example.itcough.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson

object GoogleService {
    const val RC_SIGN_IN = 100
    var userEmail: String? = null
    var userID: String? = null
    var userPhotoUrl: Uri? = null
    var userName: String? = null

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(context, R.string.default_web_client_id)) // 替換為您的 OAuth 客戶端 ID
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
    fun getAccount(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account
    }
    fun isSignIn(context: Context): Boolean {
        return getAccount(context) != null
    }
    fun updateUserInfo(context: Context) {
        val account = getAccount(context)
        userEmail = account?.email
        userID = account?.id
        userPhotoUrl = account?.photoUrl
        userName = account?.displayName
        sendUpdateUserInfoRequest()
    }
    fun signIn(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        googleSignInClient: GoogleSignInClient,
        switchUserIfExist: Boolean = false
    ) {
        val account = getAccount(activity)
        if (account != null) {
            if (switchUserIfExist) {
                signOut(activity, googleSignInClient) {
                    launcher.launch(googleSignInClient.signInIntent)
                }
                return
            } else {
                return
            }
        }
        launcher.launch(googleSignInClient.signInIntent)
    }
    fun onSignInResult (
        context: Context,
        result: ActivityResult,
        onSignInSuccessful: () -> Unit = {},
        onSignInFailed: () -> Unit = {}
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            Log.d("GoogleService", "Sign In Successful: ${task.getResult(ApiException::class.java).email}")
            updateUserInfo(context)
            onSignInSuccessful()
            // 這裡你可以將頭像 URL 顯示在 UI 中，例如 ImageView
        } catch (e: ApiException) {
            Log.e("GoogleService", "Sign In Failed: ${e.toString()}")
            updateUserInfo(context)
            onSignInFailed()
        }
    }
    @JvmStatic
    fun signOut(activity: Activity, googleSignInClient: GoogleSignInClient, onSignOutComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener(activity) {
            updateUserInfo(activity)
            onSignOutComplete()
        }
    }
    private fun sendUpdateUserInfoRequest() {
        val jsonData = Gson().toJson(mapOf(
            "userId" to userID,
            "userEmail" to userEmail,
            "userName" to userName
        ))
        Connection.sendJsonPostRequest(
            Connection.UPDATE_USER_INFO_PATH,
            jsonData,
        )
    }
}