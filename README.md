# CoinCurrently

Android and widget app for following crypto markets, news, and a personal portfolio.

- [Website](https://coincurrently.app)
- [Google Play](https://play.google.com/store/apps/details?id=com.cryptium.coincurrently)
- [Privacy Policy](https://coincurrently.app/privacy)
- [iOS](https://github.com/orre1996/CryptoCurrently)

The Android app lives in `CoinCurrently/app`. Widgets reuse the same domain types and repositories where they need shared data.

## What it does

- Markets — coins, NFTs, exchanges, derivatives, and categories
- Personal portfolio
- Price alerts
- News (articles, videos, and podcasts)
- Home screen widgets

## Why this is open source

CoinCurrently is focused on privacy. The source is public so anyone can see what the app does with their data. The license is source-available, not OSI open source — see [License](#license).

## Privacy

CoinCurrently does not ask for an email or password. Portfolio holdings, transactions, and settings stay on the device.

The app uses a few third-party SDKs. None of them require a name, email, or phone number, so there is still no personal account.

- **[Aptabase](https://aptabase.com)** is a privacy-focused analytics service. The app sends named product events (for example which screen opened). It is not an advertising SDK and does not build a marketing profile.
- **[RevenueCat](https://www.revenuecat.com)** manages Google Play subscriptions. Pro status comes from the store receipt through RevenueCat, not from Firebase.
- **[Firebase](https://firebase.google.com)** signs in anonymously — a random ID on the device, not an email. Price alerts need that so Firebase Cloud Messaging can issue an FCM token. The backend uses the token to send a push when a price hits; without it, the phone cannot receive alert notifications.

See the [Privacy Policy](https://coincurrently.app/privacy).

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

### CoinCurrently backend

`API_BASE_URL` is the app's own API. It proxies CoinGecko and news so third-party keys stay off the client, and it hosts first-party routes for price alerts, big-mover notifications, wallet import, gas fees, referrals, redeem codes, user, and feedback. Those routes send a bearer token. The backend itself is not in this repo.

## Pro

Subscriptions go through RevenueCat and Google Play Billing. Free users get markets, news, a small portfolio, and widgets. Pro unlocks live prices (WebSocket), extra price alerts, more portfolio transactions, wallet import, portfolio export, and some news filters (coin, exchange, NFT, technical analysis).

## Targets

| Target | Role |
| --- | --- |
| `:app` | Android app (`CoinCurrently/app`), including widgets |
| `app/src/test` | JVM unit tests |

Packages match folders under `com.cryptium.coincurrently`. Widgets compile in `:app`; they are not a separate Gradle module.

## Requirements

- Android 12 (API 31)
- Android Studio with Android Gradle Plugin 8.11 or later

## Local setup

1. Open the `CoinCurrently/` Gradle project in Android Studio and sync.
2. Copy `Config/secrets.example.properties` to `Config/secrets.properties` and fill in local values. That file is gitignored. Keys are `API_BASE_URL`, `BACKEND_API_KEY`, `REVENUECAT_PRODUCTION_API_KEY`, `REVENUECAT_TEST_API_KEY`, and `APTABASE_APP_KEY`.
3. Copy `Config/google-services.json.example` to `CoinCurrently/app/google-services.json` with your Firebase app config. That file is gitignored.
4. Do not commit real keys, signing material, or a live `google-services.json`.

## Tests

JVM unit tests live in `CoinCurrently/app/src/test`. Run `:app:testDebugUnitTest` from the `CoinCurrently/` Gradle project, or the unit-test target in Android Studio.

## Contributing

Pull requests are welcome. Match the existing MVVM + repository layout. Do not commit secrets, signing material, or a live `google-services.json`.

## License

This repository is source-available under the [CoinCurrently Source-Available License](LICENSE). You may view, study, and use it for personal and educational purposes, and contributions are welcome. You may not republish it, ship a public fork, or publish a derived app to an app store.
