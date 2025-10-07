package com.example.flightapp.domain.network

interface IResponseStatus {

    val code: Int?
    val message: String?
}