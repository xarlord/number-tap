# Number Tap — Play Store & Monetization & Retention Blueprint

> Expert game design analysis addressing 4 critical gaps:
> 1. Play Store Deployment Readiness
> 2. Monetization Strategy
> 3. Google Play Quality Requirements
> 4. Cross-Session Retention & Reward Mechanism

---

## 1. PLAY STORE DEPLOYMENT PLAN

### 1.1 Pre-Launch Checklist

| Step | Item | Status | Target |
|------|------|--------|--------|
| 1 | Google Play Developer Account ($25 one-time) | 🔴 Not started | Week 1 |
| 2 | App signing key generation (Google Play App Signing) | 🔴 Not started | Week 1 |
| 3 | Store Listing: Title, Short/Full Description, Icons | 🔴 Not started | Week 2 |
| 4 | Screenshots: 4 minimum (phone), 8 recommended | 🔴 Not started | Week 2 |
| 5 | Feature Graphic (1024×500 banner) | 🔴 Not started | Week 2 |
| 6 | Content Rating questionnaire (IARC) | 🔴 Not started | Week 2 |
| 7 | Data Safety form (privacy declarations) | 🔴 Not started | Week 2 |
| 8 | Privacy Policy URL (required) | 🔴 Not started | Week 1 |
| 9 | Release APK (signed, minified, optimized) | 🔴 Not started | Week 3 |
| 10 | Internal Test Track (first upload) | 🔴 Not started | Week 3 |
| 11 | Closed/Open Beta Test Track | 🔴 Not started | Week 4 |
| 12 | Production Release | 🔴 Not started | Week 5 |

### 1.2 Store Listing Strategy

**Title:** `Number Tap - Speed Puzzle` (30 char max, keyword-rich)

**Short Description (80 chars):**
`Tap numbers in order before time runs out. Fast fingers, sharp eyes!`

**Full Description (4000 chars max — keyword-optimized):**
```
🔴 NUMBER TAP — THE ORDERED GRID 🔴

Race against the clock in this addictive speed puzzle! Find and tap numbers
in ascending order on a chaotic grid. Simple to learn, impossible to master.

⚡ GAMEPLAY
• Tap numbers 1, 2, 3... in order as fast as you can
• Every correct tap adds time — every wrong tap costs you
• Grid grows harder as your score climbs
• Build combos for bonus effects

🎨 4 VISUAL THEMES
• Terminal — classic green-on-black CRT
• Chalkboard — classroom nostalgia
• Matrix — digital rain
• Default — sleek dark minimalist

🏆 DAILY CHALLENGES & REWARDS
• Come back daily for bonus coins and power-ups
• Complete missions for unlock rewards
• Climb the difficulty tiers: Easy → Medium → Hard → Extreme

🎵 IMMERSIVE AUDIO
• Dynamic background music
• Combo pitch stepping
• Countdown urgency sounds

📊 STATS & PROGRESSION
• Track accuracy, average speed, best combos
• Persistent high scores
• Share scores with friends

Perfect for fans of: brain games, speed puzzles, number games,
focus trainers, casual hyper-casual games, math puzzles.

Download now and test your reflexes!
```

### 1.3 Asset Requirements

| Asset | Size | Format | Notes |
|-------|------|--------|-------|
| App Icon | 512×512 | PNG-32 | Yellow-on-dark, number grid motif |
| Feature Graphic | 1024×500 | PNG/JPG | Hero shot of gameplay + "NUMBER TAP" |
| Phone Screenshots | 16:9 or 9:16 | PNG | Min 4, show: Menu, Gameplay, Game Over, Theme Select |
| Tablet Screenshots | 16:10 | PNG | Optional but recommended for 7" & 10" |

### 1.4 Release Pipeline

```
Local Build (debug) → Local Test (emulator)
        ↓
Release Build (signed) → Proguard/R8 minified
        ↓
Internal Test Track → 2-3 testers via email
        ↓
Closed Beta → 10-50 testers (Google Groups)
        ↓
Open Beta → Anyone with link
        ↓
Production → Public launch
        ↓
Staged Rollout → 10% → 25% → 50% → 100%
```

### 1.5 App Signing & Build Config

```kotlin
// app/build.gradle.kts additions needed:
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

Generate upload key:
```bash
keytool -genkey -v -keystore numbertap-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

---

## 2. MONETIZATION STRATEGY

### 2.1 Revenue Model Overview

For a hyper-casual game in 2025, the industry-standard monetization mix is:

