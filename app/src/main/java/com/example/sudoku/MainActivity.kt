package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

// ======================= MAIN ACTIVITY =======================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SudokuApp()
                }
            }
        }
    }
}

// ======================= DIFFICULTY =======================

enum class Difficulty(val label: String, val cluesToKeep: Int) {
    EASY("Easy", 42),
    MEDIUM("Medium", 34),
    HARD("Hard", 27)
}

// ======================= SUDOKU ENGINE =======================

object SudokuEngine {

    // Generates a fully solved 9x9 board using randomized backtracking
    fun generateSolvedBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        val emptyCell = findEmptyCell(board) ?: return true
        val (row, col) = emptyCell
        val numbers = (1..9).toMutableList()
        numbers.shuffle(Random(System.nanoTime()))
        for (num in numbers) {
            if (isValidPlacement(board, row, col, num)) {
                board[row][col] = num
                if (fillBoard(board)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    private fun findEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (r in 0..8) {
            for (c in 0..8) {
                if (board[r][c] == 0) return Pair(r, c)
            }
        }
        return null
    }

    fun isValidPlacement(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0..8) {
            if (board[row][i] == num) return false
            if (board[i][col] == num) return false
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }
        return true
    }

    // Creates a puzzle by removing cells from a solved board, keeping `cluesToKeep` filled
    fun generatePuzzle(solved: Array<IntArray>, cluesToKeep: Int): Array<IntArray> {
        val puzzle = Array(9) { r -> solved[r].copyOf() }
        val cellsToRemove = 81 - cluesToKeep
        val positions = (0..80).toMutableList()
        positions.shuffle(Random(System.nanoTime()))

        var removed = 0
        var index = 0
        while (removed < cellsToRemove && index < positions.size) {
            val pos = positions[index]
            val row = pos / 9
            val col = pos % 9
            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0
                removed++
            }
            index++
        }
        return puzzle
    }

    fun copyBoard(board: Array<IntArray>): Array<IntArray> = Array(9) { r -> board[r].copyOf() }
}

// ======================= GAME STATE =======================

class SudokuState {
    var solution by mutableStateOf(SudokuEngine.generateSolvedBoard())
    var puzzle by mutableStateOf(arrayOf<IntArray>())
    var fixedCells by mutableStateOf(setOf<Pair<Int, Int>>())
    var userBoard by mutableStateOf(arrayOf<IntArray>())
    var selectedCell by mutableStateOf<Pair<Int, Int>?>(null)
    var mistakes by mutableIntStateOf(0)
    var secondsElapsed by mutableIntStateOf(0)
    var isRunning by mutableStateOf(true)
    var isComplete by mutableStateOf(false)
    var difficulty by mutableStateOf(Difficulty.MEDIUM)

    fun newGame(diff: Difficulty = difficulty) {
        difficulty = diff
        solution = SudokuEngine.generateSolvedBoard()
        puzzle = SudokuEngine.generatePuzzle(solution, diff.cluesToKeep)
        userBoard = SudokuEngine.copyBoard(puzzle)
        fixedCells = buildSet {
            for (r in 0..8) for (c in 0..8) if (puzzle[r][c] != 0) add(Pair(r, c))
        }
        selectedCell = null
        mistakes = 0
        secondsElapsed = 0
        isRunning = true
        isComplete = false
    }

    fun selectCell(row: Int, col: Int) {
        if (!isComplete) selectedCell = Pair(row, col)
    }

    fun inputNumber(num: Int) {
        val cell = selectedCell ?: return
        val (row, col) = cell
        if (fixedCells.contains(cell) || isComplete) return

        val newBoard = SudokuEngine.copyBoard(userBoard)
        newBoard[row][col] = num
        userBoard = newBoard

        if (num != 0 && num != solution[row][col]) {
            mistakes++
        }

        checkCompletion()
    }

    fun eraseCell() {
        inputNumber(0)
    }

    private fun checkCompletion() {
        for (r in 0..8) {
            for (c in 0..8) {
                if (userBoard[r][c] != solution[r][c]) return
            }
        }
        isComplete = true
        isRunning = false
    }
}

// ======================= UI =======================

@Composable
fun SudokuApp() {
    val state = remember { SudokuState() }

    LaunchedEffect(Unit) {
        if (state.puzzle.isEmpty()) state.newGame()
    }

    LaunchedEffect(state.isRunning, state.isComplete) {
        while (state.isRunning && !state.isComplete) {
            delay(1000L)
            state.secondsElapsed++
        }
    }

    if (state.puzzle.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sudoku",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )

        StatsRow(state)

        Spacer(modifier = Modifier.height(16.dp))

        SudokuGrid(state)

        Spacer(modifier = Modifier.height(20.dp))

        NumberPad(state)

        Spacer(modifier = Modifier.height(16.dp))

        DifficultyRow(state)

        if (state.isComplete) {
            Spacer(modifier = Modifier.height(16.dp))
            WinBanner(state)
        }
    }
}

