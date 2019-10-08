package com.karanchuk.ratesapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RevolutApi {

    @GET("latest")
    fun getRates(
        @Query("base") base: String
    ): Call<RatesApi>
}
