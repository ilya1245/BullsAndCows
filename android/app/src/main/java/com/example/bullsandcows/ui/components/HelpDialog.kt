package com.example.bullsandcows.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bullsandcows.R
import com.example.bullsandcows.ui.theme.BullColor
import com.example.bullsandcows.ui.theme.CowColor

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text       = stringResource(R.string.help_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier            = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.help_intro))

                HelpSection(stringResource(R.string.help_how_to_play_title))
                Text(stringResource(R.string.help_how_to_play))
                Text(stringResource(R.string.help_bull),  color = BullColor)
                Text(stringResource(R.string.help_cow),   color = CowColor)
                Text(stringResource(R.string.help_win))

                HelpSection(stringResource(R.string.help_example_title))
                Surface(
                    tonalElevation = 2.dp,
                    shape          = MaterialTheme.shapes.small
                ) {
                    Text(
                        text     = stringResource(R.string.help_example),
                        modifier = Modifier.padding(8.dp),
                        style    = MaterialTheme.typography.bodySmall
                    )
                }

                HelpSection(stringResource(R.string.help_trick_title))
                Text(stringResource(R.string.help_trick))

                HelpSection(stringResource(R.string.help_types_title))
                Text("1. " + stringResource(R.string.help_type_1))
                Text("2. " + stringResource(R.string.help_type_2))
                Text("3. " + stringResource(R.string.help_type_3))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_ok))
            }
        }
    )
}

@Composable
private fun HelpSection(title: String) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(top = 4.dp)
    )
}