@Composable
fun StatsRow(state: SudokuState) {
    val minutes = state.secondsElapsed / 60
    val seconds = state.secondsElapsed % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatChip(label = "Time", value = timeText)
        StatChip(label = "Mistakes", value = "${state.mistakes}")
        StatChip(label = "Level", value = state.difficulty.label)
    }
}

@Composable
fun StatChip(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFDDE3EE), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SudokuGrid(state: SudokuState) {
    val selected = state.selectedCell
    val selectedValue = selected?.let { (r, c) -> state.userBoard[r][c] }

    Column(
        modifier = Modifier
            .border(2.dp, Color(0xFF3B5998))
            .background(Color.White)
    ) {
        for (row in 0..8) {
            Row {
                for (col in 0..8) {
                    val isFixed = state.fixedCells.contains(Pair(row, col))
                    val value = state.userBoard[row][col]
                    val isSelected = selected == Pair(row, col)
                    val isPeer = selected != null && !isSelected && (
                            selected.first == row ||
                                    selected.second == col ||
                                    (selected.first / 3 == row / 3 && selected.second / 3 == col / 3)
                            )
                    val isSameNumber = selectedValue != null && selectedValue != 0 &&
                            value == selectedValue && !isSelected
                    val isWrong = !isFixed && value != 0 && value != state.solution[row][col]

                    val bgColor = when {
                        isSelected -> Color(0xFFAFC6F0)
                        isSameNumber -> Color(0xFFD8E4FA)
                        isPeer -> Color(0xFFEDF1FB)
                        else -> Color.White
                    }
                    val textColor = when {
                        isWrong -> Color(0xFFD32F2F)
                        isFixed -> Color(0xFF1A1A1A)
                        else -> Color(0xFF3B5998)
                    }

                    val thickColor = Color(0xFF3B5998)
                    val thinColor = Color(0xFFBFC9DC)

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(bgColor)
                            .drawBehind {
                                val thin = 1.dp.toPx()
                                val thick = 3.dp.toPx()
                                val w = size.width
                                val h = size.height

                                // thin grid lines on all sides
                                drawLine(thinColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(w, 0f), thin)
                                drawLine(thinColor, androidx.compose.ui.geometry.Offset(0f, h), androidx.compose.ui.geometry.Offset(w, h), thin)
                                drawLine(thinColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, h), thin)
                                drawLine(thinColor, androidx.compose.ui.geometry.Offset(w, 0f), androidx.compose.ui.geometry.Offset(w, h), thin)

                                // thick lines on 3x3 box boundaries
                                if (row % 3 == 0) drawLine(thickColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(w, 0f), thick)
                                if (row == 8) drawLine(thickColor, androidx.compose.ui.geometry.Offset(0f, h), androidx.compose.ui.geometry.Offset(w, h), thick)
                                if (col % 3 == 0) drawLine(thickColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, h), thick)
                                if (col == 8) drawLine(thickColor, androidx.compose.ui.geometry.Offset(w, 0f), androidx.compose.ui.geometry.Offset(w, h), thick)
                            }
                            .clickable { state.selectCell(row, col) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (value != 0) {
                            Text(
                                text = value.toString(),
                                fontSize = 18.sp,
                                fontWeight = if (isFixed) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad(state: SudokuState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            for (num in 1..5) {
                NumberButton(num) { state.inputNumber(num) }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row {
            for (num in 6..9) {
                NumberButton(num) { state.inputNumber(num) }
                Spacer(modifier = Modifier.width(6.dp))
            }
            EraseButton { state.eraseCell() }
        }
    }
}

@Composable
fun NumberButton(num: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = num.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EraseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color(0xFFB0B0B0), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "⌫", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DifficultyRow(state: SudokuState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Difficulty.entries.forEach { diff ->
            val selected = state.difficulty == diff
            Button(
                onClick = { state.newGame(diff) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE0E5F0),
                    contentColor = if (selected) Color.White else Color(0xFF3B5998)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(diff.label)
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = { state.newGame() },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B5998)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("New Game", color = Color.White)
    }
}

@Composable
fun WinBanner(state: SudokuState) {
    val minutes = state.secondsElapsed / 60
    val seconds = state.secondsElapsed % 60
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFDFF5E1), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("🎉 Solved!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        Text(
            text = "Time: ${String.format("%02d:%02d", minutes, seconds)} • Mistakes: ${state.mistakes}",
            fontSize = 14.sp,
            color = Color(0xFF2E7D32)
        )
    }
}
