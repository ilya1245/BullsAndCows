package com.example.bullsandcows.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bullsandcows.R

@Composable
fun LeftInputRow(
    value:          String,
    onChange:       (String) -> Unit,
    enabled:        Boolean,
    onSubmit:       () -> Unit,
    modifier:       Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        CompactOutlinedTextField(
            value           = value,
            onValueChange   = { onChange(it.take(8)) },
            enabled         = enabled,
            placeholder     = { Text(stringResource(R.string.input_your_guess)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            focusRequester  = focusRequester,
            modifier        = Modifier.weight(1f)
        )
        Button(
            onClick  = onSubmit,
            enabled  = enabled && value.isNotBlank(),
            modifier = Modifier.height(40.dp)
        ) {
            Text(stringResource(R.string.btn_make_move))
        }
    }
}
