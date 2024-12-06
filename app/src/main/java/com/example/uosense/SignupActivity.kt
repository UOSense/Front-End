package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.view.View
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
            if (email.isNotEmpty() && email.contains("@uos.ac.kr")) {
                // 유효한 이메일일 때 오류 메시지 숨김
                binding.emailErrorMessage.visibility = View.GONE
                Toast.makeText(this, "웹메일이 유효합니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 유효하지 않은 이메일일 때 오류 메시지 표시
                binding.emailErrorMessage.text = "유효한 웹메일 주소를 입력하세요 (예: xxxx@uos.ac.kr)."
                binding.emailErrorMessage.visibility = View.VISIBLE
            }
        }


        //        인증 번호 발송 처리
        binding.sendVerificationCodeBtn.setOnClickListener {
            val email = binding.emailInput.text.toString()
            if (email.isNotEmpty() && email.contains("@uos.ac.kr")) {
                // 이메일로 인증 번호 발송 로직 추가
                binding.verificationLayout.visibility = View.VISIBLE
                Toast.makeText(this, "인증 번호를 발송했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "유효한 이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        //            인증 번호 제출 버튼 클릭 이벤트
        binding.submitVerificationCodeBtn.setOnClickListener {
            val verificationCode = binding.verificationCodeInput.text.toString()
            if (verificationCode.isNotEmpty()) {
                // 인증 번호 확인 로직 (추가 필요)
                Toast.makeText(this, "인증 번호가 확인되었습니다.", Toast.LENGTH_SHORT).show()
                // 다음 단계로 진행 로직 추가
            } else {
                Toast.makeText(this, "유효한 인증 번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

//        닉네임 중복 확인 버튼 체크
        binding.checkNicknameBtn.setOnClickListener {
            val nickname = binding.nicknameInput.text.toString()
            if (nickname.isNotEmpty()) {
                // 닉네임 중복 확인 로직 추가 필요
                binding.nickNameErrorMessage.visibility = View.GONE
                Toast.makeText(this, "닉네임이 중복되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                binding.nickNameErrorMessage.visibility = View.VISIBLE
                binding.nickNameErrorMessage.text = "닉네임이 중복되었습니다."
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
                binding.passwordErrorMessage.visibility = View.VISIBLE
                binding.passwordErrorMessage.text= "비밀번호가 일치하지 않습니다."
                return@setOnClickListener
            }

            // 회원가입 성공 시 가입 완료 화면으로 이동
            val intent = Intent(this, SignupCompleteActivity::class.java)
            startActivity(intent)
            finish()
        }



    }


}
