package com.example.flightapp.data.repository

import com.example.flightapp.data.api.HomeService
import com.example.flightapp.data.mapper.GetAllStatesMapper
import com.example.flightapp.domain.model.GetAllStatesUiModel
import com.example.flightapp.domain.model.RestResult
import com.example.flightapp.domain.repository.HomeRepository
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HomeRepositoryImpl @Inject constructor(
    private val homeService: Lazy<HomeService>,
    private val getAllStatesMapper: GetAllStatesMapper
) : HomeRepository {

    override suspend fun getAllStates(
        lamin: Double?,
        lomin: Double?,
        lamax: Double?,
        lomax: Double?
    ): RestResult<GetAllStatesUiModel> {
        return withContext(Dispatchers.IO) {
            try {
                val response = homeService.get().getAllStates(lamin, lomin, lamax, lomax)

                if (response.isSuccessful) {
                    RestResult.Success(getAllStatesMapper.map(response.body()))
                } else {
                    RestResult.Error("not successful")
                }
            } catch (e: Exception) {
                RestResult.Error("")
            }
        }
    }
}
