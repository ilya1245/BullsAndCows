package com.example.bullsandcows.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors()
    val textColor = MaterialTheme.colorScheme.onSurface
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor)
    val focusModifier = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier

    BasicTextField(
        value             = value,
        onValueChange     = onValueChange,
        enabled           = enabled,
        readOnly          = readOnly,
        singleLine        = true,
        keyboardOptions   = keyboardOptions,
        keyboardActions   = keyboardActions,
        textStyle         = textStyle,
        cursorBrush       = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        modifier          = focusModifier.then(modifier),
        decorationBox     = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value                = value,
                innerTextField       = innerTextField,
                enabled              = enabled,
                singleLine           = true,
                visualTransformation = VisualTransformation.None,
                interactionSource    = interactionSource,
                placeholder          = placeholder,
                label                = label,
                trailingIcon         = trailingIcon,
                colors               = colors,
                contentPadding       = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                container            = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled           = enabled,
                        isError           = false,
                        interactionSource = interactionSource,
                        colors            = colors
                    )
                }
            )
        }
    )
}
