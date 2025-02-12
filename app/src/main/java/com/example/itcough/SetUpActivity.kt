package com.example.itcough

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.itcough.`object`.Account
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.Global
import com.example.itcough.`object`.GoogleService
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.gson.Gson
import java.net.HttpURLConnection

class SetUpActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageProfileView: ImageView
    private lateinit var signInButton: Button
    private lateinit var infoView: ScrollView
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var seekBarMusicProficiency: SeekBar
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var radioGroupEducation: RadioGroup
    private lateinit var connectingMask: LinearLayout
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up)

        imageProfileView = findViewById(R.id.imageProfileView)
        infoView = findViewById(R.id.infoView)
        nameInput = findViewById(R.id.nameInput)
        ageInput = findViewById(R.id.ageInput)
        signInButton = findViewById(R.id.signInButton)
        seekBarMusicProficiency = findViewById(R.id.seekBarMusicProficiency)
        radioGroupEducation = findViewById(R.id.radioGroupEducation)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        connectingMask = findViewById(R.id.connectingMask)
        signUpButton = findViewById(R.id.signUpButton)
        googleSignInClient = GoogleService.getGoogleSignInClient(this)
        GoogleService.updateUserInfo(this)
        updateUserUI()

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            GoogleService.onSignInResult(this, result)
            updateUserUI()
        }
        signInButton.setOnClickListener {
            if (GoogleService.isSignIn(this)) {
                connectingMask.isVisible = true
                GoogleService.signOut(this, googleSignInClient) {
                    connectingMask.isVisible = false
                    updateUserUI()
                }
            }else {
                connectingMask.isVisible = true
                GoogleService.signIn(this, signInLauncher, googleSignInClient, true)
            }
        }
        signUpButton.setOnClickListener {
            sendSignUpRequest()
        }
        setEditTextAction(nameInput) {
            if (GoogleService.isSignIn(this) && Account.isSignUp) {
                Account.sendSetUserInfoRequest(
                    GoogleService.userID.toString(),
                    mapOf("userName" to nameInput.text.toString())
                )
            }
        }
        setEditTextAction(ageInput) {
            if (GoogleService.isSignIn(this) && Account.isSignUp) {
                Account.sendSetUserInfoRequest(
                    GoogleService.userID.toString(),
                    mapOf("userAge" to ageInput.text.toString())
                )
            }
        }
        radioGroupGender.setOnCheckedChangeListener(){ _,id->
            if (GoogleService.isSignIn(this) && Account.isSignUp) {
                val gender = when (id) {
                    R.id.radioMale -> "Male"
                    R.id.radioFemale -> "Female"
                    R.id.radioOther -> "Other"
                    else -> null
                }
                Account.sendSetUserInfoRequest(
                    GoogleService.userID.toString(),
                    mapOf("userGender" to gender.toString())
                )
            }
        }
        radioGroupEducation.setOnCheckedChangeListener(){ _,id->
            if (GoogleService.isSignIn(this) && Account.isSignUp) {
                val education = when (id) {
                    R.id.radioHighSchool -> "High School"
                    R.id.radioBachelor -> "Bachelor"
                    R.id.radioMaster -> "Master"
                    R.id.radioDoctorate -> "Doctorate"
                    else -> null
                }
                Account.sendSetUserInfoRequest(
                    GoogleService.userID.toString(),
                    mapOf("userEducation" to education.toString())
                )
            }
        }
        val tvValue = findViewById<TextView>(R.id.tvMusicProficiencyValue)
        seekBarMusicProficiency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val level = progress + 1 // Convert from 0-based to 1-based scale
                tvValue.text = "$level/10"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (GoogleService.isSignIn(this@SetUpActivity) && Account.isSignUp) {
                    Account.sendSetUserInfoRequest(
                        GoogleService.userID.toString(),
                        mapOf("musicProficiency" to (seekBarMusicProficiency.progress + 1).toString())
                    )
                }
            }
        })


    }
    private fun updateUserUI() {

        runOnUiThread {
            Glide.with(this) // 使用 AppCompatActivity 或 FragmentActivity 作為上下文
                .load(GoogleService.userPhotoUrl ?: R.drawable.ic_person) // 處理空值
                .placeholder(R.drawable.ic_person) // 預設佔位符
                .circleCrop() // 圓形裁切
                .into(imageProfileView)
            if (GoogleService.isSignIn(this)) {
                connectingMask.isVisible = true
                Account.sendGetUserInfoRequest(
                    GoogleService.userID.toString(),
                    onRequestSuccess = {
                        runOnUiThread {
                            setUserData()
                            connectingMask.isVisible = false
                        }
                    },
                    onRequestFail = {
                        runOnUiThread {
                            Toast.makeText(this, "Fail to get user data from server", Toast.LENGTH_SHORT).show()
                            connectingMask.isVisible = false
                        }
                    },
                    onConnectionFail = {
                        runOnUiThread {
                            Toast.makeText(this, "Fail to connect to server", Toast.LENGTH_SHORT).show()
                            connectingMask.isVisible = false
                        }
                    }
                )
            } else {
                switchToSignInMode()
            }
        }
    }
    @SuppressLint("RestrictedApi")
    private fun setEditTextAction(editText: EditText, onUnFocusAction: () -> Unit = {}) {
        editText.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            editText.getWindowVisibleDisplayFrame(rect)
            val screenHeight = editText.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight < screenHeight * 0.15) { // Keyboard is closed
                editText.clearFocus()
            }
        }
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                onUnFocusAction()
            }
        }

    }
    private fun getUserData () : Map<String, String>?{
        val name = nameInput.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "User name can not be empty", Toast.LENGTH_SHORT).show()
            return null
        }
        val age = ageInput.text.toString()
        if (age.isEmpty()) {
            Toast.makeText(this, "User age can not be empty", Toast.LENGTH_SHORT).show()
            return null
        }
        val gender = when (radioGroupGender.checkedRadioButtonId) {
            R.id.radioMale -> "Male"
            R.id.radioFemale -> "Female"
            R.id.radioOther -> "Other"
            else -> null
        }
        if (gender == null) {
            Toast.makeText(this, "choose your gender", Toast.LENGTH_SHORT).show()
            return null
        }
        val education = when (radioGroupEducation.checkedRadioButtonId) {
            R.id.radioHighSchool -> "High School"
            R.id.radioBachelor -> "Bachelor"
            R.id.radioMaster -> "Master"
            R.id.radioDoctorate -> "Doctorate"
            else -> null
        }
        if (education == null) {
            Toast.makeText(this, "choose your education level", Toast.LENGTH_SHORT).show()
            return null
        }
        val musicProficiency = (seekBarMusicProficiency.progress + 1).toString()
        return mapOf(
            "userId" to GoogleService.userID.toString(),
            "name" to name,
            "age" to age,
            "gender" to gender,
            "education" to education ,
            "musicProficiency" to musicProficiency
        )
    }
    @SuppressLint("SetTextI18n")
    private fun setUserData () {
        if (!Account.isSignUp) {
            switchToSignUpMode()
            return
        }else{
            nameInput.setText(Account.userName)
            ageInput.setText(Account.userAge.toString())
            radioGroupGender.check(
                when (Account.userGender) {
                    "Male" -> R.id.radioMale
                    "Female" -> R.id.radioFemale
                    "Other" -> R.id.radioOther
                    else -> R.id.radioGroupGender
                }
            )
            radioGroupEducation.check(
                when (Account.userEducation) {
                    "High School" -> R.id.radioHighSchool
                    "Bachelor" -> R.id.radioBachelor
                    "Master" -> R.id.radioMaster
                    "Doctorate" -> R.id.radioDoctorate
                    else -> R.id.radioGroupEducation
                }
            )
            seekBarMusicProficiency.progress = Account.userMusicProficiency!!
            switchToSettingMode()

        }
    }

    private fun sendSignUpRequest () {
        val userData = getUserData() ?: return
        val jsonData = Gson().toJson(userData)
        connectingMask.isVisible = true
        Connection.sendJsonPostRequest(
            Connection.SIGN_UP_PATH,
            jsonData,
            onRequestSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "User sign up successfully", Toast.LENGTH_SHORT).show()
                    updateUserUI()
                    connectingMask.isVisible = false
                }
            },
            onRequestFail = {
                runOnUiThread {
                    Toast.makeText(this, "Fail to save user profile to server", Toast.LENGTH_SHORT).show()
                    connectingMask.isVisible = false
                }
            },
            onConnectionFail = {
                runOnUiThread {
                    Toast.makeText(this, "Fail to connect to server", Toast.LENGTH_SHORT).show()
                    connectingMask.isVisible = false
                }
            }

        )
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            exitPage()
            return true;
        }
        return super.onKeyDown(keyCode, event)
    }
    private fun exitPage() {
        if (GoogleService.isSignIn(this) && !Account.isSignUp){
            showHasNotSingUpDialog(this)
        }else {
            finish()
        }
    }
    private fun showHasNotSingUpDialog(context: Context) {
        AlertDialog.Builder(context)
            .setMessage("The user has not been signed up\n You sure you want to leave the page?")
            .setNegativeButton("Leave") { _, _ ->
                finish() // 用户确认放弃时调用
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss() // 只关闭对话框，什么也不做
            }
            .show()
    }
    private fun switchToSignUpMode() {
        signUpButton.isVisible = true
        infoView.isVisible = true
        nameInput.setText("")
        ageInput.setText("")
        radioGroupGender.clearCheck()
        radioGroupEducation.clearCheck()
        seekBarMusicProficiency.progress = 4
        signInButton.text = "Sign Out"
    }
    private fun switchToSettingMode() {
        signUpButton.isVisible = false
        infoView.isVisible = true
        signInButton.text = "Sign Out"
    }
    private fun switchToSignInMode() {
        signUpButton.isVisible = false
        infoView.isVisible = false
        signInButton.text = "Sign In"
    }
}
