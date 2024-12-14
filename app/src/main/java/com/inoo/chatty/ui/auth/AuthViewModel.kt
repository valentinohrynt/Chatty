package com.inoo.chatty.ui.auth

import android.app.Activity
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.inoo.chatty.data.preference.ChattyAppPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val chattyAppPreferences: ChattyAppPreferences
): ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AuthUIState())
    val uiState = _uiState.asStateFlow()

    private val _name = MutableStateFlow<String>("")
    val name = _name.asStateFlow()

    private val _birthDate = MutableStateFlow<String>("")
    val birthDate = _birthDate.asStateFlow()

    private val _phoneNumber = MutableStateFlow<String>("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _email = MutableStateFlow<String>("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow<String>("")
    val password = _password.asStateFlow()

    private val _verificationCode = MutableStateFlow<String>("")
    val verificationCode = _verificationCode.asStateFlow()

    private val _loginStatus = MutableStateFlow<Boolean>(false)
    val loginStatus = _loginStatus.asStateFlow()

    init {
        getLoginStatus()
    }

    fun setName(name: String) {
        _name.value = name
    }

    fun setBirthDate(birthDate: String) {
        _birthDate.value = birthDate
    }

    fun setPhoneNumber(phoneNumber: String) {
        _phoneNumber.value = phoneNumber
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setVerificationCode(code: String) {
        _verificationCode.value = code
    }

//    fun login() {
//        if (!validateInputs(isLogin = true)) {
//            return
//        }
//        _uiState.value = AuthUIState(isLoading = true)
//        viewModelScope.launch {
//            try {
//                firebaseAuth.signInWithEmailAndPassword(email.value, password.value)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val user = firebaseAuth.currentUser
//                            if (user?.isEmailVerified == true) {
//                                viewModelScope.launch {
//                                    chattyAppPreferences.setLoggedIn()
//                                }
//                                _uiState.value = AuthUIState(success = "Login successful")
//                            }
//                            else {
//                                _uiState.value = AuthUIState(error = "Email not verified, please check your email")
//                                firebaseAuth.signOut()
//                            }
//                        } else {
//                            _uiState.value = AuthUIState(error = "Failed to login")
//                        }
//                    }
//            } catch (e: Exception) {
//                _uiState.value = AuthUIState(error = e.localizedMessage)
//            }
//        }
//    }

    fun login() {
        if (!validateInputs(isLogin = true)) {
            return
        }
        _uiState.value = AuthUIState(isLoading = true)
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email.value, password.value).await()
                val user = firebaseAuth.currentUser

                if (user?.isEmailVerified == true) {
                    chattyAppPreferences.setLoggedIn()
                    _uiState.value = AuthUIState(success = "Login successful", isLoading = false)
                } else {
                    firebaseAuth.signOut()
                    _uiState.value = AuthUIState(error = "Email not verified, please check your email", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUIState(error = e.localizedMessage ?: "Login failed", isLoading = false)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                val user = firebaseAuth.currentUser
                if (user != null) {
                    chattyAppPreferences.setLoggedIn()
                    _uiState.value = _uiState.value.copy(success = "Login successful", isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(error = "Login failed", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }

    fun sendVerificationCode(activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val smsCode = credential.smsCode
                if (smsCode != null) {
                    loginWithPhone(smsCode)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.value = _uiState.value.copy(error = "Verification failed: ${e.message}", isLoading = false)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.value = _uiState.value.copy(
                    verificationId = verificationId,
                    isCodeSent = true
                )
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+62"+phoneNumber.value)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        _uiState.value = _uiState.value.copy(isLoading = true)
    }

    fun loginWithPhone(smsCode: String = verificationCode.value) {
        viewModelScope.launch {
            val verificationId = _uiState.value.verificationId
            if (verificationId != null && smsCode.isNotEmpty()) {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
                    firebaseAuth.signInWithCredential(credential).await()
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        chattyAppPreferences.setLoggedIn()
                        _uiState.value = _uiState.value.copy(success = "Login successful", isLoading = false)
                    } else {
                        _uiState.value = _uiState.value.copy(error = "Login failed", isLoading = false)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(error = "Verification ID or SMS code is missing", isLoading = false)
            }
        }
    }


    fun register() {
        if (!validateInputs(isLogin = false)) {
            return
        }

        _uiState.value = AuthUIState(isLoading = true)
        viewModelScope.launch {
            try {
                val registrationResult = firebaseAuth.createUserWithEmailAndPassword(email.value, password.value).await()

                val user = registrationResult.user ?: throw Exception("User creation failed")

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.value)
                    .build()

                user.updateProfile(profileUpdates).await()

                user.sendEmailVerification().await()

                val firestoreUser = hashMapOf(
                    "name" to name.value,
                    "email" to email.value,
                    "birth_date" to birthDate.value,
                    "created_at" to FieldValue.serverTimestamp()
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(firestoreUser)
                    .await()

                _uiState.value = AuthUIState(success = "Registration successful, please verify your email", isLoading = false)

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Password is too weak"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                    is FirebaseAuthUserCollisionException -> "Email already in use"
                    else -> e.localizedMessage ?: "Registration failed"
                }
                _uiState.value = AuthUIState(error = errorMessage, isLoading = false)
            }
        }
    }

    private fun validateInputs(isLogin: Boolean = true): Boolean {
        return when {
            !isLogin && name.value.isBlank() -> {
                _uiState.value = AuthUIState(error = "Name cannot be empty")
                false
            }
            !isLogin && birthDate.value.isBlank() -> {
                _uiState.value = AuthUIState(error = "Birth date cannot be empty")
                false
            }
            email.value.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.value).matches() -> {
                _uiState.value = AuthUIState(error = "Invalid email address")
                false
            }
            password.value.length < 6 -> {
                _uiState.value = AuthUIState(error = "Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }

    suspend fun logout() {
        chattyAppPreferences.clear()
        firebaseAuth.signOut()
    }

    fun setError(error: String?) {
        _uiState.value = _uiState.value.copy(error = error, isLoading = false)
    }

    fun setSuccess(success: String?) {
        _uiState.value = _uiState.value.copy(success = success)
    }

    fun getLoginStatus() {
        viewModelScope.launch {
            chattyAppPreferences.hasLoggedIn.collect{
                _loginStatus.value = it
            }
        }
    }
}

data class AuthUIState(
    val isLoading: Boolean = false,
    val success: String? = null,
    val error: String? = null,
    val verificationId: String? = null,
    val isCodeSent: Boolean = false
)