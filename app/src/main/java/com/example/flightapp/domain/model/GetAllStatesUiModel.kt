package com.example.flightapp.domain.model

import com.google.gson.annotations.SerializedName

data class GetAllStatesUiModel(
    val time: Long = 0L,
    @SerializedName("states")
    val states: ArrayList<FlightStateUiModel> = arrayListOf()
)

data class FlightStateUiModel(
    val icao24: String = "",
    val callsign: String = "",
    val originCountry: String = "",
    val timePosition: Long = -1L,
    val lastContact: Long = -1L,
    val longitude: Double = -1.0,
    val latitude: Double = -1.0,
    val baroAltitude: Double = -1.0,
    val onGround: Boolean = false,
    val velocity: Double = -1.0,
    val trueTrack: Double = -1.0,
    val verticalRate: Double = -1.0,
    val sensors: List<Int> = emptyList(),
    val geoAltitude: Double = -1.0,
    val squawk: String = "",
    val spi: Boolean = false,
    val positionSource: Int = -1
)