<p align="center">
  <img alt="CoinCurrently. Privacy by default." src="docs/banner.jpg" width="900">
</p>

<p align="center">
  A private crypto tracker for iOS, Apple Watch, and Android.<br>
  Follow markets and a personal portfolio without an account, ads, or sign-up.
</p>

<p align="center">
  <a href="https://apps.apple.com/app/coincurrently/id1543974454"><img src="docs/app-store-badge.svg" alt="Download on the App Store" height="40"></a>
  &nbsp;&nbsp;&nbsp;
  <a href="https://play.google.com/store/apps/details?id=com.cryptium.coincurrently"><img src="docs/google-play-badge.png" alt="Get it on Google Play" height="40"></a>
  <br>
  <a href="https://github.com/orre1996/CryptoCurrently">iOS repo</a>
  &nbsp;·&nbsp;
  <a href="https://coincurrently.app">Website</a>
  <br><br>
  <a href="https://github.com/orre1996/CoinCurrently-Android/actions/workflows/tests.yml"><img alt="tests status" src="https://github.com/orre1996/CoinCurrently-Android/actions/workflows/tests.yml/badge.svg"></a>
</p>

<p align="center">
  <img alt="Start every day fully informed, read the market like a pro, and always know where you stand" src="docs/preview.jpg" width="900">
</p>

The Android app lives in `CoinCurrently/app`. Widgets reuse the same domain types and repositories where they need shared data.

## What it does

- Markets — coins, NFTs, exchanges, derivatives, and categories
- Personal portfolio
- Price alerts
- Home screen widgets

## Why this is open source

