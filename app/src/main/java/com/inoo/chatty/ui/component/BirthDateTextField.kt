package com.inoo.chatty.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.inoo.chatty.ui.auth.AuthViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.inoo.chatty.R
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun BirthDateTextField(
    viewModel: AuthViewModel
) {
    var birthDate by remember { mutableStateOf(TextFieldValue(text = "")) }

    OutlinedTextField(
        value = birthDate,
        onValueChange = { newValue ->
            val digitsOnly = newValue.text.filter { it.isDigit() }

            val (formattedValue, newCursorPosition) = formatBirthDate(
                digitsOnly,
                newValue.selection.start
            )

            birthDate = TextFieldValue(
                text = formattedValue,
                selection = TextRange(newCursorPosition)
            )

            if (formattedValue.length == 10) {
                viewModel.setBirthDate(formattedValue)
            }
        },
        label = { Text(text = stringResource(id = R.string.birth_date)) },
        placeholder = { Text(text = stringResource(id = R.string.birth_date_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

fun formatBirthDate(input: String, originalCursorPosition: Int): Pair<String, Int> {
    val digitsOnly = input.take(8)

    val formattedValue = when {
        digitsOnly.length <= 2 -> digitsOnly
        digitsOnly.length <= 4 -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
        else -> "${digitsOnly.take(2)}/${digitsOnly.slice(2..3)}/${digitsOnly.drop(4)}"
    }

    val cursorOffset = listOf(2, 5)
    var newCursorPosition = originalCursorPosition

    cursorOffset.forEach { offset ->
        if (originalCursorPosition > offset) newCursorPosition += 1
    }

    newCursorPosition = newCursorPosition.coerceAtMost(formattedValue.length)

    return Pair(formattedValue, newCursorPosition)
}
