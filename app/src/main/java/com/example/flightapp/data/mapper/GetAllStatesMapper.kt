package com.example.flightapp.data.mapper

import com.example.flightapp.data.model.GetAllStatesResponse
import com.example.flightapp.domain.model.FlightStateUiModel
import com.example.flightapp.domain.model.GetAllStatesUiModel
import javax.inject.Inject

internal class GetAllStatesMapper @Inject constructor() {

    fun map(response: GetAllStatesResponse?): GetAllStatesUiModel {
        return GetAllStatesUiModel(
            time = response?.time ?: 0L,
            states = response?.states?.map {
                mapToFlightStateUiModel(it)
            }?.toCollection(ArrayList()) ?: arrayListOf()
        )
    }

    private fun mapToFlightStateUiModel(state: List<Any?>?): FlightStateUiModel {
        return FlightStateUiModel(
            icao24 = state?.getOrNull(0) as? String ?: "",
            callsign = state?.getOrNull(1) as? String ?: "",
            originCountry = state?.getOrNull(2) as? String ?: "",
            timePosition = (state?.getOrNull(3) as? Double)?.toLong() ?: -1L,
            lastContact = (state?.getOrNull(4) as? Double)?.toLong() ?: -1L,
            longitude = (state?.getOrNull(5) as? Double) ?: -1.0,
            latitude = (state?.getOrNull(6) as? Double) ?: -1.0,
            baroAltitude = (state?.getOrNull(7) as? Double) ?: -1.0,
            onGround = state?.getOrNull(8) as? Boolean ?: false,
            velocity = (state?.getOrNull(9) as? Double) ?: -1.0,
            trueTrack = (state?.getOrNull(10) as? Double) ?: -1.0,
            verticalRate = (state?.getOrNull(11) as? Double) ?: -1.0,
            sensors = state?.getOrNull(12) as? List<Int> ?: emptyList(),
            geoAltitude = (state?.getOrNull(13) as? Double) ?: -1.0,
            squawk = state?.getOrNull(14) as? String ?: "",
            spi = state?.getOrNull(15) as? Boolean ?: false,
            positionSource = (state?.getOrNull(16) as? Double)?.toInt() ?: -1
        )
    }


}