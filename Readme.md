# Trady

A modern Android stock-tracking app built with Jetpack Compose, Hilt, Retrofit, Room, and MPAndroidChart.
Fetches market data (top gainers/losers, time series, company overview) from **Alpha Vantage** and uses **Finnhub** for company logos.

---

## Features

* **Home / Explore**

  * Top Gainers & Top Losers (grid of 2×2 cards)
  * Tap “View All” to see full lists
  * Tap a ticker to view detailed stock page

* **View All**

  * Full scrolling list of gainers or losers
  * Rectangular rows with logo, name, symbol, price & % change

* **Details Screen**

  * Company overview (name, symbol, type, exchange, description, sector & industry)
  * 1D/1W/1M/6M interactive line chart with custom MPAndroidChart marker
  * Key metrics (market cap, P/E ratio, beta, dividend yield, profit margin, 52-week high/low)

* **Watchlists**

  * Create / delete multiple named watchlists
  * Add/remove symbols to any watchlist
  * View watchlists in a grid of cards
  * Tappable rows to navigate to detail screen

* **Offline Storage**

  * Room database for persisting watchlists & items

---

## APK

You can find the signed release APK at:

```
app/release/app-release.apk
```

---

## Tech Stack

* **UI:** Jetpack Compose, Material 3
* **DI:** Hilt
* **Networking:** Retrofit + Moshi, OkHttp logging
* **Local DB:** Room (Room KTX)
* **Charting:** MPAndroidChart (LineChart)
* **Images:** Coil Compose

---

## APIs Used

1. **Alpha Vantage** (Free tier – 25 requests/day)

   * **TOP\_GAINERS\_LOSERS**: `/query?function=TOP_GAINERS_LOSERS&apikey=...`
   * **TIME\_SERIES\_DAILY**: `/query?function=TIME_SERIES_DAILY&symbol={symbol}&apikey=...`
   * **OVERVIEW**: `/query?function=OVERVIEW&symbol={symbol}&apikey=...`
   * **SYMBOL\_SEARCH**: `/query?function=SYMBOL_SEARCH&keywords={keywords}&apikey=...`

2. **Finnhub** (logos & profile)

   * **Company Profile**: `GET /api/v1/stock/profile2?symbol={symbol}&token={apikey}`

---

## Project Structure

```
app/src/main/java/com/example/trady/
├── MainActivity.kt          // NavHost + bottom bar + navigation setup
├── MyApplication.kt         // Hilt application entry point
├── data/
│   ├── network/
│   │   ├── AlphaVantageApi.kt
│   │   ├── FinnhubApi.kt
│   │   └── models/           // Response data classes
│   │       ├── CompanyOverviewResponse.kt
│   │       ├── SymbolSearchResponse.kt
│   │       ├── TimeSeriesDailyResponse.kt
│   │       ├── TopGainersLosersResponse.kt
│   │       └── TickerInfo.kt
│   ├── repository/
│   │   ├── StockRepository.kt
│   │   └── StockRepositoryImpl.kt
│   ├── util/
│   │   └── Resource.kt       // Loading / Success / Error wrapper
│   └── watchlist/            // Room entities & DAOs & repo
│       ├── AppDatabase.kt
│       ├── Watchlist.kt
│       ├── WatchlistItem.kt
│       ├── WatchlistDao.kt
│       ├── WatchlistItemDao.kt
│       ├── WatchlistRepository.kt
│       └── WatchlistRepositoryImpl.kt
├── di/
│   ├── NetworkModule.kt      // Retrofit / Moshi / OkHttp providers
│   ├── RepositoryModule.kt   // Binds StockRepositoryImpl
│   ├── DatabaseModule.kt     // Provides Room database & DAOs
│   └── WatchlistModule.kt    // Provides WatchlistRepository
├── ui/
│   ├── navigation/
│   │   └── Screen.kt         // Sealed routes + labels + icons
│   ├── screens/              // All @Composable screens
│   │   ├── ExploreScreen.kt
│   │   ├── ViewAllScreen.kt
│   │   ├── ProductScreen.kt
│   │   ├── WatchlistScreen.kt
│   │   └── PriceMarker.kt    // MPAndroidChart MarkerView
│   └── theme/                // Compose theming & ViewModels
│       ├── Color.kt
│       ├── Theme.kt
│       ├── Type.kt
│       └── viewmodel/
│           ├── ExploreViewModel.kt
│           ├── ProductViewModel.kt
│           └── WatchlistViewModel.kt
```

---

## How to Build

1. Clone the repo
2. In `app/build.gradle`, set your Alpha Vantage & Finnhub keys:

   ```gradle
   buildConfigField "String", "ALPHA_VANTAGE_API_KEY", "\"YOUR_ALPHA_KEY\""
   buildConfigField "String", "FINNHUB_API_KEY",       "\"YOUR_FINNHUB_KEY\""
   ```
3. `./gradlew assembleRelease`
4. Install the generated `app-release.apk` from `app/release/`

---
