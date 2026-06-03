# Game Design Document: Number Tap (The Ordered Grid)

**Genre:** Hyper-Casual / Speed Puzzle  
**Platform:** Mobile (Portrait) / Web  
**Visual Style:** Flat 2D, Minimalist, High-Contrast  

---

## 1. Game Overview & Core Loop

*Number Tap* is a frantic, high-speed puzzle game where the player battles a ticking clock to find and tap numbers in ascending order on a chaotic, static grid. The game relies entirely on cognitive scanning speed and instant tactile feedback rather than complex physics or animations.

### The Core Game Loop

```
 ┌────────────────────────────────────────┐
 │            Spawn Fresh Grid            │
 └───────────────────┬────────────────────┘
                     ▼
 ┌────────────────────────────────────────┐
 │    Player Scans & Taps Next Number     │
 └───────────────────┬────────────────────┘
                     ▼
          Is Tapped Number Correct?
         ├── Yes ──► Add Time + Advance Number State / Clear Board
         └── No  ──► Deduct Time + Screen Flash Red
                     │
                     ▼
           Has Timer Reached Zero?
         ├── Yes ──► Game Over (Show Score & High Score)
         └── No  ──► Loop Continues
```

---

## 2. Core Mechanics & Rules

### 2.1 The Board State (Data Structure)

The game board is represented by a flat 2D array of Tile objects.

- **Grid Dimensions:** Initialized at 4×4 (16 tiles total). Scales up to 5×5 (25 tiles) at higher difficulties.
- **Tile Properties:**

```json
{
  "id": 1,
  "currentValue": 1,
  "state": "ACTIVE",
  "color": "COLOR_NORMAL"
}
```

### 2.2 Gameplay Rules

1. **The Target Tracker:** The game tracks a global variable: `targetNumber = 1`.
2. **The Input Validation:** When a player taps a tile at coordinates `(row, col)`:
   - **If `tile.currentValue == targetNumber`:**
     - It is a **Success**.
     - `targetNumber` increments by 1.
     - Trigger *Success Feedback* (Section 4).
     - **Tile Replacement (Continuous Mode):** The tapped tile's value instantly changes to `tile.currentValue + GridSize` (e.g., on a 4×4 board, tapping `1` instantly turns that exact tile into `17`). This keeps the grid fully populated and chaotic.
   - **If `tile.currentValue != targetNumber`:**
     - It is a **Failure**.
     - Trigger *Failure Feedback* (Section 4).
     - Time penalty is deducted from the countdown.

### 2.3 Win / Lose Conditions

- **Win Condition (Level-Based Mode):** Clear all numbers up to the level max (e.g., 1 to 50).
- **Lose Condition:** The countdown timer hits `0`.

---

## 3. UI Layout & Visual Architecture

Because the game lacks 3D elements and smooth sliding, the UI layout must be perfectly clean and readable at a glance.

```
┌────────────────────────────────────────┐
│  [ SCORE: 0024 ]      [ TIME: 14.8s ]  │  ← Top Stats Bar (High Contrast)
├────────────────────────────────────────┤
│                                        │
│         TARGET: [ 5 ]                  │  ← Next Number Hint (Huge Font)
│                                        │
├────────────────────────────────────────┤
│ ┌──────────┐┌──────────┐┌──────────┐   │
│ │    14    ││     3    ││    22    │   │
│ └──────────┘└──────────┘└──────────┘   │
│ ┌──────────┐┌──────────┐┌──────────┐   │
│ │     7    ││  [ 5 ]   ││    11    │   │  ← The Grid Container
│ └──────────┘└──────────┘└──────────┘   │
│ ┌──────────┐┌──────────┐┌──────────┐   │
│ │    19    ││    16    ││     2    │   │
│ └──────────┘└──────────┘└──────────┘   │
│                                        │
└────────────────────────────────────────┘
```

### Visual Palette (High-Contrast Minimalist)

| Element | Color | Hex |
|---------|-------|-----|
| Background | Deep Dark Slate | `#121824` |
| Standard Tile | Muted Gray-Blue | `#2A3447` (Bold White Text) |
| Target Hint | Vibrant Electric Yellow | `#FACC15` |
| Success Flash | Bright Mint Green | `#22C55E` |
| Failure Flash | Intense Crimson Red | `#EF4444` |

---

## 4. Game "Juice" (Instant State Feedback)

Without rich animations, the game relies on frame-perfect color interpolation, screenshake, and audio cues to feel satisfying.

### 4.1 Frame-Based Color Fading (3-Frame Fake Smoothness)

When a tile is correctly tapped, do not use an animation library. Instead, step its background color programmatically across consecutive engine ticks:

| Frame | Success State Color | Failure State Color |
|:------|:-------------------|:-------------------|
| **Frame 1 (Impact)** | `#22C55E` (Pure Green) | `#EF4444` (Pure Red) |
| **Frame 2 (Fade)** | `#1E5E3A` (Muted Green) | `#6B2121` (Muted Red) |
| **Frame 3 (Settle)** | `#2A3447` (Back to Normal) | `#2A3447` (Back to Normal) |

### 4.2 Camera Screenshake (Failure Penalty)

When a mistake occurs, apply a random pixel offset between `-6px` and `+6px` to the main grid container's X and Y positioning array for exactly **60 milliseconds** (approx. 4 frames at 60Hz). This creates an instant, jarring penalty sensation.

### 4.3 Audio Engineering Map

Sound carries the behavioral weight of the UI.

- **Success Tap:** A short, high-frequency "ping" or "pop" (800Hz – 1200Hz).
- **Combo Streak:** Each consecutive correct tap within 0.5 seconds increases the pitch of the success tap by a half-step, resetting when the streak breaks.
- **Failure Tap:** A low-frequency, blunt "thud" (150Hz).

---

## 5. Technical Architecture & Progression

### 5.1 Progression & Difficulty Scaling

| Score Range | Grid Size | Max Value Spawned | Time Gain / Correct Tap | Wrong Tap Penalty |
|:------------|:----------|:------------------|:------------------------|:------------------|
| **0 – 15** | 4×4 | 16 | +1.0 second | −1.5 seconds |
| **16 – 40** | 4×4 | 32 (Continuous) | +0.7 seconds | −2.0 seconds |
| **41+** | 5×5 | 50 (Continuous) | +0.5 seconds | −3.0 seconds |

### 5.2 Board Randomization Algorithm (Fisher-Yates)

1. Initialize a 1D array of size `GridSize` populated sequentially with numbers `1` to `N`.
2. Run a standard **Fisher-Yates shuffle** on the 1D array.
3. Map the shuffled values directly into the 2D grid display container.

---

## 6. Monetization & Retention Mechanics

- **Ad Integration:** A static banner ad at the bottom of the screen (completely clear of the gameplay grid to prevent accidental taps). An interstitial ad triggers strictly on every 3rd "Game Over" screen.
- **The Rewarded Revive:** If the timer hits zero but the player is within 10% of beating their personal High Score, offer a single "Watch an Ad to get +5 Seconds" button to save the run.
