package com.example.itcough

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.itcough.`object`.Account
import com.example.itcough.`object`.Connection
import com.example.itcough.`object`.GoogleService
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.gson.Gson

class SignUpActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var imageProfileView: ImageView
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var seekBarMusicProficiency: SeekBar
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var radioGroupEducation: RadioGroup
    private lateinit var connectingMask: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var btnSignUp: Button
    private var step:Int = 0
    private val maxStep = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        imageProfileView = findViewById(R.id.imageProfileView)
        nameInput = findViewById(R.id.nameInput)
        ageInput = findViewById(R.id.ageInput)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)
        btnSignUp = findViewById(R.id.btnSignUp)
        seekBarMusicProficiency = findViewById(R.id.seekBarMusicProficiency)
        radioGroupEducation = findViewById(R.id.radioGroupEducation)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        connectingMask = findViewById(R.id.connectingMask)
        googleSignInClient = GoogleService.getGoogleSignInClient(this)
        GoogleService.updateUserInfo(this)
        updateUserUI()
        initSteps()
        switchToStep(1, null)
        val topBar  = findViewById<View>(R.id.topBarLayout)
        val btnLeft = topBar.findViewById<ImageButton>(R.id.btnLeft)
        btnLeft.setOnClickListener {
            exitPage()
        }

        btnNext.setOnClickListener {
            switchToStep(step + 1, step)
        }
        btnBack.setOnClickListener {
            switchToStep(step - 1, step)
        }
        btnSignUp.setOnClickListener {
            sendSignUpRequest()
        }
        val tvValue = findViewById<TextView>(R.id.tvMusicProficiencyValue)
        seekBarMusicProficiency.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val level = progress + 1 // Convert from 0-based to 1-based scale
                tvValue.text = "$level/10"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })



    }
    private fun updateUserUI() {

        runOnUiThread {
            Glide.with(this) // 使用 AppCompatActivity 或 FragmentActivity 作為上下文
                .load(GoogleService.userPhotoUrl ?: R.drawable.ic_person) // 處理空值
                .placeholder(R.drawable.ic_person) // 預設佔位符
                .circleCrop() // 圓形裁切
                .into(imageProfileView)
        }
    }
    private fun getUserName () : String? {
        val name = nameInput.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "User name can not be empty", Toast.LENGTH_SHORT).show()
            return null
        }
        return name
    }
    private fun getUserAge () : String? {
        val age = ageInput.text.toString()
        if (age.isEmpty()) {
            Toast.makeText(this, "User age can not be empty", Toast.LENGTH_SHORT).show()
            return null
        }
        if (age.toInt() < 18 || age.toInt() > 65) {
            Toast.makeText(this, "User age must between 18 and 65", Toast.LENGTH_SHORT).show()
            return null
        }
        return age
    }
    private fun getUserGender () : String? {
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
        return gender
    }
    private fun getUserEducation () : String? {
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
        return education
    }
    private fun getUserMusicProficiency () : String? {
        val musicProficiency = (seekBarMusicProficiency.progress + 1).toString()
        return musicProficiency
    }


    private fun getUserData () : Map<String, String>?{
        val name = getUserName() ?: return null
        val age = getUserAge() ?: return null
        val gender = getUserGender() ?: return null
        val education = getUserEducation() ?: return null
        val musicProficiency = getUserMusicProficiency() ?: return null
        return mapOf(
            "userId" to GoogleService.userID.toString(),
            "name" to name,
            "age" to age,
            "gender" to gender,
            "education" to education ,
            "musicProficiency" to musicProficiency
        )
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
                    finish()
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
    private fun switchToStep(toStep: Int, from: Int?) {
        when(from) {
            null -> {}
            1 -> {
                getUserName() ?: return
                getUserAge() ?: return
            }
            2 -> {
                getUserGender() ?: return
            }
            3 -> {
                getUserEducation() ?: return
            }
            4 -> {
                getUserMusicProficiency() ?: return
            }
            else -> return
        }
        btnBack.isVisible = true
        btnNext.isVisible = true
        btnSignUp.isVisible = false
        if (toStep == 1) {
            btnBack.isVisible = false
        }
        if (toStep == maxStep) {
            btnNext.isVisible = false
            btnSignUp.isVisible = true
        }
        val stepView = findViewById<LinearLayout>(resources.getIdentifier("step$toStep", "id", packageName))
        stepView.isVisible = true
        if (from != null) {
            val fromStepView = findViewById<LinearLayout>(resources.getIdentifier("step$from", "id", packageName))
            fromStepView.isVisible = false
        }
        step = toStep

    }
    private fun initSteps() {
        nameInput.setText("")
        ageInput.setText("")
        radioGroupGender.clearCheck()
        radioGroupEducation.clearCheck()
        seekBarMusicProficiency.progress = 4
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
}
