# 🔢 Number Tap (The Ordered Grid)

**Hyper-Casual Speed Puzzle** — Android

Tap numbers in ascending order against a ticking clock. Fast scanning, instant feedback, pure reflexes.

## Gameplay

- Numbers spawn on a grid in random order
- Tap them in ascending sequence (1 → 2 → 3 → …)
- Correct taps add time, wrong taps deduct time
- Grid expands as difficulty scales (4×4 → 5×5)
- Chase your high score before the clock hits zero

## Tech Stack

- **Platform:** Android (API 24+)
- **Language:** Kotlin
- **UI:** Jetpack Compose (flat 2D, no physics engine)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35

## Project Structure

```
app/
├── src/main/java/com/xarlord/numbertap/
│   ├── data/          # Game state models, difficulty config
│   ├── game/          # Core game logic (grid, timer, scoring)
│   ├── ui/            # Compose screens (game, menu, game over)
│   ├── audio/         # Sound pool (ping, thud, combo)
│   └── MainActivity.kt
├── src/main/res/      # Resources (colors, strings, themes)
docs/
├── GDD.md             # Game Design Document
```

## Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Documentation

- [Game Design Document (GDD)](docs/GDD.md)

## License

MIT
