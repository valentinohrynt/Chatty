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
import com.inoo.chatty.model.User
import com.inoo.chatty.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import com.inoo.chatty.repository.Result

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val chattyAppPreferences: ChattyAppPreferences
) : ViewModel() {

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

    fun setError(error: String?) {
        _uiState.value = _uiState.value.copy(error = error, isLoading = false)
    }

    fun setSuccess(success: String?) {
        _uiState.value = _uiState.value.copy(success = success)
    }

    fun login() {
        if (!validateInputs(isLogin = true)) {
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(email.value, password.value).await()
                val user = firebaseAuth.currentUser

                if (user?.isEmailVerified == true) {
                    val userRef = firestore.collection("users").document(user.uid).get().await()

                    val userData = User(
                        uid = user.uid,
                        name = userRef.getString("name"),
                        phoneNumber = userRef.getString("phone_number").orEmpty(),
                        birthDate = userRef.getString("birth_date"),
                        email = userRef.getString("email"),
                        profilePicture = userRef.getString("profile_picture"),
                        createdAt = userRef.getTimestamp("created_at")?.toDate().toString()
                    )

                    userRepository.insertUser(user = userData).observeForever { result ->
                        when (result) {
                            is Result.Success -> {
                                viewModelScope.launch {
                                    chattyAppPreferences.setLoggedIn()
                                    _uiState.value = _uiState.value.copy(
                                        success = "Login successful",
                                        isLoading = false
                                    )
                                }
                            }

                            is Result.Error -> {
                                _uiState.value =
                                    _uiState.value.copy(error = result.error, isLoading = false)
                            }

                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
                } else {
                    firebaseAuth.signOut()
                    _uiState.value = _uiState.value.copy(
                        error = "Email not verified, please check your email",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.localizedMessage ?: "Login failed",
                    isLoading = false
                )
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
                    val userRef = firestore.collection("users").document(user.uid).get().await()

                    if (!userRef.exists()) {
                        val firestoreUser = hashMapOf(
                            "name" to user.displayName,
                            "phone_number" to user.phoneNumber.toString(),
                            "email" to user.email,
                            "birth_date" to birthDate.value,
                            "profile_picture" to user.photoUrl.toString(),
                            "created_at" to FieldValue.serverTimestamp()
                        )
                        firestore.collection("users")
                            .document(user.uid)
                            .set(firestoreUser)
                            .await()
                    }

                    val updatedUserRef =
                        firestore.collection("users").document(user.uid).get().await()

                    val userData = User(
                        uid = user.uid,
                        name = updatedUserRef.getString("name"),
                        phoneNumber = updatedUserRef.getString("phone_number").orEmpty(),
                        birthDate = updatedUserRef.getString("birth_date"),
                        email = updatedUserRef.getString("email"),
                        profilePicture = updatedUserRef.getString("profile_picture"),
                        createdAt = updatedUserRef.getTimestamp("created_at")?.toDate().toString()
                    )

                    userRepository.insertUser(user = userData).observeForever { result ->
                        when (result) {
                            is Result.Success -> {
                                viewModelScope.launch {
                                    chattyAppPreferences.setLoggedIn()
                                    _uiState.value = _uiState.value.copy(
                                        success = "Login successful",
                                        isLoading = false
                                    )
                                }
                            }

                            is Result.Error -> {
                                _uiState.value =
                                    _uiState.value.copy(error = result.error, isLoading = false)
                            }

                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
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
                _uiState.value = _uiState.value.copy(
                    error = "Verification failed: ${e.message}",
                    isLoading = false
                )
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.value = _uiState.value.copy(
                    verificationId = verificationId,
                    isCodeSent = true
                )
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+62" + phoneNumber.value)
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
                        val userRef = firestore.collection("users").document(user.uid).get().await()

                        val userData = User(
                            uid = user.uid,
                            name = userRef.getString("name"),
                            phoneNumber = phoneNumber.value,
                            birthDate = userRef.getString("birth_date"),
                            email = userRef.getString("email"),
                            profilePicture = userRef.getString("profile_picture"),
                            createdAt = userRef.getTimestamp("created_at")?.toString()
                        )

                        chattyAppPreferences.setLoggedIn()
                        _uiState.value =
                            _uiState.value.copy(success = "Login successful", isLoading = false)
                    } else {
                        _uiState.value =
                            _uiState.value.copy(error = "Login failed", isLoading = false)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Verification ID or SMS code is missing",
                    isLoading = false
                )
            }
        }
    }

    fun register() {
        if (!validateInputs(isLogin = false)) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val registrationResult =
                    firebaseAuth.createUserWithEmailAndPassword(email.value, password.value).await()

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
                    "phone_number" to phoneNumber.value,
                    "profile_picture" to user.photoUrl.toString(),
                    "created_at" to FieldValue.serverTimestamp()
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(firestoreUser)
                    .await()

                _uiState.value = _uiState.value.copy(
                    success = "Registration successful, please verify your email",
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Password is too weak"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                    is FirebaseAuthUserCollisionException -> "Email already in use"
                    else -> e.localizedMessage ?: "Registration failed"
                }
                _uiState.value = _uiState.value.copy(error = errorMessage, isLoading = false)
            }
        }
    }

    private fun validateInputs(isLogin: Boolean = true): Boolean {
        return when {
            !isLogin && name.value.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Name cannot be empty")
                false
            }

            !isLogin && birthDate.value.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Birth date cannot be empty")
                false
            }

            email.value.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.value).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Invalid email address")
                false
            }

            password.value.length < 6 -> {
                _uiState.value =
                    _uiState.value.copy(error = "Password must be at least 6 characters")
                false
            }

            else -> true
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.deleteUser().collect { result ->
                when (result) {
                    is Result.Success -> {
                        firebaseAuth.signOut()
                        chattyAppPreferences.clear()
                        _loginStatus.value = false
                    }

                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.error)
                    }

                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun getLoginStatus() {
        viewModelScope.launch {
            chattyAppPreferences.hasLoggedIn.collect {
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