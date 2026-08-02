# Third-party services

This app talks to the following external services. Each has its own privacy
policy, terms, and retention behavior which you should review before use.

| Service | Used for | Data shared | Policy |
|---|---|---|---|
| 1secmail (`1secmail.com`) | Temporary e-mail reception (REST API + WebSocket) | The generated/disposable address you ask to create; message fetch requests for that address | See `https://www.1secmail.com/` |
| mail.tm (`api.mail.tm`, `mail.tm`) | Temporary e-mail reception (REST API with account JWT) | The address + auto-created throwaway credentials for the mailbox you choose | See `https://mail.tm/` |
| Unity Mediation SDK / LevelPlay (optional, only when ads enabled) | In-app advertisements (banner/interstitial/rewarded) | Device advertising identifiers processed by Unity; see `https://unity.com/legal/privacy-policy` | Unity Privacy Policy |

## Notes
- No other analytics, crash reporting, or ad networks are embedded.
- The ad SDK is bundled but **inert by default**: it only initializes when a
  developer supplies real LevelPlay credentials and the user opts in to ads. Ads
  never gate e-mail functionality.
- All requests to the mail providers are made directly from your device over
  HTTPS; the app runs no backend.

## Open-source dependencies
The standard OSS libraries used (OkHttp, Retrofit, Coroutines, Room/DataStore,
WorkManager, Compose/Material, Coil, jsoup, Gson) are governed by their own
open-source licenses, reproduced in the dependency metadata at build time.