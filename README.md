# Minesweeper

A Java implementation of the classic Minesweeper game. Players can click cells to reveal them, flag potential mines, and attempt to clear the board without detonating any mines. This implementation supports different difficulty levels and features a fully interactive graphical interface.

---

## Features
- **Interactive Gameplay**: Use your mouse to reveal cells or flag potential mines.
- **Dynamic Difficulty**: Choose from easy, medium, or hard levels, each with varying grid sizes and mine counts.
- **Recursive Cell Reveal**: Automatically reveals adjacent cells when a revealed cell has no neighboring mines.
- **Win/Loss Conditions**:
  - Win: Reveal all non-mine cells.
  - Lose: Click on a mine.
- **Visual Feedback**:
  - Revealed cells display numbers indicating nearby mines.
  - Flags mark suspected mines.
- **Timer and Mine Counter**: Displays the elapsed time and remaining mines.

---

## Controls
   - **Left Click**: Reveal a cell.
   - **Right Click**: Flag or unflag a cell.
## Select a difficulty level to start:
   - **Easy**: 8x10 grid, 10 mines.
   - **Medium**: 16x18 grid, 40 mines.
   - **Hard**: 16x30 grid, 70 mines.

---

## Project Structure
```
Minesweeper/
├── Minesweeper.java        # Main game logic and graphical interface
├── Utils.java              # Utility functions for cell generation and grid setup
├── Cell.java               # Represents a single cell on the grid
└── ExamplesGame.java       # Includes test cases and demo functionality
```

---

## Technologies Used
- **Language**: Java
- **Libraries**:
  - **JavaLib**: Used for graphics and game interaction.
  - **Tester Library**: For unit testing.
- **Development Tools**: IntelliJ IDEA / Eclipse / VS Code.

---
