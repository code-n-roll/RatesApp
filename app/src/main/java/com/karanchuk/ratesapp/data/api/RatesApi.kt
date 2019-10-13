package com.karanchuk.ratesapp.data.api

import com.google.gson.annotations.SerializedName

class RatesApi {
    @SerializedName("base") val base = ""
    @SerializedName("date") val date = ""
    @SerializedName("rates") val rates = emptyMap<String, String>()
}
