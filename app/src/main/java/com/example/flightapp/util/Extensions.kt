package com.example.flightapp.util

import com.example.flightapp.domain.model.RestResult
import com.example.flightapp.domain.network.IResponseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T> Flow<RestResult<T>>.onSuccess(action: suspend (T) -> Unit): Flow<RestResult<T>> {
    return transform { restResult ->
        if (restResult is RestResult.Success) {
            action.invoke(restResult.result)
        }
        emit(restResult)
    }
}

fun <T> Flow<RestResult<T>>.onSuccessWithStatus(
    action: suspend (T, IResponseStatus) -> Unit
): Flow<RestResult<T>> {
    return transform { restResult ->
        if (restResult is RestResult.SuccessWithStatus) {
            action.invoke(restResult.result, restResult.status)
        }
        emit(restResult)
    }
}

fun <T> Flow<RestResult<T>>.onError(action: suspend (String?) -> Unit): Flow<RestResult<T>> {
    return transform { restResult ->
        if (restResult is RestResult.Error) {
            action.invoke(restResult.error)
        }
        emit(restResult)
    }
}