| Channel | % of Revenue | Implementation Effort |
|---------|-------------|----------------------|
| Interstitial Ads (AdMob) | 50% | Low |
| Rewarded Video Ads | 30% | Medium |
| Banner Ads | 10% | Low |
| In-App Purchases (IAP) | 10% | High |

**Target eCPM:** $2-8 (hyper-casual avg), **Target ARPU:** $0.01-0.05/user/day

### 2.2 Ad Integration Plan

#### A. Banner Ad (Bottom — Always Visible Except During Gameplay)

```
┌──────────────────────────────────┐
│         GAME CONTENT             │
│                                  │
│         [Grid / Menu]            │
│                                  │
├──────────────────────────────────┤
│     🟨🟨🟨 BANNER AD 🟨🟨🟨    │  ← AdMob Banner (320x50 dp)
└──────────────────────────────────┘
```

- **Placement:** Bottom of Menu Screen + Game Over Screen only
- **NEVER during active gameplay** — violates Google Play policy and hurts UX
- **AdMob Unit:** `AdView` with `AdSize.BANNER`
- **Refresh rate:** 60 seconds (default)

#### B. Interstitial Ads (Between Sessions)

- **Trigger:** Every 3rd "Game Over" → before showing GameOver screen
- **Frequency cap:** Max 1 per 3 minutes real time
- **Loading:** Pre-load after each game start, show when ready
- **AdMob Unit:** `InterstitialAd`

```
Game Over (1st, 2nd) → Show GameOver Screen
Game Over (3rd) → Show Interstitial → Then GameOver Screen
```

#### C. Rewarded Video Ads (Player-Initiated)

These are the highest-earning ads because players CHOOSE to watch them.

| Reward | Trigger | Value |
|--------|---------|-------|
| **Revive** | Game Over + score ≥ 90% of high score | +5 seconds |
| **Double Coins** | Game Over screen | 2× coin earnings |
| **Free Power-Up** | Menu screen, once per day | Random power-up |
| **Skip Tier** | In-game when difficulty jumps | Stay at current tier |

- **AdMob Unit:** `RewardedAd`
- **Ad Network:** AdMob mediation → Meta Audience Network → Unity Ads
- **Duration:** 15-30 second videos
- **Reward delivery:** Only after video completes (onUserEarnedReward callback)

### 2.3 In-App Purchases (Optional — Phase 2)

| SKU | Price | Content |
|-----|-------|---------|
| `remove_ads` | $1.99 | Remove all ads permanently |
| `coin_pack_small` | $0.99 | 500 coins |
| `coin_pack_large` | $1.99 | 2000 coins (bonus 200) |
| `theme_pack_premium` | $2.99 | Unlock Neon + Blueprint + Pixel themes |

### 2.4 AdMob Integration Architecture

```kotlin
// New files needed:
// ad/AdManager.kt — Singleton, manages all ad lifecycle
// ad/AdConstants.kt — Ad unit IDs (test vs production)

class AdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var gameOverCount = 0

    companion object {
        // Test ad unit IDs (replace with real ones for production)
        const val BANNER_UNIT = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_UNIT = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_UNIT = "ca-app-pub-3940256099942544/5224354917"
    }

    fun shouldShowInterstitial(): Boolean {
        gameOverCount++
        return gameOverCount % 3 == 0
    }

    fun loadRewardedAd(onLoaded: (RewardedAd) -> Unit) { ... }
    fun loadInterstitial(onLoaded: (InterstitialAd) -> Unit) { ... }
}
```

---

## 3. GOOGLE PLAY QUALITY REQUIREMENTS

### 3.1 Technical Requirements Checklist

| # | Requirement | Status | Fix |
|---|------------|--------|-----|
| 1 | Target API level 33+ (Android 13) | ✅ targetSdk 35 | Done |
| 2 | No `REQUEST_INSTALL_PACKAGES` permission | ✅ Clean | Done |
| 3 | Proper backup rules (`android:fullBackupContent`) | 🔴 Missing | Add XML |
| 4 | Network security config (if network access) | ⚠️ Not needed | No network calls yet |
| 5 | Scoped storage compliance | ✅ No file access | Done |
| 6 | Doze mode / background execution limits | ✅ No background work | Done |
| 7 | WebView policy (no hidden iframes/ads) | ✅ No WebView | Done |
| 8 | Ads properly labeled ("Ad" / "Sponsored") | 🔴 N/A yet | Required when ads added |
| 9 | GDPR/CCAA consent for ads | 🔴 Missing | Need CMP SDK |
| 10 | No hard crashes on any supported device | ⚠️ Need testing | Test matrix needed |

### 3.2 Play Policy Compliance (2024-2025)

