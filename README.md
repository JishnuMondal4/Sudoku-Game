# Sudoku

A native Android Sudoku puzzle game built with **Kotlin** and **Jetpack Compose**. Every game generates a brand-new, uniquely solvable 9×9 board using a randomized backtracking algorithm — no two puzzles are ever the same.

## Features

- 🧩 **Infinite puzzle generation** — a custom backtracking algorithm builds a fully solved board and carves out a puzzle from it on every new game
- 🎚️ **Three difficulty levels** — Easy, Medium, and Hard, controlling how many starting clues are visible
- 🎯 **Smart cell highlighting** — selecting a cell highlights its row, column, 3×3 box, and every other cell sharing the same number
- ❌ **Live mistake tracking** — incorrect entries are flagged in red and counted in real time
- ⏱️ **Built-in timer** — tracks how long each puzzle takes to solve
- 🏆 **Auto win-detection** — a completion banner shows your final time and mistake count
- 🎨 **Hand-drawn grid** — bold 3×3 box borders and thin cell gridlines rendered directly on canvas for a crisp, authentic Sudoku look

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI Toolkit | Jetpack Compose |
| Design System | Material 3 |
| State Management | Compose State (`mutableStateOf`, `remember`) |
| Concurrency | Kotlin Coroutines (game timer) |
| Rendering | Canvas / `drawBehind` (custom grid borders) |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | 24 (Android 7.0+) |
| Target/Compile SDK | 34 (Android 14) |


## Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) Hedgehog or newer
- JDK 8+

### Setup
1. Clone the repository
   ```bash
   git clone https://github.com/JishnuMondal4/Sudoku-Game.git
   ```
2. Open the project folder in Android Studio
3. Let Gradle sync
4. Run the app on an emulator or physical device

## How to Play

1. Tap **New Game** or pick a difficulty (Easy / Medium / Hard)
2. Tap any empty cell to select it
3. Tap a number (1–9) on the number pad to fill it in
4. Use the erase button (⌫) to clear a cell
5. Fill the entire grid correctly to win — your time and mistake count are shown at the end

## Project Structure

```
app/src/main/java/com/example/sudoku/
├── MainActivity.kt          # Entry point, game engine, state, and UI
└── ui/theme/
    ├── Color.kt              # App color palette
    ├── Theme.kt               # Material 3 theme setup
    └── Type.kt                # Typography
```

## Author

**Jishnu Mondal**
[GitHub](https://github.com/JishnuMondal4) · [LinkedIn](https://www.linkedin.com/in/jishnu-mondal-143947329/)

## License

This project is open source and available under the [MIT License](LICENSE).
