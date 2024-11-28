package com.example.uosense.network

import com.example.uosense.models.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface MenuApi {
    @POST("/menu")
    @Multipart
    suspend fun uploadMenu(
        @Part("menus") menus: List<MultipartBody.Part>
    )

    @PUT("/update/menu")
    suspend fun updateMenu(
        @Body menuRequest: MenuRequest
    )

    @DELETE("/delete/menu/{id}")
    suspend fun deleteMenu(
        @Path("id") menuId: Int
    )
}
