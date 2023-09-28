package com.lrm.accountant.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://fssservices.bookxpert.co/api/getownerslist/2021-01-16/payments/"

// Building the retrofit object
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// Interface which makes GET request to the web api
interface NetworkApiService {
    @GET("owner")
    suspend fun getData(): String
}

object AccountApi {
    val retrofitService: NetworkApiService by lazy {
        retrofit.create(NetworkApiService::class.java)
    }
}