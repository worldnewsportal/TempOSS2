# TempMail — disposable e-mail that tells the truth

A free, open-source Android app for temporary e-mail addresses: create one or several disposable addresses, receive real mail for up to **7 days**, switch or generate a new address any time — with **no cooldowns**, no account required, and native Arabic + English (RTL) support.

> **Honesty first (rule #60).** Every temporary-mail provider used by this app
> supports *receiving only*. Sending, replying and forwarding are therefore
> **disabled on purpose** — the app never pretends a feature exists when its real
> provider cannot do it.

---

## Features

- **Real providers, no fake mail.** Built on the public APIs of `1secmail.com` and
  `mail.tm`. Every message you see is real.
- **Multiple mailboxes at once.** Keep any number of active addresses; switch freely.
- **Change / generate instantly.** No cooldown, no timer, no "wait 3 minutes".
- **7-day lifetime.** Mailboxes expire 7 days after creation and are purged locally
  with their cached data (messages + attachments).
- **Gmail-style folders.** Inbox / Unread / Starred / labels (SPAM, TRASH, ARCHIVE, …),
  with real search where the provider supports it.
- **Themes.** System / Light / Dark / OLED, plus optional dynamic color.
- **Arabic & English.** UI strings fully localized with RTL support.
- **Real ads (opt-in).** Unity **LevelPlay** banners/interstitials/rewarded that you
  can disable in Settings. Rewards are granted **only** through the SDK completion
  callback — never on "watched"/"closed".
- **Privacy & trust.** Local caching, sanitized HTML, safe link handling, and
  Keystore-backed storage for provider auth tokens.

## Screens
Onboarding (with ad consent) · Mailbox manager · Generate/Change address ·
Inbox · Message reader (sanitized HTML) · Settings (theme/language/notifications/ads).

## Tech stack
- Kotlin + Jetpack Compose (Material 3), Navigation
- Room (local cache), DataStore (settings), WorkManager (periodic sync)
- Retrofit + OkHttp (+ SSE/WebSocket realtime for 1secmail)
- jsoup (HTML sanitizing)
- Unity Mediation SDK / LevelPlay 9.x for ads
- No DI framework; one small hand-rolled `AppContainer`

## Environment / versions (2026 baseline)
- Kotlin 2.3.21, AGP 9.1.1, Gradle 9.1, Compose BOM 2026.06.00
- compileSdk 36, targetSdk 36, minSdk 29
- JDK 17

## Building

The repo is ready to open in **Android Studio** (which will also generate the local
`gradlew` wrapper scripts/jar on first sync):

```bash
cp local.properties.example local.properties
# fill in your Unity/LevelPlay credentials only if you want real ads
./gradlew assembleDebug
```

To enable **real** (Release) ads you must supply real LevelPlay credentials:

```properties
unity.appKey=YOUR_APP_KEY
unity.banner.adUnitId=YOUR_BANNER_UNIT
unity.interstitial.adUnitId=YOUR_INTERSTITIAL_UNIT
unity.rewarded.adUnitId=YOUR_REWARDED_UNIT
```

Debug builds use the official Unity test/demo IDs (`demoBanner`,
`demoInterstitial`, `demoRewarded`, app key `25b63cf85`). If the app key / ad unit
IDs are missing or blank, the ad system **disables itself gracefully** — the e-mail
app keeps working with zero ads.

The real LevelPlay SDK binding lives in the single adapter file:

```
app/src/main/java/com/yourname/tempmail/ads/LevelPlaySession.kt
```

To finish wiring the actual SDK, implement the interface methods using the
officially named classes from
`com.unity3d.mediation:*` (MediationSdk etc.) for your installed
`mediation-sdk` version.

## Tests & CI
- JVM unit tests: `EmailAddress`, `Lifetime`, `UrlValidator`, `HtmlSanitizer`, `RateLimiter`, `ProviderCapabilities`, mappers.
- Instrumented tests: Room in-memory database, Compose smoke test.
- CI: `.github/workflows/build.yml` builds, lints and runs unit tests on pull requests.

## License
Apache-2.0 — see [LICENSE](LICENSE).

## Legal
- [PRIVACY.md](PRIVACY.md) · [TERMS.md](TERMS.md) ·
  [THIRD_PARTY_SERVICES.md](THIRD_PARTY_SERVICES.md) · [SECURITY](SECURITY.md) ·
  [CONTRIBUTING](CONTRIBUTING.md) · [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md).