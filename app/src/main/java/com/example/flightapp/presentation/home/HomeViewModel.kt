package com.example.flightapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightapp.domain.usecase.home.GetAllStatesUseCase
import com.example.flightapp.util.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = HomeContract.HomeUiState()
        )

    private fun loadFlightData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = "")

            getAllStatesUseCase(
                lamin = 40.22,
                lomin = 27.34,
                lamax = 41.60,
                lomax = 30.74
            ).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    flightData = response,
                )
            }.launchIn(viewModelScope)
        }
    }

}


