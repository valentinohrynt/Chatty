package com.inoo.chatty.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.inoo.chatty.R
import com.inoo.chatty.ui.component.BirthDateTextField
import com.inoo.chatty.ui.component.LoadingState
import com.inoo.chatty.ui.component.ShowToast
import com.inoo.chatty.ui.component.ToastType
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(onSuccess: () -> Unit) {
    val viewModel = hiltViewModel<AuthViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val loginStatus by viewModel.loginStatus.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isLogin = remember { mutableStateOf(true) }
    val toastMessage = remember { mutableStateOf("") }
    val toastType = remember { mutableStateOf(ToastType.SUCCESS) }
    val showToast = remember { mutableStateOf(false) }

    LaunchedEffect(loginStatus) {
        viewModel.getLoginStatus()
        if (loginStatus) {
            onSuccess()
        }
    }

    LaunchedEffect(uiState) {
        when {
            uiState.success != null -> {
                toastMessage.value = uiState.success.orEmpty()
                showToast.value = true
                viewModel.setSuccess(null)
                onSuccess()
            }
            uiState.error != null -> {
                toastMessage.value = uiState.error.orEmpty()
                showToast.value = true
                toastType.value = ToastType.ERROR
                viewModel.setError(null)
            }
        }
    }

    Box(
        modifier = Modifier.padding(40.dp),
    ) {
        AnimatedContent(
            targetState = isLogin.value,
            transitionSpec = {
                val slideDirection = if (targetState) 1f else -1f

                (slideInHorizontally(
                    initialOffsetX = { width -> (width * slideDirection).toInt() },
                    animationSpec = tween(500, easing = EaseOutBack)
                ) + fadeIn(
                    animationSpec = tween(300)
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(500, easing = EaseOutBack)
                )).with(
                    slideOutHorizontally(
                        targetOffsetX = { width -> (-width * slideDirection).toInt() },
                        animationSpec = tween(500, easing = EaseInBack)
                    ) + fadeOut(
                        animationSpec = tween(300)
                    ) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(500, easing = EaseInBack)
                    )
                ).using(
                    SizeTransform(clip = false)
                )
            },
            label = "auth_screen_animation",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = false
                }
        ) { isLoginScreen ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = false
                    }
            ) {
                if (isLoginScreen) {
                    LoginScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber,
                        verificationCode = verificationCode,
                        changeToRegister = {
                            isLogin.value = false
                        },
                        onSuccess = onSuccess,
                        onGoogleSignInClick = {
                            val credentialManager = CredentialManager.create(context)

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(getString(context, R.string.default_web_client_id))
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            coroutineScope.launch {
                                try {
                                    val result: GetCredentialResponse = credentialManager.getCredential(
                                        request = request,
                                        context = context,
                                    )
                                    when (val credential = result.credential) {
                                        is CustomCredential -> {
                                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                try {
                                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                                    viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                                                } catch (e: GoogleIdTokenParsingException) {
                                                    Log.d("Error", e.message.toString())
                                                }
                                            } else {
                                                Log.d("Error", "Invalid credential type")
                                            }
                                        }
                                        else -> {
                                            Log.d("Error", "Invalid credential")
                                        }
                                    }
                                } catch (e: GetCredentialException) {
                                    Log.d("Error", e.message.toString())
                                }
                            }
                        },
                        onPhoneSignInClick = {
                            viewModel.sendVerificationCode(context as Activity)
                        },
                        onVerifyCodeClick = {
                            viewModel.loginWithPhone()
                        }
                    )
                } else {
                    RegisterScreen(
                        viewModel = viewModel,
                        uiState = uiState,
                        name = name,
                        email = email,
                        password = password,
                        changeToLogin = {
                            isLogin.value = true
                        },
                        onSuccess = {
                            isLogin.value = true
                        }
                    )
                }
            }
        }

        if (showToast.value) {
            ShowToast(
                message = toastMessage.value,
                type = toastType.value,
                duration = 2000L,
                onDismiss = { showToast.value = false }
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    uiState: AuthUIState,
    email: String?,
    password: String?,
    phoneNumber: String?,
    verificationCode: String?,
    changeToRegister: () -> Unit,
    onSuccess: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onPhoneSignInClick: () -> Unit,
    onVerifyCodeClick: () -> Unit
) {
    var loginMethod by remember { mutableStateOf(LoginMethod.EMAIL) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LoginMethodTab(
                    text = stringResource(id = R.string.email_login),
                    selected = loginMethod == LoginMethod.EMAIL,
                    onClick = { loginMethod = LoginMethod.EMAIL }
                )
                Spacer(modifier = Modifier.width(16.dp))
                LoginMethodTab(
                    text = stringResource(id = R.string.phone_login),
                    selected = loginMethod == LoginMethod.PHONE,
                    onClick = { loginMethod = LoginMethod.PHONE }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (loginMethod) {
                LoginMethod.EMAIL -> EmailLoginContent(
                    email = email,
                    password = password,
                    viewModel = viewModel,
                    uiState = uiState
                )
                LoginMethod.PHONE -> PhoneLoginContent(
                    phoneNumber = phoneNumber,
                    viewModel = viewModel,
                    uiState = uiState,
                    onPhoneSignInClick = onPhoneSignInClick,
                    onVerifyCodeClick = onVerifyCodeClick,
                    verificationCode = verificationCode
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGoogleSignInClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = stringResource(id = R.string.sign_in_with_google)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.sign_in_with_google))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = stringResource(id = R.string.no_account))
                    TextButton(onClick = changeToRegister) {
                        Text(text = stringResource(id = R.string.register))
                    }
                }
            }
        }
    }
    if (uiState.success != null) {
        onSuccess()
    }
}

