package com.example.bullsandcows.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameInputField(
    value: String,
    placeholder: String,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        !enabled  -> MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        isActive  -> MaterialTheme.colorScheme.primary
        else      -> MaterialTheme.colorScheme.outline
    }
    val borderWidth = if (isActive) 2.dp else 1.dp

    Surface(
        shape  = RoundedCornerShape(4.dp),
        border = BorderStroke(borderWidth, borderColor),
        color  = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .height(40.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text  = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            } else {
                Text(
                    text  = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}
