package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityStartBinding
import com.example.uosense.helpers.SQLiteHelper

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인 버튼 클릭 이벤트
        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

//            로그인 로직
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            //

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = SQLiteHelper(this)

                if (dbHelper.validateUser(email, password)) {
                    Toast.makeText(this, "로그인 시도: $email", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "유효하지 않은 사용자 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 클릭 이벤트
        binding.signup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // 비밀번호 찾기 클릭 이벤트
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "아이디/비밀번호 찾기 기능 준비 중", Toast.LENGTH_SHORT).show()
        }
    }
}
