package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 물리적 뒤로 가기 활성화
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })



        // 웹메일 중복 확인
        binding.verifyEmailButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (email.isNotEmpty()) {
                // 닉네임 중복 확인 로직 추가 필요
                Toast.makeText(this, "웹메일이 중복되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "웹메일이 중복되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }


        //        인증 번호 발송 처리
        binding.sendVerificationCodeBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (email.isNotEmpty() && email.contains("@uos.ac.kr")) {
                // 이메일로 인증 번호 발송 로직 추가
                Toast.makeText(this, "인증 번호를 발송했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "유효한 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

//            인증 번호 재전송 처리
        binding.resendVerificationCodeBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (email.isNotEmpty() && email.contains("@uos.ac.kr")) {
                // 이메일로 인증 번호 재전송 로직 추가
                Toast.makeText(this, "인증 번호를 재전송했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "유효한 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }


//        닉네임 중복 확인 버튼 체크
        binding.checkNicknameBtn.setOnClickListener {
            val nickname = binding.nicknameInput.text.toString()
            if (nickname.isNotEmpty()) {
                // 닉네임 중복 확인 로직 추가 필요
                Toast.makeText(this, "닉네임이 중복되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "닉네임이 중복되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }


        // 회원가입 완료 버튼 클릭 이벤트
        binding.registerBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.passwordConfirmInput.text.toString()
            val nickname = binding.nicknameInput.text.toString()

//            회원가입 로직
            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 회원가입 성공 시 가입 완료 화면으로 이동
            val intent = Intent(this, SignupCompleteActivity::class.java)
            startActivity(intent)
            finish()
        }



    }


}