#### A. Spam & Minimum Functionality
- ✅ Original game, not a clone
- ✅ Fully functional, not a placeholder
- ✅ Meaningful content (not just a wrapper for ads)

#### B. Deceptive Behavior
- ✅ App title matches content
- ✅ No impersonation of other apps
- ⚠️ Screenshots must match actual gameplay
- 🔴 Need accurate metadata description

#### C. Ad Policy Compliance (Critical)
- ❌ Ads must not interfere with app navigation
- ❌ No unexpected ads (must be predictable)
- ❌ No ads on lock screen
- ❌ Rewarded ads must deliver stated reward
- ❌ Interstitials must have close button visible after 5 seconds
- ❌ Banner ads must not overlap interactive elements

#### D. Data Safety Section (Required)
Currently we collect NO user data:
```
Data collected: None
Data shared: None
Data encrypted: N/A
Data deletion: N/A
```
When Firebase Analytics is added:
```
Data collected: App activity (analytics), Device info
Data shared: None (first-party only)
Committed to Play Families policy: Yes
```

### 3.3 Store Listing Quality

| Requirement | Criteria | Status |
|------------|----------|--------|
| Icon | No transparency, no text, recognizable at small sizes | 🔴 Need proper icon |
| Screenshots | 4-8 phone, landscape/portrait consistent | 🔴 Need captures |
| Description | Keywords, features, no keyword stuffing | 🔴 Need to write |
| Privacy Policy | Accessible URL, accurate | 🔴 Need to host |
| Content Rating | Complete IARC questionnaire | 🔴 Need to complete |

### 3.4 Crash-Free & ANR Targets

| Metric | Target | How to Achieve |
|--------|--------|----------------|
| Crash-free rate | ≥ 99.5% | Firebase Crashlytics |
| ANR rate | ≤ 0.5% | Keep main thread clean |
| Battery drain | Minimal | No background services |
| Cold start | < 2 seconds | Lazy initialization |

### 3.5 Testing Matrix

| Dimension | Minimum Coverage |
|-----------|-----------------|
| API levels | 24 (Android 7.0) through 35 (Android 15) |
| Screen sizes | Small phone (320dp) → Large tablet (720dp) |
| Orientations | Portrait only (locked) |
| Languages | English (launch), add more later |
| Memory | Test on 2GB RAM devices |

### 3.6 Required Code Changes for Compliance

```xml
<!-- AndroidManifest.xml additions -->
<application
    android:fullBackupContent="@xml/backup_rules"
    android:allowBackup="true">

<!-- res/xml/backup_rules.xml (NEW) -->
<full-backup-content>
    <exclude domain="sharedpref" path="."/>
</full-backup-content>
```

```xml
<!-- Lock to portrait -->
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:configChanges="orientation|screenSize|screenLayout" />
```

---

## 4. CROSS-SESSION RETENTION & REWARD MECHANISM

### 4.1 The Problem

Current state: Players open the game, play once, close it. No reason to come back.
- D1 retention target: 40% (hyper-casual avg: 30-35%)
- D7 retention target: 15% (hyper-casual avg: 8-12%)
- D30 retention target: 5% (hyper-casual avg: 2-3%)

### 4.2 Proposed Solution: The "Tap Streak" System

A multi-layered reward system that ties directly to game difficulty:

