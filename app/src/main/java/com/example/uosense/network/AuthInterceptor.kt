import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    private val tokenManager = TokenManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()

        // 토큰이 없는 경우 예외 발생
        val accessToken = tokenManager.getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            throw IllegalStateException("Access token is missing")
        }

        // 기존 요청에 토큰 추가
        originalRequest = attachToken(originalRequest, accessToken)
        var response = chain.proceed(originalRequest)

        // 401 Unauthorized 응답 시
        if (response.code == 401) {
            response.close()

            // 토큰 재발급 시도
            val isTokenRefreshed = runBlocking {
                tokenManager.refreshAccessToken()
            }

            if (isTokenRefreshed) {
                val newAccessToken = tokenManager.getAccessToken()
                if (!newAccessToken.isNullOrEmpty()) {
                    val newRequest = attachToken(originalRequest, newAccessToken)
                    response = chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    // Authorization 헤더 추가 함수
    private fun attachToken(request: Request, token: String): Request {
        return request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
    }
}
