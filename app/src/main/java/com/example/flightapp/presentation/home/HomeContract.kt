package com.example.flightapp.presentation.home

import com.example.flightapp.domain.model.GetAllStatesUiModel

object HomeContract {

    data class HomeUiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val flightData: GetAllStatesUiModel = GetAllStatesUiModel(),
    )
}