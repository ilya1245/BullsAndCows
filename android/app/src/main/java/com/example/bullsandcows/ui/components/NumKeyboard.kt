package com.example.bullsandcows.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumKeyboard(
    onKey:    (Char) -> Unit,
    onHide:   () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 8.dp,
        modifier       = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: 1..5
            KeyRow(keys = listOf('1', '2', '3', '4', '5'), onKey = onKey)
            // Row 2: 6..0
            KeyRow(keys = listOf('6', '7', '8', '9', '0'), onKey = onKey)
            // Row 3: * ? ⌫  ⌄(hide, double width)
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (ch in listOf('*', '?', '⌫')) {
                    KeyButton(
                        label    = ch.toString(),
                        onClick  = { onKey(ch) },
                        modifier = Modifier.weight(1f)
                    )
                }
                KeyButton(
                    label    = "⌄",
                    onClick  = onHide,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
private fun KeyRow(keys: List<Char>, onKey: (Char) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (ch in keys) {
            KeyButton(
                label    = ch.toString(),
                onClick  = { onKey(ch) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeyButton(
    label:    String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick          = onClick,
        modifier         = modifier.height(48.dp),
        contentPadding   = PaddingValues(0.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
