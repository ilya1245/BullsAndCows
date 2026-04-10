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
fun RightInputRow(
    bulls:              String,
    cows:               String,
    onBulls:            (String) -> Unit,
    onCows:             (String) -> Unit,
    enabled:            Boolean,
    onSubmit:           () -> Unit,
    modifier:           Modifier = Modifier,
    bullsFocusRequester: FocusRequester? = null
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = stringResource(R.string.input_bulls),
            style = MaterialTheme.typography.bodyMedium
        )
        CompactOutlinedTextField(
            value           = bulls,
            onValueChange   = { v: String -> if (v.length <= 1) onBulls(v.filter { c -> c.isDigit() }) },
            enabled         = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction    = ImeAction.Next
            ),
            focusRequester  = bullsFocusRequester,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text  = stringResource(R.string.input_cows),
            style = MaterialTheme.typography.bodyMedium
        )
        CompactOutlinedTextField(
            value           = cows,
            onValueChange   = { v: String -> if (v.length <= 1) onCows(v.filter { c -> c.isDigit() }) },
            enabled         = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.width(56.dp)
        )
        Button(
            onClick  = onSubmit,
            enabled  = enabled && bulls.isNotBlank() && cows.isNotBlank(),
            modifier = Modifier.height(40.dp)
        ) {
            Text(stringResource(R.string.btn_answer))
        }
    }
}
