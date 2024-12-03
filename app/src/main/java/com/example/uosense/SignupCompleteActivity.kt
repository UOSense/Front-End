package com.example.uosense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivitySignupCompleteBinding

class SignupCompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 탐방 시작 버튼 클릭 이벤트
        binding.goToMainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
