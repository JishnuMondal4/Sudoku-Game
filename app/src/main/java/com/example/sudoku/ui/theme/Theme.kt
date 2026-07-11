package com.example.sudoku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SudokuPrimary,
    secondary = SudokuSecondary,
    background = SudokuBackground,
    surface = SudokuSurface
)

@Composable
fun SudokuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
