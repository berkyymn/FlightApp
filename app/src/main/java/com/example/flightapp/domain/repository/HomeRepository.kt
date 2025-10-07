package com.example.flightapp.domain.repository

import com.example.flightapp.domain.model.GetAllStatesUiModel
import com.example.flightapp.domain.model.RestResult

interface HomeRepository {

    suspend fun getAllStates(
        lamin: Double? = null,
        lomin: Double? = null,
        lamax: Double? = null,
        lomax: Double? = null
    ): RestResult<GetAllStatesUiModel>

}