package com.example.mastermartini.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mastermartini.R
import com.example.mastermartini.ui.theme.MasterMartiniTheme
import com.example.mastermartini.ui.theme.SuccessGreen
import com.example.mastermartini.util.validation.FieldState

private enum class FieldStatus { Neutral, Valid, Error }

/**
 * The app's single form field. It only displays [state]; validation happens in the
 * ViewModel. Shows a leading icon, a ✓ / ⚠ status icon, an animated border color
 * and an animated error message once the field has been touched.
 *
 * @param onFocusLost called the first time focus leaves the field after having it,
 *   so the ViewModel can mark it as touched.
 * @param extraTrailing optional action placed before the status icon (e.g. the location pin).
 */
@Composable
fun ValidatedTextField(
    state: FieldState,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    helperText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
    extraTrailing: (@Composable () -> Unit)? = null,
) {
    val status = when {
        state.showError -> FieldStatus.Error
        state.touched && state.isValid -> FieldStatus.Valid
        else -> FieldStatus.Neutral
    }
    val colors = MaterialTheme.colorScheme
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            FieldStatus.Error -> colors.error
            FieldStatus.Valid -> SuccessGreen
            FieldStatus.Neutral -> colors.outline
        },
        label = "fieldBorder",
    )
    val focusedBorderColor by animateColorAsState(
        targetValue = if (status == FieldStatus.Neutral) colors.primary else borderColor,
        label = "fieldFocusedBorder",
    )
    val iconColor by animateColorAsState(
        targetValue = if (status == FieldStatus.Neutral) colors.onSurfaceVariant else borderColor,
        label = "fieldIcon",
    )

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    // Keep the last message so it stays readable while the error animates out
    var lastError by remember { mutableStateOf("") }
    val shownError = state.error ?: lastError
    SideEffect { if (state.error != null) lastError = state.error }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focus ->
                    if (focus.isFocused) {
                        hadFocus = true
                    } else if (hadFocus) {
                        hadFocus = false
                        onFocusLost()
                    }
                }
                .semantics { if (state.showError) error(state.error.orEmpty()) },
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = iconColor) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    extraTrailing?.invoke()
                    if (isPassword) {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = stringResource(
                                    if (passwordVisible) R.string.cd_hide_password else R.string.cd_show_password
                                ),
                            )
                        }
                    }
                    StatusIcon(status = status, tint = iconColor)
                }
            },
            isError = state.showError,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = borderColor,
                focusedBorderColor = focusedBorderColor,
                errorBorderColor = borderColor,
                errorLabelColor = colors.error,
                errorLeadingIconColor = iconColor,
            ),
        )
        AnimatedVisibility(
            visible = state.showError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = shownError,
                color = colors.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
        if (helperText != null && !state.showError) {
            Text(
                text = helperText,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun StatusIcon(status: FieldStatus, tint: Color) {
    when (status) {
        FieldStatus.Valid -> Icon(
            Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.cd_field_valid),
            tint = tint,
            modifier = Modifier.padding(end = 12.dp),
        )
        FieldStatus.Error -> Icon(
            Icons.Filled.Error,
            contentDescription = stringResource(R.string.cd_field_error),
            tint = tint,
            modifier = Modifier.padding(end = 12.dp),
        )
        FieldStatus.Neutral -> Unit
    }
}

@Preview(showBackground = true)
@Composable
private fun ValidatedTextFieldPreview() {
    MasterMartiniTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ValidatedTextField(FieldState(), {}, {}, "Nombre", Icons.Outlined.Person)
            ValidatedTextField(FieldState("Ana", touched = true), {}, {}, "Nombre", Icons.Outlined.Person)
            ValidatedTextField(
                FieldState("Al", error = "Debe tener al menos 3 letras", touched = true),
                {}, {}, "Nombre", Icons.Outlined.Person,
            )
        }
    }
}