```
┌─────────────────────────────────────────────────────────┐
│                    REWARD LAYERS                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Layer 1: DAILY LOGIN STREAK                            │
│  ┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐    │
│  │ Day ││ Day ││ Day ││ Day ││ Day ││ Day ││ Day │    │
│  │  1  ││  2  ││  3  ││  4  ││  5  ││  6  ││  7  │    │
│  │ 10¢ ││ 15¢ ││ 20¢ ││ 25¢ ││ 30¢ ││ 40¢ ││100¢ │    │
│  └─────┘└─────┘└─────┘└─────┘└─────┘└─────┘└─────┘    │
│                                          ║              │
│                                    BONUS: Free          │
│                                    Power-Up             │
│                                                         │
│  Layer 2: DAILY MISSIONS (3 per day)                    │
│  ┌──────────────────────────────────────────────┐       │
│  │ ☐ Score 25+ in a single game          (20¢)  │       │
│  │ ☐ Reach x5 combo streak               (15¢)  │       │
│  │ ☐ Play 5 games today                  (10¢)  │       │
│  └──────────────────────────────────────────────┘       │
│                                                         │
│  Layer 3: ACHIEVEMENTS (permanent)                      │
│  ┌──────────────────────────────────────────────┐       │
│  │ 🔓 First Game          → "Baby Steps"  (5¢)  │       │
│  │ 🔓 Score 50            → "Speed Demon" (50¢)  │       │
│  │ 🔓 x10 Combo           → "Unstoppable"(100¢) │       │
│  │ 🔓 Play 7 days in a row→ "Dedicated"  (200¢) │       │
│  │ 🔓 All 4 themes used   → "Stylist"    (50¢)  │       │
│  │ 🔓 Score 100           → "Legend"     (500¢) │       │
│  └──────────────────────────────────────────────┘       │
│                                                         │
│  Layer 4: DIFFICULTY-LOCKED REWARDS                     │
│  ┌──────────────────────────────────────────────┐       │
│  │ Score 15 → Unlock "Slow Motion" power-up     │       │
│  │ Score 30 → Unlock "Highlight" power-up       │       │
│  │ Score 50 → Unlock "Extra Time +3s" power-up  │       │
│  │ Score 75 → Unlock "Peek" power-up            │       │
│  │ Score 100→ Unlock "Neon" theme               │       │
│  └──────────────────────────────────────────────┘       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 4.3 Currency System: Coins

**Why coins, not just unlocks?**
- Gives players a reason to accumulate
- Enables IAP purchase option
- Adds visual progress indicator

**Earning Rate:**

| Action | Coins Earned |
|--------|-------------|
| Correct tap | 1¢ |
| x5 combo | +5¢ bonus |
| x10 combo | +15¢ bonus |
| Daily login (Day 1) | 10¢ |
| Daily login (Day 7) | 100¢ |
| Complete all 3 daily missions | 50¢ bonus |
| New high score | 25¢ bonus |
| Achievement unlock | 5-500¢ |

**Spending:**

| Item | Cost | Effect |
|------|------|--------|
| Slow Motion (3s) | 50¢ | Timer runs at 0.5× speed for 3 seconds |
| Highlight | 30¢ | Target tile glows for 5 seconds |
| Extra Time +3s | 40¢ | Adds 3 seconds to clock |
| Peek | 60¢ | Shows path of next 3 numbers briefly |
| Skip Ad | 20¢ | Skips next interstitial |

### 4.4 Power-Up System (Tied to Difficulty)

Power-ups are **earned** through gameplay milestones and **purchased** with coins:

```kotlin
enum class PowerUp(val unlockScore: Int, val cost: Int, val durationMs: Long) {
    SLOW_MOTION(unlockScore = 15, cost = 50, durationMs = 3000),
    HIGHLIGHT(unlockScore = 30, cost = 30, durationMs = 5000),
    EXTRA_TIME(unlockScore = 50, cost = 40, durationMs = 0),
    PEEK(unlockScore = 75, cost = 60, durationMs = 2000);

    val isUnlocked: Boolean get() = /* check persisted high score */
}
```

**Game Balance:**
- Power-ups can only be activated BEFORE a game starts (menu screen)
- Each power-up can be used once per game
- Costs scale with difficulty tier:
  - Easy (0-15): base cost
  - Medium (16-40): cost × 1.5
  - Hard (41+): cost × 2.0

### 4.5 Daily Mission Generator

```kotlin
object DailyMissionGenerator {

