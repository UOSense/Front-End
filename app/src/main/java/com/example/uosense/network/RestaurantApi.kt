import com.example.uosense.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {

    // 특정 식당 메뉴 수정
    @PUT("/api/v1/restaurant/update/menu")
    suspend fun updateMenu(
        @Body menuRequest: MenuRequest
    ): Response<Unit>

    // 특정 식당 정보 수정
    @PUT("/api/v1/restaurant/update")
    suspend fun editRestaurant(
        @Body updatedRequest: RestaurantRequest
    ): Response<Unit>

    // 특정 식당 영업 정보 수정
    @PUT("/api/v1/restaurant/update/businessday")
    suspend fun editBusinessDay(
        @Body businessDayList: BusinessDayList
    ): Response<Unit>

    // 웹메일 인증 코드 발송
    @POST("/api/v1/webmail/verify")
    suspend fun sendAuthCode(
        @Body webmailRequest: WebmailRequest
    ): Response<Boolean>

    // 인증 코드 확인
    @POST("/api/v1/webmail/authenticate-code")
    suspend fun validateCode(
        @Body authCodeRequest: AuthCodeRequest
    ): Response<Boolean>

    // 특정 식당 사진 조회
    @GET("/api/v1/restaurant/get/images")
    suspend fun getRestaurantImages(
        @Query("restaurantId") restaurantId: Int
    ): RestaurantImagesResponse

    // 특정 식당 사진 등록
    @Multipart
    @POST("/api/v1/restaurant/create/images")
    suspend fun uploadRestaurantImages(
        @Query("restaurantId") restaurantId: Int,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    // 신규 식당 등록
    @POST("/api/v1/restaurant/create")
    suspend fun createRestaurant(
        @Body restaurantRequest: RestaurantRequest
    ): Response<Unit>

    // 특정 식당 영업 정보 등록
    @POST("/api/v1/restaurant/create/businessday")
    suspend fun createBusinessDay(
        @Body businessDayList: BusinessDayList
    ): Response<Unit>

    // 특정 식당 메뉴 등록
    @Multipart
    @POST("/api/v1/restaurant/create/menu")
    suspend fun uploadMenu(
        @Query("restaurantId") restaurantId: Int,
        @Query("name") name: String,
        @Query("price") price: Int,
        @Query("description") description: String,
        @Part image: MultipartBody.Part?
    ): Response<Unit>

    // 특정 식당 정보 조회
    @GET("/api/v1/restaurant/get")
    suspend fun getRestaurantById(
        @Query("restaurantId") restaurantId: Int
    ): RestaurantInfo

    // 식당 정보 일괄 조회
    @GET("/api/v1/restaurant/get/list")
    suspend fun getRestaurantList(
        @Query("doorType") doorType: String? = null,
        @Query("filter") filter: String? = "DEFAULT"
    ): List<RestaurantListResponse>

    // 특정 식당 메뉴 조회
    @GET("/api/v1/restaurant/get/menu")
    suspend fun getMenuList(
        @Query("restaurantId") restaurantId: Int
    ): List<MenuResponse>

    // 특정 식당 영업 정보 조회
    @GET("/api/v1/restaurant/get/businessday")
    suspend fun getBusinessDayList(
        @Query("restaurantId") restaurantId: Int
    ): BusinessDayList

    // 식당 검색
    @GET("/api/v1/search")
    suspend fun searchRestaurants(
        @Query("keyword") keyword: String,
        @Query("closestDoor") closestDoor: String
    ): List<RestaurantListResponse>

    // 검색 결과 정렬 (검색시에만)
    @GET("/api/v1/search/sort")
    suspend fun sortRestaurants(
        @Query("keyword") keyword: String,
        @Query("filter") filter: String
    ): List<RestaurantListResponse>

    // 검색 필터링 (출입구)
    @GET("/api/v1/search/filter")
    suspend fun filterByGate(
        @Query("keyword") keyword: String,
        @Query("doorType") doorType: String
    ): List<RestaurantListResponse>

    // 특정 식당 삭제
    @DELETE("/api/v1/restaurant/delete")
    suspend fun deleteRestaurant(
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    // 특정 식당 메뉴 삭제
    @DELETE("/api/v1/restaurant/delete/menu")
    suspend fun deleteMenu(
        @Query("menuId") menuId: Int
    ): Response<Unit>

    // 특정 식당 영업 정보 삭제
    @DELETE("/api/v1/restaurant/delete/businessday")
    suspend fun deleteBusinessDay(
        @Query("businessDayId") businessDayId: Int
    ): Response<Unit>

    // 즐겨찾기 추가
    @POST("/api/v1/bookmark/create")
    suspend fun addBookmark(
        @Query("restaurantId") restaurantId: Int
    ): Response<Unit>

    // 즐겨찾기 삭제
    @DELETE("/api/v1/bookmark/delete")
    suspend fun deleteBookmark(
        @Query("bookMarkId") bookMarkId: Int
    ): Response<Unit>

    // 즐겨찾기 조회 (사용자 기준)
    @GET("/api/v1/bookmark/get/mine")
    suspend fun getMyBookmarks(): List<BookMarkResponse>

    @GET("/api/v1/bookmark/get/user")
    suspend fun getUserBookmarks(
        @Query("userId") userId: Int
    ): List<BookMarkResponse>

    // 리뷰 등록
    @POST("/api/v1/review/create")
    suspend fun createReview(
        @Body reviewRequest: ReviewRequest
    ): Response<Int>

    // 리뷰 삭제
    @DELETE("/api/v1/review/delete")
    suspend fun deleteReview(
        @Query("reviewId") reviewId: Int
    ): Response<Unit>

    // 리뷰 목록 조회 (식당 기준)
    @GET("/api/v1/review/get/list")
    suspend fun getRestaurantReviews(
        @Query("restaurantId") restaurantId: Int
    ): List<ReviewResponse>

    // 특정 사용자 리뷰 조회
    @GET("/api/v1/review/get/user")
    suspend fun getUserReviews(
        @Query("userId") userId: Int
    ): List<ReviewResponse>

    // 특정 리뷰 조회
    @GET("/api/v1/review/get")
    suspend fun getReviewById(
        @Query("reviewId") reviewId: Int
    ): ReviewResponse

    // 리뷰 좋아요 추가
    @PATCH("/api/v1/review/like")
    suspend fun likeReview(
        @Query("userId") userId: Int,
        @Query("reviewId") reviewId: Int
    ): Response<Unit>

    // 리뷰 신고
    @POST("/api/v1/report/create/review")
    suspend fun reportReview(
        @Body reportRequest: ReportRequest
    ): Response<Unit>

    // 모든 신고 내역 조회
    @GET("/api/v1/report/get/list")
    suspend fun getReports(): List<ReportResponse>

    // 토큰 재발급
    @GET("/api/v1/user/reissue")
    suspend fun reissueToken(): Response<Unit>

    // 로그아웃 처리
    @PUT("/api/v1/user/signout")
    suspend fun signOut(): Response<Unit>

    // 회원가입
    @POST("/api/v1/user/signup")
    suspend fun signUp(): Response<Unit>
}
