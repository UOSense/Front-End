package com.example.uosense.models

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime

// 메뉴 요청 모델
data class MenuRequest(
    val id: Int?,
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val image: String? = null
)

// 메뉴 응답 모델
data class MenuResponse(
    val menuId: Int,
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String?,
    val imageUrl: String?
)

// 식당 요청 모델
data class RestaurantRequest(
    val id: Int? = null,
    val name: String,
    val doorType: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phoneNumber: String? = null,
    val category: String,
    val subDescription: String? = null,
    val description: String
)

// 영업일 정보 모델
data class BusinessDay(
    val id: Int? = null,
    val restaurantId: Int,
    val dayOfWeek: String,
    val haveBreakTime: Boolean,
    val startBreakTime: String? = null,
    val stopBreakTime: String? = null,
    val openingTime: String,
    val closingTime: String,
    val holiday: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readValue(Int::class.java.classLoader) as? Int,
        restaurantId = parcel.readInt(),
        dayOfWeek = parcel.readString() ?: "",
        haveBreakTime = parcel.readByte() != 0.toByte(),
        startBreakTime = parcel.readString(),
        stopBreakTime = parcel.readString(),
        openingTime = parcel.readString() ?: "",
        closingTime = parcel.readString() ?: "",
        holiday = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeInt(restaurantId)
        parcel.writeString(dayOfWeek)
        parcel.writeByte(if (haveBreakTime) 1 else 0)
        parcel.writeString(startBreakTime)
        parcel.writeString(stopBreakTime)
        parcel.writeString(openingTime)
        parcel.writeString(closingTime)
        parcel.writeByte(if (holiday) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BusinessDay> {
        override fun createFromParcel(parcel: Parcel): BusinessDay {
            return BusinessDay(parcel)
        }

        override fun newArray(size: Int): Array<BusinessDay?> {
            return arrayOfNulls(size)
        }
    }
}


// 영업일 리스트 모델
data class BusinessDayList(
    val restaurantId: Int,
    val businessDayInfoList: List<BusinessDayInfo>
)

// 영업일 정보 모델 (응답)
data class BusinessDayInfo(
    val id: Int?,
    val dayOfWeek: String,
    val breakTime: Boolean,
    val startBreakTime: String? ,
    val stopBreakTime: String? ,
    val openingTime: String?,
    val closingTime: String?,
    val holiday: Boolean
)

// 로컬 시간 모델
data class LocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int? = null,
    val nano: Int? = null
)


// 식당 이미지 응답 모델
data class RestaurantImagesResponse(
    val restaurantId: Int,
    val imageList: List<ImageInfo>
) {

}

// 이미지 정보 모델
data class ImageInfo(
    val url: String,
    val id: Int
)

// 새 메뉴 요청 모델
data class NewMenuRequest(
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val image: String
)

// 식당 목록 응답 모델 (Parcelable 포함)
data class RestaurantListResponse(
    val id: Int,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val address: String,
    val rating: Double,
    val category: String,
    val reviewCount: Int,
    val bookmarkCount: Int,
    val restaurantImage: String? = null,
    val doorType: String? = null,
    val phoneNumber: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeDouble(longitude)
        parcel.writeDouble(latitude)
        parcel.writeString(address)
        parcel.writeDouble(rating)
        parcel.writeString(category)
        parcel.writeInt(reviewCount)
        parcel.writeInt(bookmarkCount)
        parcel.writeString(restaurantImage)
        parcel.writeString(doorType)
        parcel.writeString(phoneNumber)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<RestaurantListResponse> {
        override fun createFromParcel(parcel: Parcel): RestaurantListResponse {
            return RestaurantListResponse(parcel)
        }

        override fun newArray(size: Int): Array<RestaurantListResponse?> {
            return arrayOfNulls(size)
        }
    }
}


// 식당 정보 모델
data class RestaurantInfo(
    val id: Int,
    val name: String,
    val doorType: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phoneNumber: String?,
    val rating: Double?,
    val category: String?,
    val subDescription: String?,
    val description: String?,
    val reviewCount: Int?,
    val bookmarkCount: Int?,
    val bookmarkId: Int?, // 즐겨찾기 ID 추가
    val businessDays: List<BusinessDay>? = null
) : Parcelable {

    // Parcel 생성자
    private constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        name = parcel.readString() ?: "",
        doorType = parcel.readString(),
        latitude = parcel.readDouble(),
        longitude = parcel.readDouble(),
        address = parcel.readString() ?: "",
        phoneNumber = parcel.readString(),
        rating = parcel.readValue(Double::class.java.classLoader) as? Double,
        category = parcel.readString(),
        subDescription = parcel.readString(),
        description = parcel.readString(),
        reviewCount = parcel.readValue(Int::class.java.classLoader) as? Int,
        bookmarkCount = parcel.readValue(Int::class.java.classLoader) as? Int,
        bookmarkId = parcel.readValue(Int::class.java.classLoader) as? Int,
        businessDays = parcel.createTypedArrayList(BusinessDay.CREATOR)
    )

    // Parcel 데이터 쓰기
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(doorType)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(address)
        parcel.writeString(phoneNumber)
        parcel.writeValue(rating)
        parcel.writeString(category)
        parcel.writeString(subDescription)
        parcel.writeString(description)
        parcel.writeValue(reviewCount)
        parcel.writeValue(bookmarkCount)
        parcel.writeValue(bookmarkId)
        parcel.writeTypedList(businessDays)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<RestaurantInfo> {
        override fun createFromParcel(parcel: Parcel): RestaurantInfo = RestaurantInfo(parcel)
        override fun newArray(size: Int): Array<RestaurantInfo?> = arrayOfNulls(size)
    }
}

data class ReviewRequest(
    val restaurantId: Int,
    val body: String,
    val rating: Double,
    val dateTime: String,
    val tag: String?,
    val isReviewEventCheck: Boolean
)

data class ReportRequest(
    val reviewId: Int,
    val detail: String, // "ABUSIVE", "DEROGATORY", "ADVERTISEMENT"
    val createdAt: String
)


data class ReviewResponse(
    val id: Int,
    val restaurantId: Int,
    val userId: Int,
    val body: String,
    val rating: Double,
    val dateTime: String,
    val tag: String?,
    val likeCount: Int,
    val imageUrls: List<String>?,
    val reviewEventCheck: Boolean
)



// 즐겨찾기 응답 모델
data class BookMarkResponse(
    val id: Int,            // 북마크 고유 ID
    val userId: Int,        // 사용자 ID
    val restaurantId: Int   // 식당 ID
)
// 신고 응답 모델
data class ReportResponse(
    val id: Int,                // 신고 고유 ID
    val reviewId: Int,          // 신고된 리뷰 ID
    val userId: Int,            // 신고한 사용자 ID
    val detail: String,         // 신고 상세 내용 (예: ABUSIVE, DEROGATORY, ADVERTISEMENT)
    val createdAt: LocalDateTime // 신고 생성 일시
)