    fun generateDailyMissions(highScore: Int): List<Mission> {
        val pool = mutableListOf<Mission>()

        // Score-based missions (scale with player level)
        pool.add(Mission("Score ${minOf(25, highScore + 5)} in a single game", 20))
        pool.add(Mission("Score ${minOf(15, highScore)} or higher", 15))
        pool.add(Mission("Beat your high score", 50))

        // Combo missions
        pool.add(Mission("Reach x5 combo", 15))
        pool.add(Mission("Reach x3 combo 3 times", 20))

        // Volume missions
        pool.add(Mission("Play 5 games", 10))
        pool.add(Mission("Play 10 games", 20))
        pool.add(Mission("Play 3 games without mistakes", 30))

        // Accuracy missions
        pool.add(Mission("Achieve 90%+ accuracy in a game", 25))
        pool.add(Mission("Play a game with 0 wrong taps", 40))

        // Select 3 random missions, seeded by date for consistency
        val seed = LocalDate.now().toEpochDay()
        return pool.shuffled(Random(seed)).take(3)
    }
}
```

### 4.6 Data Persistence

All reward state is persisted locally via SharedPreferences + DataStore:

```kotlin
data class PlayerProfile(
    val coins: Int = 0,
    val totalGamesPlayed: Int = 0,
    val highScore: Int = 0,
    val currentLoginStreak: Int = 0,
    val lastLoginDate: String = "",       // "2025-06-06"
    val longestLoginStreak: Int = 0,
    val unlockedPowerUps: Set<PowerUp> = emptySet(),
    val unlockedAchievements: Set<String> = emptySet(),
    val dailyMissions: List<MissionState> = emptyList(),
    val dailyMissionsDate: String = "",   // regenerate daily
    val gamesPlayedToday: Int = 0,
    val totalCoinsEarned: Int = 0
)
```

### 4.7 Notification Strategy (Optional — Phase 2)

Using `WorkManager` + `NotificationCompat`:

| Trigger | Notification | Timing |
|---------|-------------|--------|
| Login streak at risk | "Your 6-day streak is about to break! 🔥" | If not opened by 8pm local |
| Daily missions available | "New daily missions ready! Complete them for 45 coins" | 9am local |
| New achievement close | "Only 5 more points to unlock 'Speed Demon'!" | After game over if close |
| Weekly challenge | "This week's challenge: Score 100!" | Monday 9am |

### 4.8 Expected Impact on Retention

| Mechanic | D1 Impact | D7 Impact | D30 Impact |
|----------|-----------|-----------|------------|
| Daily login streak | +8-12% | +5-8% | +2-3% |
| Daily missions | +5-8% | +3-5% | +1-2% |
| Power-ups tied to score | +3-5% | +4-6% | +3-4% |
| Achievement system | +2-3% | +2-3% | +2-3% |
| Coin economy | +2-3% | +3-4% | +2-3% |
| **Combined** | **+20-31%** | **+17-26%** | **+10-15%** |

**Target retention with all systems:**
- D1: 50-55% (vs 30% baseline)
- D7: 22-28% (vs 10% baseline)
- D30: 10-13% (vs 3% baseline)

---

## 5. IMPLEMENTATION PRIORITY

### Phase 1: Play Store Minimum (Week 1-2)
- [ ] Backup rules XML
- [ ] Portrait lock in manifest
- [ ] ProGuard/R8 configuration
- [ ] Signed release build
- [ ] Store listing assets (icon, screenshots, descriptions)
- [ ] Privacy policy page (GitHub Pages or similar)
- [ ] Internal test track upload

### Phase 2: Ad Integration (Week 2-3)
- [ ] AdMob SDK integration
- [ ] Banner ads (menu + game over only)
- [ ] Interstitial ads (every 3rd game over)
- [ ] Rewarded video (revive + double coins)
- [ ] GDPR consent (UMP SDK)
- [ ] "Remove Ads" IAP

### Phase 3: Retention System (Week 3-5)
- [ ] PlayerProfile data model + persistence
- [ ] Coin currency system
- [ ] Daily login streak tracking
- [ ] Daily mission generator
- [ ] Achievement system
- [ ] Power-up system (earned + purchased)
- [ ] Menu screen: missions panel, coin display, power-up selector

### Phase 4: Analytics & Polish (Week 5-6)
- [ ] Firebase Analytics (all game events)
- [ ] Firebase Crashlytics
- [ ] A/B testing (ad frequency, reward amounts)
- [ ] Remote Config (tune coin economy without app update)
- [ ] Notification system (streaks, missions)
- [ ] Final Play Store review submission

---

## 6. NEW FILES NEEDED

```
app/src/main/java/com/xarlord/numbertap/
├── ad/
│   ├── AdManager.kt           — AdMob lifecycle + frequency capping
│   └── AdConstants.kt         — Test/production unit IDs
├── data/
│   ├── PlayerProfile.kt       — Persistent player state
│   ├── PowerUp.kt             — Power-up enum + unlock logic
│   ├── Mission.kt             — Daily mission model
│   └── Achievement.kt         — Achievement definitions
├── game/
│   ├── RewardEngine.kt        — Coin calculation + streak logic
│   ├── MissionGenerator.kt    — Daily mission selection
│   └── AchievementManager.kt  — Achievement unlock detection
├── ui/
│   ├── MissionPanel.kt        — Daily mission display
│   ├── PowerUpSelector.kt     — Pre-game power-up selection
│   ├── CoinDisplay.kt         — Coin balance widget
│   └── AchievementPopup.kt    — Achievement unlock overlay
├── persistence/
│   └── ProfileManager.kt      — DataStore/SharedPreferences layer
└── notification/
    └── NotificationScheduler.kt — WorkManager daily notifications

app/src/main/res/xml/
├── backup_rules.xml           — Backup exclusion rules
└── network_security_config.xml — (if needed for ads)

app/src/main/assets/
└── privacy_policy.html        — Offline privacy policy
```
