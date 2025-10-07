package com.example.flightapp.presentation.home

import com.example.flightapp.domain.model.GetAllStatesUiModel

object HomeContract {

    data class HomeUiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val flightData: GetAllStatesUiModel = GetAllStatesUiModel(),
        val coordinates: Coordinates = Coordinates(),
        val timeStamp: Long = System.currentTimeMillis()
    )

    sealed class HomeAction{
        data class GetFlightData(
            val coordinates: Coordinates
        ): HomeAction()
    }

    data class Coordinates(
        val laMin: Double = 40.22,
        val loMin: Double = 27.34,
        val laMax: Double = 41.60,
        val loMax: Double = 30.74
    )
}