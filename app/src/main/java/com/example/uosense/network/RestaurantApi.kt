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

    // 기존 식당 정보 수정
    @PUT("/api/v1/restaurant/restaurant/{restaurantId}/update")
    suspend fun editRestaurant(
        @Path("restaurantId") restaurantId: Int,
        @Body updatedRequest: RestaurantRequest
    ): Response<Unit>

    // 특정 식당 영업 정보 수정
    @PUT("/api/v1/restaurant/restaurant/update/businessday")
    suspend fun editBusinessDay(
        @Body businessDayList: BusinessDayList
    ): Response<Unit>

    // 웹메일 인증 코드 발송
    @POST("/api/v1/webmail/verify")
    suspend fun sendAuthCode(
        @Body webmailRequest: WebmailRequest
    ): Response<String>

    // 인증 코드 확인
    @POST("/api/v1/webmail/authenticate-code")
    suspend fun validateCode(
        @Body authCodeRequest: AuthCodeRequest
    ): Response<String>

    // 특정 식당 사진 조회
    @GET("/api/v1/restaurant/{restaurantId}/images")
    suspend fun getRestaurantImages(
        @Path("restaurantId") restaurantId: Int
    ): RestaurantImagesResponse

    // 특정 식당 사진 등록
    @Multipart
    @POST("/api/v1/restaurant/{restaurantId}/images")
    suspend fun uploadRestaurantImages(
        @Path("restaurantId") restaurantId: Int,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    // 신규 식당 등록
    @POST("/api/v1/restaurant/restaurant/new")
    suspend fun createRestaurant(
        @Body restaurantRequest: RestaurantRequest
    ): Response<Unit>

    // 특정 식당 영업 정보 등록
    @POST("/api/v1/restaurant/restaurant/businessday")
    suspend fun createBusinessDay(
        @Body businessDayList: BusinessDayList
    ): Response<Unit>

    // 특정 식당 메뉴 등록
    @Multipart
    @POST("/api/v1/restaurant/menu")
    suspend fun uploadMenu(
        @Part menus: List<MultipartBody.Part>
    ): Response<Unit>

    // 특정 식당 정보 조회
    @GET("/api/v1/restaurant/{restaurantId}/show")
    suspend fun getRestaurantById(
        @Path("restaurantId") restaurantId: Int
    ): RestaurantInfo

    // 식당 정보 일괄 조회
    @GET("/api/v1/restaurant/show")
    suspend fun getAllRestaurants(
        @Query("doorType") doorType: String?,
        @Query("category") category: String?
    ): List<RestaurantListResponse>

    // 특정 식당 메뉴 조회
    @GET("/api/v1/restaurant/{restaurantId}/menu")
    suspend fun getMenuList(
        @Path("restaurantId") restaurantId: Int
    ): List<MenuResponse>

    // 특정 식당 영업 정보 조회
    @GET("/api/v1/restaurant/{restaurantId}/businessday")
    suspend fun getBusinessDayList(
        @Path("restaurantId") restaurantId: Int
    ): BusinessDayList

    // 식당 검색
    @GET("/api/v1/search")
    suspend fun searchRestaurants(
        @Query("keyword") keyword: String,
        @Query("closestDoor") closestDoor: String
    ): List<RestaurantListResponse>

    // 식당 삭제
    @DELETE("/api/v1/restaurant/restaurant/{restaurantId}/delete")
    suspend fun deleteRestaurant(
        @Path("restaurantId") restaurantId: Int
    ): Response<Unit>

    // 특정 식당 메뉴 삭제
    @DELETE("/api/v1/restaurant/delete/menu/{id}")
    suspend fun deleteMenu(
        @Path("id") menuId: Int
    ): Response<Unit>

    // 특정 식당 영업 정보 삭제
    @DELETE("/api/v1/restaurant/restaurant/delete/businessday/{businessdayId}")
    suspend fun deleteBusinessDay(
        @Path("businessdayId") businessdayId: Int
    ): Response<Unit>
}

