package com.inoo.chatty.ui.main.profile

import androidx.lifecycle.ViewModel
import com.inoo.chatty.model.User
import com.inoo.chatty.repository.Result
import com.inoo.chatty.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData = _userData.asStateFlow()

    fun getUserData() {
        userRepository.getUserData().observeForever { result ->
            when (result) {
                is com.inoo.chatty.repository.Result.Success -> {
                    _uiState.value = _uiState.value.copy(success = "Success", isLoading = false)
                    _userData.value = result.data
                }

                is com.inoo.chatty.repository.Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.error, isLoading = false)
                }

                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
}

data class ProfileScreenUiState(
    val isLoading: Boolean = true,
    val success: String? = null,
    val error: String? = null
)