package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivitySignupBinding
import com.example.uosense.helpers.SQLiteHelper

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var isEmailVerified = false // 이메일 중복 확인 인증 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 물리적 뒤로 가기 활성화
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // 웹메일 중복 확인
        binding.verifyEmailButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (email.isNotEmpty() && email.contains("@uos.ac.kr")) {
                val dbHelper = SQLiteHelper(this)

                if (dbHelper.isEmailExists(email)) {
//                    이메일 중복 경우
                    binding.emailErrorMessage.visibility=View.VISIBLE
                    binding.emailErrorMessage.text="중복된 웹메일입니다."
                    isEmailVerified = false
                }else {
                    binding.emailErrorMessage.visibility=View.GONE
                    Toast.makeText(this, "사용 가능한 웹메일입니다.", Toast.LENGTH_SHORT).show()
                    isEmailVerified = true
                }
            } else {
                // 유효하지 않은 이메일일 때 오류 메시지 표시
                binding.emailErrorMessage.visibility = View.VISIBLE
                binding.emailErrorMessage.text = "유효한 웹메일 주소를 입력하세요 (예: xxxx@uos.ac.kr)."
                isEmailVerified = false
            }
        }

        // 인증 번호 발송 처리
        binding.sendVerificationCodeBtn.setOnClickListener {
            if (!isEmailVerified) {
                binding.emailErrorMessage.visibility = View.VISIBLE
                binding.emailErrorMessage.text = "중복 확인을 먼저 진행해 주세요."
                return@setOnClickListener
            }

            val email = binding.emailInput.text.toString()
            if (email.isNotEmpty() && email.contains("@uos.ac.kr")) {
                binding.verificationLayout.visibility = View.VISIBLE
                Toast.makeText(this, "인증 번호를 발송했습니다.", Toast.LENGTH_SHORT).show()

//                타이머 시작
                startTimer()

            } else {
                Toast.makeText(this, "유효한 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 인증 번호 제출 버튼 클릭 이벤트
        binding.submitVerificationCodeBtn.setOnClickListener {
            val verificationCode = binding.verificationCodeInput.text.toString()
            if (verificationCode.isNotEmpty()) {
                if (::countDownTimer.isInitialized) {
                    countDownTimer.cancel()
                }
                Toast.makeText(this, "인증 번호가 확인되었습니다.", Toast.LENGTH_SHORT).show()
                binding.timerTextView.visibility=View.GONE
            } else {
                Toast.makeText(this, "유효한 인증 번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 닉네임 중복 확인 버튼 클릭
        binding.checkNicknameBtn.setOnClickListener {
            val nickname = binding.nicknameInput.text.toString()
            if (nickname.isNotEmpty()) {
                val dbHelper = SQLiteHelper(this)

                if (dbHelper.isNicknameExists(nickname)){
//                    닉네임 중복 경우
                    binding.nickNameErrorMessage.visibility = View.VISIBLE
                    binding.nickNameErrorMessage.text = "닉네임이 중복되었습니다."
                }else {
                    binding.nickNameErrorMessage.visibility = View.GONE
                    Toast.makeText(this, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.nickNameErrorMessage.visibility=View.VISIBLE
                binding.nickNameErrorMessage.text="닉네임을 입력하세요."
            }
        }

        // 회원가입 완료 버튼 클릭 이벤트
        binding.registerBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.passwordConfirmInput.text.toString()
            val nickname = binding.nicknameInput.text.toString()

            // 입력 검증
            if (!validateInputs(email, password, confirmPassword, nickname)) {
                return@setOnClickListener
            }

            // 회원가입 처리
            registerUser(email, password, nickname)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    // 사용자 입력 검증
    private fun validateInputs(email: String, password: String, confirmPassword: String, nickname: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#])[A-Za-z\\d@\$!%*?&#]{8,20}$"

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailErrorMessage.visibility = View.VISIBLE
            binding.emailErrorMessage.text = "유효한 이메일을 입력하세요."
            return false
        }
        if (password.isEmpty() || password.length < 8 || password.length > 20) {
            binding.passwordErrorMessage.visibility = View.VISIBLE
            binding.passwordErrorMessage.text = "비밀번호는 최소 8자 이상, 최대 20자 이하여야 합니다."
            return false
        }
        if (!password.matches(Regex(passwordPattern))){
            binding.passwordErrorMessage.visibility = View.VISIBLE
            binding.passwordErrorMessage.text = "비밀번호는 대,소문자, 숫자, 특수 문자를 포함해야 합니다."
            return false
        }
        if (password != confirmPassword) {
            binding.passwordErrorMessage.visibility = View.VISIBLE
            binding.passwordErrorMessage.text = "비밀번호가 일치하지 않습니다."
            return false
        }
        return true
    }

    // 회원가입 처리
    private fun registerUser(email: String, password: String, nickname: String) {
        val dbHelper = SQLiteHelper(this)

        // 데이터 저장
        if (dbHelper.insertUser(email, password, nickname)) {
            // 로그 추가
            android.util.Log.d("SQLiteDB", "User saved: email=$email, nickname=$nickname")
            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignupCompleteActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "회원가입 실패! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var countDownTimer: CountDownTimer
    private var isTimerRunning = false

    private fun startTimer() {
        // 기존 타이머 취소
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }

        // 새 타이머 시작
        countDownTimer = object : CountDownTimer(180000, 1000) { // 3분 (180,000ms)
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                isTimerRunning = false
                binding.timerTextView.text = "00:00"
                Toast.makeText(this@SignupActivity, "인증 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        countDownTimer.start()
        isTimerRunning = true
        binding.timerTextView.visibility = View.VISIBLE // 타이머 표시
    }



}

