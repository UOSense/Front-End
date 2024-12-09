import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sharedPreferences: SharedPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // SharedPreferences에서 access token 가져오기
        val accessToken = sharedPreferences.getString("access_token", "") ?: ""

        // access token이 있으면 Authorization 헤더에 추가
        val request = if (accessToken.isNotEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
