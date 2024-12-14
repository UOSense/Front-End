import com.example.uosense.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {

    // 회원 관리
    @POST("/api/v1/user/signup")
    suspend fun signupUser(
        @Body newUserRequest: NewUserRequest
    ): Response<Boolean>

    @POST("/api/v1/user/check-nickname")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<Boolean>

    @PUT("/api/v1/user/signout")
    suspend fun logoutUser(
        @Header("Cookie") refreshToken: String
    ): Response<Unit>

    @GET("/api/v1/user/reissue")
    suspend fun reissueToken(
        @Header("Cookie") refreshToken: String
    ): Response<Unit>

    // 웹메일 인증 관리
    @GET("/api/v1/webmail/check-format")
    suspend fun checkEmail(
        @Query("mailAddress") email: String
    ): Response<Boolean>

    @POST("/api/v1/webmail/verify")
    suspend fun sendAuthCode(
        @Body webmailRequest: WebmailRequest
    ): Response<Boolean>

    @POST("/api/v1/webmail/authenticate-code")
    suspend fun validateCode(
        @Body authCodeRequest: AuthCodeRequest
    ): Response<Boolean>

    // 식당 생성
    @POST("/api/v1/restaurant/create")
    suspend fun createRestaurant(
        @Header("Authorization") token: String,
        @Body restaurantRequest: RestaurantRequest
    ): Response<RestaurantInfo>
    //이미지 생성
    @Multipart
    @POST("/api/v1/restaurant/create/images")
    suspend fun uploadRestaurantImages(
        @Header("Authorization") token: String,
        @Query("restaurantId") restaurantId: Int,
        @Part images: List<MultipartBody.Part>
    ): Response<RestaurantImagesResponse>

    //영업일 생성
    @POST("/api/v1/restaurant/create/businessday")
    suspend fun createBusinessDay(
        @Header("Authorization") token: String,
        @Body businessDayList: BusinessDayList
    ): Response<Unit>
    //메뉴 생성
    @Multipart
    @POST("/api/v1/restaurant/create/menu")
    suspend fun uploadMenu(
        @Header("Authorization") token: String,
        @Query("restaurantId") restaurantId: Int,
        @Query("name") name: String,
        @Query("price") price: Int,
        @Query("description") description: String,
        @Part image: MultipartBody.Part?
    ): Response<Unit>
    // 식당 수정
    @PUT("/api/v1/restaurant/update")
    suspend fun editRestaurant(
        @Header("Authorization") token: String,
        @Body updatedRequest: RestaurantRequest
    ): Response<Unit>
    // 메뉴 수정
    @PUT("/api/v1/restaurant/update/menu")
    suspend fun updateMenu(
        @Header("Authorization") token: String,
        @Body menuRequest: MenuRequest
    ): Response<Unit>
    // 위치 수정
    @PUT("/api/v1/restaurant/update")
    suspend fun updateRestaurantLocation(
        @Header("Authorization") token: String,
        @Body updatedRequest: RestaurantRequest
    ): Response<Unit>
    // 영업일 수정
    @PUT("/api/v1/restaurant/update/businessday")
    suspend fun editBusinessDay(
        @Header("Authorization") token: String,
        @Body businessDayList: BusinessDayList
    ): Response<Unit>

    @DELETE("/api/v1/restaurant/delete")
    suspend fun deleteRestaurant(
        @Header("Authorization") token: String,
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    @DELETE("/api/v1/restaurant/delete/menu")
    suspend fun deleteMenu(
        @Header("Authorization") token: String,
        @Query("menuId") menuId: Int
    ): Response<Unit>

    @DELETE("/api/v1/restaurant/delete/businessday")
    suspend fun deleteBusinessDay(
        @Header("Authorization") token: String,
        @Query("businessDayId") businessDayId: Int
    ): Response<Unit>

    // 검색
    @GET("/api/v1/search")
    suspend fun searchRestaurants(
        @Query("keyword") keyword: String,
        @Query("doorType") doorType: String? = null
    ): Response<List<RestaurantListResponse>>

    @GET("/api/v1/search/sort")
    suspend fun sortRestaurants(
        @Query("keyword") keyword: String,
        @Query("filter") filter: String
    ): Response<List<RestaurantListResponse>>

    // 리뷰 관리
    @POST("/api/v1/review/create")
    suspend fun createReview(
        @Body reviewRequest: ReviewRequest,
        @Header("access") accessToken: String
    ): Response<Int>

    @Multipart
    @POST("/api/v1/review/create/images")
    suspend fun uploadReviewImages(
        @Query("reviewId") reviewId: Int,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    @DELETE("/api/v1/review/delete")
    suspend fun deleteReview(
        @Query("reviewId") reviewId: Int
    ): Response<Unit>

    @GET("/api/v1/review/get")
    suspend fun getReviewById(
        @Query("reviewId") reviewId: Int
    ): Response<ReviewResponse>

    @GET("api/v1/restaurant/get/menu")
    suspend fun getMenu(
        @Query("restaurantId") restaurantId: Int
    ): Response<List<MenuResponse>>

    @GET("/api/v1/restaurant/get/businessday")
    suspend fun getBusinessDayList(
        @Query("restaurantId") restaurantId: Int
    ): Response<BusinessDayList>

    // 특정 식당 정보 조회
    @GET("/api/v1/restaurant/get")
    suspend fun getRestaurantById(
        @Query("restaurantId") restaurantId: Int
    ): Response<RestaurantInfo>

    // 식당 리스트 조회
    @GET("/api/v1/restaurant/get/list")
    suspend fun getRestaurantList(
        @Query("doorType") doorType: String? = null,
        @Query("filter") filter: String = "DEFAULT"
    ): Response<List<RestaurantListResponse>>

    @GET("/api/v1/review/get/list")
    suspend fun getRestaurantReviews(
        @Query("restaurantId") restaurantId: Int
    ): Response<List<ReviewResponse>>

    @GET("/api/v1/restaurant/get/images")
    suspend fun getRestaurantImages(
        @Query("restaurantId") restaurantId: Int
    ): Response<RestaurantImagesResponse>


    @GET("/api/v1/review/get/user")
    suspend fun getUserReviews(
        @Query("userId") userId: Int
    ): Response<List<ReviewResponse>>

    // 즐겨찾기 관리
    @POST("/api/v1/bookmark/create")
    suspend fun addBookmark(
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    @DELETE("/api/v1/bookmark/delete")
    suspend fun deleteBookmark(
        @Query("bookMarkId") bookMarkId: Int
    ): Response<Unit>

    @GET("/api/v1/bookmark/get/user")
    suspend fun getUserBookmarks(
        @Query("userId") userId: Int
    ): Response<List<BookMarkResponse>>

    @GET("/api/v1/bookmark/get/mine")
    suspend fun getMyBookmarks(): Response<List<BookMarkResponse>>

    // 신고 관리
    @POST("/api/v1/report/create/review")
    suspend fun reportReview(
        @Body reportRequest: ReportRequest
    ): Response<Unit>

    @GET("/api/v1/report/get/list")
    suspend fun getReports(): Response<List<ReportResponse>>

    // 로그인 API 호출 정의 (RestaurantApi.kt에 추가 필요)
    @POST("/api/v1/user/signin")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<Unit>
}
