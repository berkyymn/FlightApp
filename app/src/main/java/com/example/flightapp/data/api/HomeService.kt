package com.example.flightapp.data.api

import com.example.flightapp.data.model.GetAllStatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface HomeService {
    
    @GET("states/all")
    suspend fun getAllStates(
        @Query("lamin") lamin: Double? = null,
        @Query("lomin") lomin: Double? = null,
        @Query("lamax") lamax: Double? = null,
        @Query("lomax") lomax: Double? = null
    ): Response<GetAllStatesResponse>
}
