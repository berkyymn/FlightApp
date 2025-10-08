package com.example.flightapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightapp.domain.model.GetAllStatesUiModel
import com.example.flightapp.domain.usecase.home.GetAllStatesUseCase
import com.example.flightapp.util.onError
import com.example.flightapp.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val getAllStatesUseCase: GetAllStatesUseCase
) : ViewModel() {

    private var periodicUpdateJob: Job? = null

    private val _effect = Channel<HomeContract.HomeEffect>()
    val effect = _effect.receiveAsFlow()


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
        periodicUpdateJob?.cancel()
        periodicUpdateJob = flow {
            while (true) {
                delay(HomeContract.CHECK_UPDATE_INTERVAL)
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

        if (timeDifference >= HomeContract.WAIT_AFTER_LAST_UPDATE) {
            loadFlightData()
        }
    }

    private fun loadFlightData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }

            getAllStatesUseCase(
                lamin = uiState.value.coordinates.laMin,
                lomin = uiState.value.coordinates.loMin,
                lamax = uiState.value.coordinates.laMax,
                lomax = uiState.value.coordinates.loMax
            ).onSuccess { response ->

                val filteredFlights = filterFlightData(response, uiState.value.originCountry)

                _uiState.update {
                    _uiState.value.copy(
                        isLoading = false,
                        allFlightData = response,
                        originCountry = if (filteredFlights.states.isEmpty()) {
                            HomeContract.DEFAULT_ORIGIN_COUNTRY
                        } else {
                            uiState.value.originCountry
                        },
                        filteredFlightData = if (filteredFlights.states.isEmpty()) {
                            response
                        } else {
                            filteredFlights
                        },
                        timeStamp = System.currentTimeMillis()
                    )
                }
            }.onError {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = it.error
                    )
                }

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

            is HomeContract.HomeAction.ChangeOriginCountry -> {
                _uiState.update {
                    it.copy(
                        originCountry = action.country,
                        filteredFlightData = filterFlightData(it.allFlightData, action.country)
                    )
                }
            }

            is HomeContract.HomeAction.OnMarkerClick -> {
                action.callSign?.let {
                    uiState.value.filteredFlightData.states.find { flight -> flight.callsign == action.callSign }
                        ?.let { flightState ->
                            _effect.trySend(
                                HomeContract.HomeEffect.ShowFlightInfoInToastMessage(
                                    flightState
                                )
                            )
                        }
                }
            }


            is HomeContract.HomeAction.OnResume -> {
                startPeriodicUpdates()
            }

            is HomeContract.HomeAction.OnPause -> {
                periodicUpdateJob?.cancel()
            }
        }
    }

    private fun filterFlightData(
        allData: GetAllStatesUiModel,
        country: String
    ): GetAllStatesUiModel {
        return if (country == HomeContract.DEFAULT_ORIGIN_COUNTRY) {
            allData
        } else {
            allData.copy(
                states = allData.states.filter { flight ->
                    flight.originCountry == country
                } as ArrayList
            )
        }
    }
}
