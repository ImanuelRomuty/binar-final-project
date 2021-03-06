package com.binar.secondhand.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.binar.secondhand.data.source.remote.AuthRemoteDataSource
import com.binar.secondhand.data.source.remote.request.LoginRequest
import com.binar.secondhand.data.source.remote.response.LoginResponse
import com.binar.secondhand.storage.AppLocalData
import com.binar.secondhand.storage.UserLoggedIn
import com.binar.secondhand.utils.loge
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

interface AuthRepository {
    fun login(loginRequest: LoginRequest): LiveData<Result<LoginResponse>>
    fun setUserLoggedIn(userLoggedIn: UserLoggedIn)
    fun isUserHasLoggedIn(): Boolean
}

class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val appLocalData: AppLocalData,
) : AuthRepository {
    override fun login(loginRequest: LoginRequest): LiveData<Result<LoginResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = authRemoteDataSource.login(loginRequest)

                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        emit(Result.Success(it))
                    }
                } else {
                    loge("login() => Request error")
                    val error = response.errorBody()?.string()
                    if (error != null) {
                        val jsonObject = JSONObject(error)
                        val message = jsonObject.getString("message")
                        emit(Result.Error(null, message))
                    }
                }
            } catch (e: Exception) {
                loge("login() => ${e.message}")
                emit(Result.Error(null, "Something went wrong"))
            }
        }

    override fun setUserLoggedIn(userLoggedIn: UserLoggedIn) =
        appLocalData.setUserLoggedIn(userLoggedIn)

    override fun isUserHasLoggedIn(): Boolean =
        appLocalData.isUserHasLoggedIn
}