package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityStartBinding
import com.example.uosense.models.LoginRequest
import com.example.uosense.network.CustomRetrofitProvider
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인 버튼 클릭 이벤트
        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
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

//    로그인 처리
    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        val customRetrofitProvider = CustomRetrofitProvider(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = customRetrofitProvider.getRestaurantApi().loginUser(loginRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // 로그인 성공 처리
                        val accessToken = response.headers()["access"]?.removePrefix("Bearer ") ?: ""
                        val refreshToken = response.headers()["Set-Cookie"]?.split(";")
                            ?.find { it.startsWith("refresh=") }
                            ?.substringAfter("refresh=") ?: ""

                        if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                            saveTokensToLocal(accessToken, refreshToken)
                            Toast.makeText(this@StartActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@StartActivity, "토큰 발급 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@StartActivity, "로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StartActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveTokensToLocal(accessToken: String, refreshToken: String) {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.apply()
    }
}
