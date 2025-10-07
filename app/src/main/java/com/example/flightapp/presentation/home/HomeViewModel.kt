package com.example.flightapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightapp.domain.usecase.home.GetAllStatesUseCase
import com.example.flightapp.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val getAllStatesUseCase: GetAllStatesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeContract.HomeUiState())

    val uiState: StateFlow<HomeContract.HomeUiState> = _uiState
        .onStart {
            loadFlightData()
            startPeriodicUpdates()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = HomeContract.HomeUiState()
        )

    private fun startPeriodicUpdates() {
        flow {
            while (true) {
                delay(1000)
                emit(Unit)
            }
        }.onEach {
            checkAndUpdateIfNeeded()
        }.launchIn(viewModelScope)
    }

    private fun checkAndUpdateIfNeeded() {
        val currentTime = System.currentTimeMillis()
        val lastUpdateTime = _uiState.value.timeStamp
        val timeDifference = currentTime - lastUpdateTime

        if (timeDifference >= 10000) {
            loadFlightData()
        }
    }

    private fun loadFlightData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")

            getAllStatesUseCase(
                lamin = uiState.value.coordinates.laMin,
                lomin = uiState.value.coordinates.loMin,
                lamax = uiState.value.coordinates.laMax,
                lomax = uiState.value.coordinates.loMax
            ).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    flightData = response,
                    timeStamp = System.currentTimeMillis()
                )
            }.launchIn(viewModelScope)
        }
    }

    fun onAction(action: HomeContract.HomeAction) {
        when (action) {
            is HomeContract.HomeAction.GetFlightData -> {
                _uiState.update {
                    it.copy(
                        coordinates = action.coordinates,
                        timeStamp = System.currentTimeMillis()
                    )
                }

                loadFlightData()
            }

            else -> {
                println("")
            }
        }

    }


}


