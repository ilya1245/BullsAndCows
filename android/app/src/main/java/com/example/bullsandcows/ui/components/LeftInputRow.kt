package com.example.bullsandcows.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bullsandcows.R

@Composable
fun LeftInputRow(
    value:      String,
    isActive:   Boolean,
    onActivate: () -> Unit,
    enabled:    Boolean,
    onSubmit:   () -> Unit,
    modifier:   Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        GameInputField(
            value       = value,
            placeholder = stringResource(R.string.input_your_guess),
            isActive    = isActive,
            enabled     = enabled,
            onClick     = onActivate,
            modifier    = Modifier.weight(1f)
        )
        Button(
            onClick  = onSubmit,
            enabled  = enabled && value.isNotBlank() && !value.contains('*'),
            modifier = Modifier.height(40.dp)
        ) {
            Text(stringResource(R.string.btn_make_move))
        }
    }
}
