package com.example.flightapp.data.model

import com.google.gson.annotations.SerializedName

internal data class GetAllStatesResponse(
    @SerializedName("time")
    val time: Long? = null,
    @SerializedName("states")
    val states: List<List<Any?>>? = null
)
