import android.content.Context
import android.util.Log
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun saveAccessToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString("access_token", token)
        editor.apply()
    }

    suspend fun refreshAccessToken(): Boolean {
        val refreshToken = getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e("TokenManager", "Refresh token is missing")
            return false
        }

        return try {
            val response = RetrofitInstance.restaurantApi.reissueToken("refresh=$refreshToken")
            if (response.isSuccessful) {
                val newAccessToken = response.headers()["access"]?.removePrefix("Bearer ") ?: ""
                if (newAccessToken.isNotEmpty()) {
                    saveAccessToken(newAccessToken)
                    Log.d("TokenManager", "Access token refreshed successfully")
                    true
                } else {
                    Log.e("TokenManager", "Failed to retrieve new access token")
                    false
                }
            } else {
                Log.e("TokenManager", "Failed to refresh token: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error refreshing token: ${e.message}")
            false
        }
    }
}
