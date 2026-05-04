package com.example.xafaeltalp.model.api

import retrofit2.http.GET

data class ApiResponse(
    val status: String
)

interface ApiService {
    @GET("Warriordescens/json0489/refs/heads/main/jsondata")
    suspend fun getServiceStatus(): List<ApiResponse>
}
