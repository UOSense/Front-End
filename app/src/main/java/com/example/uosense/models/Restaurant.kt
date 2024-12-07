package com.example.uosense.models

import android.os.Parcel
import android.os.Parcelable


data class MenuRequest(
    val id: Int,
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val image: String
)

data class MenuResponse(
    val menuId: Int,
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val imageUrl: String
)



data class RestaurantRequest(
    val id: Int?,
    val name: String,
    val doorType: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phoneNumber: String?,
    val category: String,
    val subDescription: String?,
    val description: String
)

data class BusinessDay(
    val id: Int, // 고유 ID
    val restaurant_id: Int, // Restaurant의 외래 키
    val day_of_week: String, // 요일 (enum 값: 'Monday', 'Tuesday', ...)
    val have_break_time: Boolean, // 쉬는 시간 여부
    val start_break_time: String?, // 쉬는 시간 시작 (nullable)
    val stop_break_time: String?, // 쉬는 시간 종료 (nullable)
    val opening_time: String, // 영업 시작 시간
    val closing_time: String, // 영업 종료 시간
    val is_holiday: Boolean // 휴일 여부
)

data class BusinessDayList(
    val restaurantId: Int,
    val businessDayInfoList: List<BusinessDayInfo>
)

data class BusinessDayInfo(
    val id: Int?,
    val dayOfWeek: String,
    val haveBreakTime: Boolean,
    val startBreakTime: LocalTime?,
    val stopBreakTime: LocalTime?,
    val openingTime: LocalTime,
    val closingTime: LocalTime,
    val holiday: Boolean
)

data class LocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int?,
    val nano: Int?
)

data class WebmailRequest(
    val email: String,
    val purpose: String
)


data class AuthCodeRequest(
    val email: String,
    val code: String
)


data class RestaurantImagesResponse(
    val restaurantId: Int,
    val imageList: List<ImageInfo>
)

data class ImageInfo(
    val url: String,
    val description: String?
)


data class NewMenuRequest(
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val image: String
)

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
    val restaurantImage: String?,
    val doorType: String?, // 새로 추가된 속성
    val phoneNumber: String? // 새로 추가된 속성
): Parcelable {
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
    val businessDays: List<BusinessDay>?
)





