# CoinCurrently

Android app for following crypto markets, news, and a personal portfolio.

The Android app lives in `CoinCurrently/app`. Home-screen widgets live in the same module and reuse the same domain types and repositories where they need shared data. Kotlin packages match folders under `com.cryptium.coincurrently`.

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

Repositories are Kotlin types, often with a `companion object` that holds the shared cache (`NewsRepository`, `ToplistRepository`). `MainRepository` is a thin Retrofit wrapper. Feature repositories sit on top of it and add caching and shared state. Live prices that many screens need live in `CurrencyRepository`.

A repository is responsible for:

- Loading and refreshing a domain (for example `getNewNews(count, category)`)
- Holding the cached copy of that data
- Writing user or session state that belongs to that domain (selected news category, saved coins)
- Talking to `MainRepository` / `ApiHelper`, `StorageManager`, and disk cache — not to Compose

Several screens can share the same repository. Home, Market, and Coin Detail all read coin prices from the same source instead of each keeping their own list.

### View models

View models are AndroidX `ViewModel`s. They typically expose Compose `mutableStateOf` properties. They:

- Ask repositories for data
- Keep screen-only state (selected tab, empty/loading, whether a sheet is open)
- Map repository models into what the view needs (a short home carousel vs a full market list, filtered news, formatted P&L)
- Handle user actions, then write back through the repository when the change should be shared

They do not persist the app-wide cache. If Home and News both show news, the repository cache is what they share; each view model only keeps the slice its screen displays.

### Views

Compose screens take a view model (`viewModel()`, or a factory when the screen needs `Application`), observe it, and call `suspend` methods from `LaunchedEffect` / coroutines. Shared chrome (charts, list rows, sheets) lives in `sharedui` so features stay focused on their own flow.

`MainActivity` hosts Compose Navigation (`app/NavigationEntries.kt`, `app/NavigationScreens.kt`). Feature screens are registered there; they are implemented under `features/`.

### Example: News

1. `NewsRepository` fetches and caches `NewNewsModel` per `NewsCategory`.
2. `NewsViewModel` loads that cache, then filters it for Articles / Videos / Podcasts and the selected sentiment.
3. `NewsScreen` binds to those lists and asks the view model to refresh when the user changes tab or filter.

## File structure

Kotlin sources live under `CoinCurrently/app/src/main/java/com/cryptium/coincurrently/`:

```
com.cryptium.coincurrently/
  CoinCurrentlyApplication.kt   Process start: Coil, Firebase, RevenueCat
  app/                          Activity, nav graph, nav argument types
  core/                         Shared infrastructure (not feature UI)
    analytics/
    config/                     Locale, Remote Config keys
    extensions/
    networking/                 Retrofit, MainRepository, response models
    notifications/
    persistence/                StorageManager, CurrencyRepository
    purchases/
    work/                       WorkManager jobs
  sharedui/                     Reusable views, charts, modifiers, sheets
  features/                     One folder per product area
    Home/
    Market/                     Toplist, NFTs, Exchanges, Derivatives, Categories
    CoinDetail/
    Portfolio/
    News/
    …
  widgets/                      Glance and legacy AppWidget providers
    glance/
    receivers/
    views/
```

A typical feature looks like:

```
features/News/
  NewsScreen.kt
  NewsViewModel.kt
  NewsRepository.kt
```

Larger features add `Views/`, `ViewModels/`, `Repositories/`, or product slices (`Home/Trending`, `Market/Toplist`). Smaller features may keep a few files at the feature root. Shared types used by many features belong in `core` or `sharedui`, not in the first feature that happened to need them.

In-app widget *settings* are `features/Widgets/`. Home-screen widget *providers* are `widgets/`.

`Config/` (repo root) holds `secrets.example.properties` and `google-services.json.example`. Values stay in the gitignored live files. Android resources (layouts, drawables, strings) stay in the Gradle `res` tree, not next to Kotlin.

## Targets

| Target | Role |
| --- | --- |
| `:app` | Android app, Compose UI, and widgets |
| `app/src/test` | JVM unit tests (repositories, analytics contracts, helpers) |

There is a single Gradle module. Glance and `AppWidgetProvider` classes compile in `:app`; they are not a separate module.

## Local setup

1. Open the `CoinCurrently/` Gradle project in Android Studio and sync.
2. Copy `Config/secrets.example.properties` to `Config/secrets.properties` and fill in local values. That file is gitignored.
3. Copy `Config/google-services.json.example` to `CoinCurrently/app/google-services.json` with your Firebase app config. That file is gitignored.
4. Do not commit real keys, signing material, or a live `google-services.json`.

See [LICENSE](LICENSE) for terms.
