package com.example.bullsandcows.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bullsandcows.R
import com.example.bullsandcows.ui.screens.KbTarget

@Composable
fun RightInputRow(
    bulls:        String,
    cows:         String,
    kbTarget:     KbTarget,
    onFieldClick: (KbTarget) -> Unit,
    enabled:      Boolean,
    onSubmit:     () -> Unit,
    modifier:     Modifier = Modifier
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
        GameInputField(
            value       = bulls,
            placeholder = "0",
            isActive    = kbTarget == KbTarget.RIGHT_BULLS,
            enabled     = enabled,
            onClick     = { onFieldClick(KbTarget.RIGHT_BULLS) },
            modifier    = Modifier.width(56.dp)
        )
        Text(
            text  = stringResource(R.string.input_cows),
            style = MaterialTheme.typography.bodyMedium
        )
        GameInputField(
            value       = cows,
            placeholder = "0",
            isActive    = kbTarget == KbTarget.RIGHT_COWS,
            enabled     = enabled,
            onClick     = { onFieldClick(KbTarget.RIGHT_COWS) },
            modifier    = Modifier.width(56.dp)
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
