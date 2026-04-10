package com.example.bullsandcows.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bullsandcows.R
import com.example.bullsandcows.model.Attempt
import com.example.bullsandcows.ui.theme.BullColor
import com.example.bullsandcows.ui.theme.CowColor
import com.example.bullsandcows.ui.theme.RevealRowBg
import com.example.bullsandcows.ui.theme.WinRowBg

@Composable
fun HistoryTable(
    attempts: List<Attempt>,
    moveHeader: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to last row when new attempt added
    LaunchedEffect(attempts.size) {
        if (attempts.isNotEmpty()) listState.animateScrollToItem(attempts.size - 1)
    }

    Column(modifier = modifier) {
        // Header row
        TableRow(
            number = moveHeader,
            bulls  = stringResource(R.string.col_bulls),
            cows   = stringResource(R.string.col_cows),
            isHeader = true,
            isWin    = false,
            isReveal = false
        )
        HorizontalDivider()

        LazyColumn(state = listState) {
            items(attempts) { attempt ->
                TableRow(
                    number   = attempt.number,
                    bulls    = attempt.bulls.toString(),
                    cows     = attempt.cows.toString(),
                    isHeader = false,
                    isWin    = attempt.isWin,
                    isReveal = attempt.isReveal
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun TableRow(
    number: String,
    bulls: String,
    cows: String,
    isHeader: Boolean,
    isWin: Boolean,
    isReveal: Boolean
) {
    val bg = when {
        isWin    -> WinRowBg
        isReveal -> RevealRowBg
        else     -> androidx.compose.ui.graphics.Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textStyle = if (isHeader)
            MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        else
            MaterialTheme.typography.bodyMedium

        Text(
            text      = number,
            style     = textStyle,
            modifier  = Modifier.weight(2f),
            textAlign = TextAlign.Start
        )
        Text(
            text      = bulls,
            style     = textStyle,
            color     = if (!isHeader) BullColor else MaterialTheme.colorScheme.onSurface,
            modifier  = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text      = cows,
            style     = textStyle,
            color     = if (!isHeader) CowColor else MaterialTheme.colorScheme.onSurface,
            modifier  = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
