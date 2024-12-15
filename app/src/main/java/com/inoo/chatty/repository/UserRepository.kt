package com.inoo.chatty.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.inoo.chatty.data.local.dao.UserDao
import com.inoo.chatty.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(
    private val userDao: UserDao
) {
    fun getUserData(): LiveData<Result<User>> = liveData {
        emit(Result.Loading)
        try {
            val user = userDao.getUserData()
            if (user != null) {
                emit(Result.Success(user))
            } else {
                emit(Result.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred"))
        }
    }

    fun insertUser(user: User): LiveData<Result<Unit>> = liveData {
        emit(Result.Loading)
        try {
            userDao.insertUser(user)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred"))
        }
    }

    fun deleteUser(): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            userDao.deleteUser()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An error occurred"))
        }
    }
}