@Composable
private fun EmailLoginContent(
    email: String?,
    password: String?,
    viewModel: AuthViewModel,
    uiState: AuthUIState
) {
    OutlinedTextField(
        value = email.orEmpty(),
        onValueChange = { viewModel.setEmail(it) },
        label = { Text(text = stringResource(id = R.string.email)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = password.orEmpty(),
        onValueChange = { viewModel.setPassword(it) },
        label = { Text(text = stringResource(id = R.string.password)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { viewModel.login() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isLoading
    ) {
        if (uiState.isLoading) {
            LoadingState()
        } else {
            Text(text = stringResource(id = R.string.login))
        }
    }
}

@Composable
private fun PhoneLoginContent(
    phoneNumber: String?,
    verificationCode: String?,
    viewModel: AuthViewModel,
    uiState: AuthUIState,
    onPhoneSignInClick: () -> Unit,
    onVerifyCodeClick: () -> Unit
) {
    if (uiState.isCodeSent) {
        OutlinedTextField(
            value = verificationCode.orEmpty(),
            onValueChange = { viewModel.setVerificationCode(it) },
            label = { Text(text = stringResource(id = R.string.verification_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onVerifyCodeClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && verificationCode?.isNotBlank() == true
        ) {
            if (uiState.isLoading) {
                LoadingState()
            } else {
                Text(text = stringResource(id = R.string.verify_code))
            }
        }
    } else {
        OutlinedTextField(
            value = phoneNumber.orEmpty(),
            onValueChange = { viewModel.setPhoneNumber(it) },
            label = { Text(text = stringResource(id = R.string.phone_number)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Text(text = "+62", modifier = Modifier.padding(start = 8.dp))
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onPhoneSignInClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && phoneNumber?.isNotBlank() == true
        ) {
            if (uiState.isLoading) {
                LoadingState()
            } else {
                Text(text = stringResource(id = R.string.send_verification_code))
            }
        }
    }
}

@Composable
private fun LoginMethodTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class LoginMethod {
    EMAIL, PHONE
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    uiState: AuthUIState,
    name: String?,
    email: String?,
    password: String?,
    changeToLogin: () -> Unit,
    onSuccess: () -> Unit
){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chatty",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name.orEmpty(),
                onValueChange = { viewModel.setName(it) },
                label = { Text(text = stringResource(id = R.string.name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            BirthDateTextField(viewModel = viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email.orEmpty(),
                onValueChange = { viewModel.setEmail(it) },
                label = { Text(text = stringResource(id = R.string.email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password.orEmpty(),
                onValueChange = { viewModel.setPassword(it) },
                label = { Text(text = stringResource(id = R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button (
                onClick = { viewModel.register() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(8.dp)
                    )
                } else {
                    Text(text = stringResource(id = R.string.register))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box (
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.already_have_account)
                    )
                    TextButton(
                        onClick = { changeToLogin() }
                    ) {
                        Text(text = stringResource(id = R.string.login))
                    }
                }
            }
        }
    }
    if (uiState.success != null) {
        onSuccess()
    }
}