# CoinCurrently

Android and widget app for following crypto markets, news, and a personal portfolio.

The Android app lives in `CoinCurrently/app`. Widgets reuse the same domain types and repositories where they need shared data.

## Why this is open source

CoinCurrently is focused on privacy. The source is public so anyone can see what the app does with their data.

## Architecture

The app uses **MVVM + repositories**.

- A **repository** is the source of truth for a slice of data (news, toplist, portfolio, and so on). It fetches from the network, caches, and persists. Screens do not talk to the API directly.
- A **view model** reads from one or more repositories, then shapes that data for a specific screen: filtering, sorting, loading flags, and user actions.
- A **view** (Compose screen) renders the view model and sends user events back. It should not own cache or networking.

```
View  →  ViewModel  →  Repository  →  MainRepository / StorageManager / SharedPreferences
              ↑               │
              └──── cached / mapped data
```

### Repositories

Repositories are Kotlin types, usually with a `companion object` that holds the shared cache (`NewsRepository`). Production code uses that shared instance. `MainRepository` is the Retrofit wrapper; feature repositories add caching and shared state on top.

A repository is responsible for:

- Loading and refreshing a domain (for example `getNewNews(count, category)`)
- Holding the cached copy of that data
- Writing user or session state that belongs to that domain (selected news category, saved coins)
- Talking to `MainRepository`, `StorageManager`, and disk cache — not to Compose

Several screens can share the same repository. Home, Market, and Coin Detail all read coin prices from the same source instead of each keeping their own list.

### View models

View models are AndroidX `ViewModel`s and typically expose Compose `mutableStateOf` properties. They:

- Ask repositories for data
- Keep screen-only state (selected tab, empty/loading)
- Map repository models into what the view needs (a short home carousel vs a full market list, filtered news, formatted P&L)
- Handle user actions, then write back through the repository when the change should be shared

They do not persist the app-wide cache. If Home and News both show news, the repository cache is what they share; each view model only keeps the slice its screen displays.

### Views

Compose screens take a view model (`viewModel()`, or a factory when the screen needs `Application`), observe it, and call `suspend` methods from `LaunchedEffect` / coroutines. Shared chrome (charts, list rows, sheets) lives in `sharedui` so features stay focused on their own flow.

### Example: News

1. `NewsRepository` fetches and caches `NewNewsModel` per `NewsCategory`.
2. `NewsViewModel` loads that cache, then filters it for Articles / Videos / Podcasts and the selected category.
3. `NewsScreen` binds to those lists and asks the view model to refresh when the user changes tab or filter.

## File structure

```
CoinCurrently/app/src/main/java/com/cryptium/coincurrently/
  app/                 Entry point, activity, navigation
  core/                Shared infrastructure (not feature UI)
    analytics/
    config/            Secrets loaded into BuildConfig (values stay in gitignored secrets.properties)
    extensions/
    networking/        Retrofit, MainRepository, response models
    notifications/
    persistence/       StorageManager, CurrencyRepository
    purchases/
    work/
  sharedui/            Reusable views, charts, modifiers, sheets
  features/            One folder per product area
    Home/
    Market/            Toplist, NFTs, Exchanges, Derivatives, Categories
    CoinDetail/
    Portfolio/
    News/
    …
  widgets/             Home screen widgets (Glance and AppWidgetProvider)

CoinCurrently/app/src/test/    Mirrors core/ and features/
Config/                        secrets.example.properties, google-services.json.example
```

A typical feature looks like:

```
features/News/
  NewsScreen.kt
  NewsViewModel.kt
  NewsRepository.kt
  Views/
```

Smaller features may keep a few files at the feature root. Shared types used by many features belong in `core` or `sharedui`, not in the first feature that happened to need them.

## Networking

Market data, news, and the fear and greed index each come from a different API. `MainRepository` and `ApiService` in `core/networking` own the requests; screens never call these APIs directly.

### CoinGecko

Crypto market data is based on [CoinGecko's API](https://www.coingecko.com/en/api). Most endpoints work on the free or Basic plans. These require the [Analyst plan](https://www.coingecko.com/en/api/pricing) (or higher):

- `/coins/list/new` — recently listed coins
- `/coins/top_gainers_losers` — top gainers and losers
- `/global/market_cap_chart` — historical global market cap and volume
- `/nfts/markets` — NFT collection market data
- `/nfts/{id}/market_chart` — NFT floor price, market cap, and volume history
- `/nfts/{id}/tickers` — NFT floor price and volume per marketplace
- CGSimplePrice WebSocket — live coin prices

### News

News (articles, videos, and podcasts) is based on [cryptonews-api.com](https://cryptonews-api.com).

### Fear and Greed

The fear and greed index is based on [alternative.me](https://alternative.me/crypto/fear-and-greed-index/).

## Targets

| Target | Role |
| --- | --- |
| `:app` | Android app (`CoinCurrently/app`), including widgets |
| `app/src/test` | JVM unit tests, mostly repositories |

Packages match folders under `com.cryptium.coincurrently`. Widgets compile in `:app`; they are not a separate Gradle module.

## Local setup

1. Open the `CoinCurrently/` Gradle project in Android Studio and sync.
2. Copy `Config/secrets.example.properties` to `Config/secrets.properties` and fill in local values. That file is gitignored.
3. Copy `Config/google-services.json.example` to `CoinCurrently/app/google-services.json` with your Firebase app config. That file is gitignored.
4. Do not commit real keys, signing material, or a live `google-services.json`.

See [LICENSE](LICENSE) for terms.
