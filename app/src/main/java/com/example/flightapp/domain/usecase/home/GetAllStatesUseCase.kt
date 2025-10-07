package com.example.flightapp.domain.usecase.home

import com.example.flightapp.domain.repository.HomeRepository
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllStatesUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    operator fun invoke(
        lamin: Double? = null,
        lomin: Double? = null,
        lamax: Double? = null,
        lomax: Double? = null
    ) = flow {
        emit(homeRepository.getAllStates(lamin, lomin, lamax, lomax))
    }
}
