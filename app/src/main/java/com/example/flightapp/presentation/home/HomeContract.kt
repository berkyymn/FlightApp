package com.example.flightapp.presentation.home

import com.example.flightapp.domain.model.FlightStateUiModel
import com.example.flightapp.domain.model.GetAllStatesUiModel

object HomeContract {

    const val WAIT_AFTER_CAMERA_MOVEMENT_STOPPED = 1000L
    const val CHECK_UPDATE_INTERVAL = 1000L
    const val WAIT_AFTER_LAST_UPDATE = 10000L
    const val DEFAULT_ORIGIN_COUNTRY = "ALL"


    data class HomeUiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val allFlightData: GetAllStatesUiModel = GetAllStatesUiModel(),
        val filteredFlightData: GetAllStatesUiModel = GetAllStatesUiModel(),
        val originCountry: String = DEFAULT_ORIGIN_COUNTRY,
        val coordinates: Coordinates = Coordinates(),
        val timeStamp: Long = System.currentTimeMillis(),
    )

    sealed class HomeEffect{
        data class ShowFlightInfoInToastMessage(
            val flight: FlightStateUiModel
        ): HomeEffect()
    }

    sealed class HomeAction{
        data class GetFlightData(
            val coordinates: Coordinates
        ): HomeAction()

        data class ChangeOriginCountry(
            val country: String
        ): HomeAction()

        data class OnMarkerClick(
            val callSign: String?
        ): HomeAction()

        data object OnResume: HomeAction()
        data object OnPause: HomeAction()
    }

    data class Coordinates(
        val laMin: Double = 40.22,
        val loMin: Double = 27.34,
        val laMax: Double = 41.60,
        val loMax: Double = 30.74
    )
}