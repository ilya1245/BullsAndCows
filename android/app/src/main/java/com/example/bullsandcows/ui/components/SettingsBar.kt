package com.example.bullsandcows.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.example.bullsandcows.R
import com.example.bullsandcows.model.GameSettings
import com.example.bullsandcows.model.StatusSpec

@Composable
fun TopBar(onMenuClick: () -> Unit, statusSpec: StatusSpec) {
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", modifier = Modifier.size(18.dp))
            }
            Text(
                text     = statusSpec.resolve(),
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusSpec.resolve(): String = when (this) {
    StatusSpec.Configure -> stringResource(R.string.status_configure)
    StatusSpec.Broken    -> stringResource(R.string.status_game_broken)
    is StatusSpec.Running -> {
        val s = settings
        val typeText = stringResource(when (s.gameType) {
            1    -> R.string.status_you_guess
            2    -> R.string.status_comp_guesses
            else -> R.string.status_both_guess
        })
        val numText = stringResource(R.string.status_digits, s.numSize)
        var result = "$typeText | $numText"
        if (s.gameType != 1) {
            val skillLabel = listOf(
                stringResource(R.string.skill_highest),
                stringResource(R.string.skill_high),
                stringResource(R.string.skill_medium),
                stringResource(R.string.skill_low)
            )[s.skill - 1]
            result += " | " + stringResource(R.string.status_skill, skillLabel)
        }
        if (s.gameType == 3) {
            val firstLabel = listOf(
                stringResource(R.string.first_move_you),
                stringResource(R.string.first_move_comp)
            )[s.whoFirst - 1]
            result += " | " + stringResource(R.string.status_first, firstLabel)
        }
        result
    }
}

@Composable
fun DrawerContent(
    settings: GameSettings,
    isRunning: Boolean,
    onSettings: (GameSettings) -> Unit,
    onNewGame: () -> Unit,
    onBreak: () -> Unit,
    onHelp: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language selector — always enabled
            val currentLang = remember {
                val tag = AppCompatDelegate.getApplicationLocales()[0]?.language ?: ""
                when (tag) { "en" -> 1; "uk" -> 2; else -> 0 }
            }
            DropdownSetting(
                label    = stringResource(R.string.settings_language),
                options  = listOf(stringResource(R.string.lang_system), "English", "Українська"),
                selected = currentLang,
                enabled  = true,
                onSelect = { idx ->
                    val locales = when (idx) {
                        1    -> LocaleListCompat.forLanguageTags("en")
                        2    -> LocaleListCompat.forLanguageTags("uk")
                        else -> LocaleListCompat.getEmptyLocaleList()
                    }
                    AppCompatDelegate.setApplicationLocales(locales)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text  = stringResource(R.string.settings_game_type),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            DropdownSetting(
                label    = stringResource(R.string.settings_game_type),
                options  = listOf(
                    stringResource(R.string.game_type_comp_thinks),
                    stringResource(R.string.game_type_player_thinks),
                    stringResource(R.string.game_type_both_think)
                ),
                selected = settings.gameType - 1,
                enabled  = !isRunning,
                onSelect = { onSettings(settings.copy(gameType = it + 1)) }
            )

            DropdownSetting(
                label    = stringResource(R.string.settings_num_size),
                options  = listOf("3", "4", "5", "6"),
                selected = settings.numSize - 3,
                enabled  = !isRunning,
                onSelect = { onSettings(settings.copy(numSize = it + 3)) }
            )

            if (settings.gameType != 1) {
                DropdownSetting(
                    label    = stringResource(R.string.settings_skill),
                    options  = listOf(
                        stringResource(R.string.skill_highest),
                        stringResource(R.string.skill_high),
                        stringResource(R.string.skill_medium),
                        stringResource(R.string.skill_low)
                    ),
                    selected = settings.skill - 1,
                    enabled  = !isRunning,
                    onSelect = { onSettings(settings.copy(skill = it + 1)) }
                )
            }

            if (settings.gameType == 3) {
                DropdownSetting(
                    label    = stringResource(R.string.settings_first_move),
                    options  = listOf(
                        stringResource(R.string.first_move_you),
                        stringResource(R.string.first_move_comp)
                    ),
                    selected = settings.whoFirst - 1,
                    enabled  = !isRunning,
                    onSelect = { onSettings(settings.copy(whoFirst = it + 1)) }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked         = settings.lastMove,
                        onCheckedChange = { onSettings(settings.copy(lastMove = it)) },
                        enabled         = !isRunning
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text  = stringResource(R.string.settings_last_move),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Button(
                onClick  = onNewGame,
                enabled  = !isRunning,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_new_game)) }

            OutlinedButton(
                onClick  = onBreak,
                enabled  = isRunning,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_break_game)) }

            OutlinedButton(
                onClick  = onHelp,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_help)) }
        }
    }
}

// ---- Reusable dropdown ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSetting(
    label: String,
    options: List<String>,
    selected: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier         = modifier
    ) {
        CompactOutlinedTextField(
            value        = options.getOrElse(selected) { "" },
            onValueChange = {},
            readOnly     = true,
            enabled      = enabled,
            label        = { Text(label, style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            modifier     = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded         = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { idx, text ->
                DropdownMenuItem(
                    text    = { Text(text) },
                    onClick = { onSelect(idx); expanded = false }
                )
            }
        }
    }
}