CoinCurrently is focused on privacy. The source is public so anyone can see what the app does with their data. The license is source-available, not OSI open source — see [License](#license).

## Privacy

CoinCurrently does not ask for an email or password. Portfolio holdings, transactions, and settings stay on the device.

The app uses a few third-party SDKs. None of them require a name, email, or phone number, so there is still no personal account.

- **[Aptabase](https://aptabase.com)** is a privacy-focused analytics service. The app sends named product events (for example which screen opened). It is not an advertising SDK and does not build a marketing profile.
- **[RevenueCat](https://www.revenuecat.com)** manages Google Play subscriptions. Pro status comes from the store receipt through RevenueCat, not from Firebase.
- **[Firebase](https://firebase.google.com)** starts only when `google-services.json` is included. It signs in anonymously — a random ID on the device, not an email. Price alerts need that so Firebase Cloud Messaging can issue an FCM token. The backend uses the token to send a push when a price hits; without it, the phone cannot receive alert notifications.

See the [Privacy Policy](https://coincurrently.app/privacy).

## Architecture

The app uses **MVVM + repositories**.

- A **repository** is the source of truth for a slice of data (toplist, portfolio, and so on). It fetches from the network, caches, and persists. Screens do not talk to the API directly.
- A **view model** reads from one or more repositories, then shapes that data for a specific screen: filtering, sorting, loading flags, and user actions.
- A **view** (Compose screen) renders the view model and sends user events back. It should not own cache or networking.

```
View  →  ViewModel  →  Repository  →  MainRepository / StorageManager / SharedPreferences
              ↑               │
              └──── cached / mapped data
```

### Repositories

Repositories are Kotlin types, usually with a `companion object` that holds the shared cache (`ToplistRepository`). Production code uses that shared instance. `MainRepository` is the Retrofit wrapper; feature repositories add caching and shared state on top.

A repository is responsible for:

- Loading and refreshing a domain (for example `getToplist(...)`)
- Holding the cached copy of that data
- Writing user or session state that belongs to that domain (saved coins)
- Talking to `MainRepository`, `StorageManager`, and disk cache — not to Compose

Several screens can share the same repository. Home, Market, and Coin Detail all read coin prices from the same source instead of each keeping their own list.

### View models

View models are AndroidX `ViewModel`s and typically expose Compose `mutableStateOf` properties. They:

- Ask repositories for data
- Keep screen-only state (selected tab, empty/loading)
- Map repository models into what the view needs (a short home carousel vs a full market list, formatted P&L)
- Handle user actions, then write back through the repository when the change should be shared

They do not persist the app-wide cache. Home and Market both read coin prices from the same repository; each view model only keeps the slice its screen displays.

### Views

Compose screens take a view model (`viewModel()`, or a factory when the screen needs `Application`), observe it, and call `suspend` methods from `LaunchedEffect` / coroutines. Shared chrome (charts, list rows, sheets) lives in `sharedui` so features stay focused on their own flow.

### Example: Home

1. `ToplistRepository` fetches and caches the coin toplist.
2. `HomeViewModel` keeps a short slice of that list for the selected carousel filter.
3. The Home screen binds to that slice and asks the view model to refresh when the user changes filter.

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
    …
  widgets/             Home screen widgets (Glance and AppWidgetProvider)

CoinCurrently/app/src/test/    Mirrors core/ and features/
Config/                        secrets.example.properties, google-services.json.example
```

A typical feature looks like:

```
features/Portfolio/
  PortfolioScreen.kt
  PortfolioViewModel.kt
  PortfolioRepository.kt
```

Smaller features may keep a few files at the feature root. Shared types used by many features belong in `core` or `sharedui`, not in the first feature that happened to need them.

## Networking

Market data and the fear and greed index each come from a different API. `MainRepository` and `ApiService` in `core/networking` own the requests; screens never call these APIs directly.

### CoinGecko

Crypto market data is based on [CoinGecko's API](https://www.coingecko.com/en/api). Without Pro, coin detail, OHLC, and market charts call the public API directly at `https://api.coingecko.com/api/v3` (`/coins/{id}`, `/coins/{id}/ohlc`, and `/coins/{id}/market_chart`). Those three calls do not use `API_BASE_URL`.

Every other market route is `{API_BASE_URL}/crypto/api/v3/...`. `API_BASE_URL` is your own backend. CoinGecko's public host does not serve the `/crypto` prefix, so `https://api.coingecko.com` cannot be used as `API_BASE_URL`.

Most of the proxied endpoints work on the free or Basic plans. These require the [Analyst plan](https://www.coingecko.com/en/api/pricing) (or higher):

- `/coins/list/new` — recently listed coins
- `/coins/top_gainers_losers` — top gainers and losers
- `/global/market_cap_chart` — historical global market cap and volume
- `/nfts/markets` — NFT collection market data
- `/nfts/{id}/market_chart` — NFT floor price, market cap, and volume history
- `/nfts/{id}/tickers` — NFT floor price and volume per marketplace
- CGSimplePrice WebSocket — live coin prices

### Fear and Greed

The fear and greed index is requested directly from [alternative.me](https://alternative.me/crypto/fear-and-greed-index/). It does not use `API_BASE_URL`.

### CoinCurrently backend

The backend is not in this repo. It proxies CoinGecko so third-party keys stay off the client, and it hosts first-party routes for price alerts, big-mover notifications, wallet import, gas fees, referrals, redeem codes, user, and feedback. Those first-party routes send a bearer token.

Without that backend, the app can still show coin detail, OHLC, market charts (for users without Pro), and the fear and greed index. Toplists, search, trending, exchanges, categories, derivatives, NFTs, gainers and losers, gas fees, and alerts need `API_BASE_URL`.

## Pro

Subscriptions go through RevenueCat and Google Play Billing. Free users get markets, a small portfolio, and widgets. Pro unlocks live prices (WebSocket), extra price alerts, more portfolio transactions, wallet import, and portfolio export.

## Targets

| Target | Role |
| --- | --- |
| `:app` | Android app (`CoinCurrently/app`), including widgets |
| `app/src/test` | JVM unit tests |

Packages match folders under `com.cryptium.coincurrently`. Widgets compile in `:app`; they are not a separate Gradle module.

## Requirements

- Android 12 (API 31)
- Android Studio with Android Gradle Plugin 8.11 or later

`CoinCurrently/app/gms-local-repo` redistributes a modified Apache-2.0 build of `play-services-basement`. The change and the upstream license are described in `CoinCurrently/app/gms-local-repo/NOTICE`.

## Local setup

1. Open the `CoinCurrently/` Gradle project in Android Studio and sync.
2. Copy `Config/secrets.example.properties` to `Config/secrets.properties`. That file is gitignored. If it is missing, the build uses the example placeholders, which are enough to launch. The build fails only if both files are missing. Keys:
   - `API_BASE_URL` — your backend, with no trailing slash. Markets and the first-party routes above need it. A placeholder lets the app launch. Coin detail, OHLC, market charts, and the fear and greed index can still load. Toplists, search, and alerts stay empty until it points at a backend.
   - `BACKEND_API_KEY` — sent as `x-coin-currently-key`. A placeholder is enough for the public CoinGecko and fear-and-greed calls.
   - `REVENUECAT_PRODUCTION_API_KEY` and `REVENUECAT_TEST_API_KEY` — placeholders are enough unless you are testing purchases. Debug builds use the test key.
   - `APTABASE_APP_KEY` — a placeholder is enough unless you want analytics.
   - `SCHEDULER_PRICE_ALERT_SECRET` — optional. Leave it empty if unused.
3. `CoinCurrently/app/google-services.json` is optional and gitignored. Without it, Firebase does not start. Copy `Config/google-services.json.example` there and replace the placeholders only for anonymous sign-in and price-alert push. The example placeholders are not a working Firebase config.
4. Do not commit real keys, signing material, or a live `google-services.json`.

## Tests

JVM unit tests live in `CoinCurrently/app/src/test`. Run `:app:testDebugUnitTest` from the `CoinCurrently/` Gradle project, or the unit-test target in Android Studio.

GitHub runs that same task on every push to `main` and on every pull request. The badge at the top of this page is the latest result on `main`: green is passing, red is failing. It stays blank until the workflow has run once on `main`.

## Contributing

Pull requests are welcome. Match the existing MVVM + repository layout. Do not commit secrets, signing material, or a live `google-services.json`.

## License

This repository is source-available under the [CoinCurrently Source-Available License](LICENSE). You may view, study, and use it for personal and educational purposes, and contributions are welcome. You may not republish it, ship a public fork, or publish a derived app to an app store.
