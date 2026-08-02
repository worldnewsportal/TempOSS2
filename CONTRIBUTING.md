# Contributing

Thanks for helping. Keep these rules in mind so the app stays honest and safe.

## Code of conduct
By contributing you agree to [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Getting started
1. Open the repo in Android Studio (latest stable) and let the wrapper sync.
2. Create a branch: `git checkout -b feature/yourthing`.
3. Keep one logical change per PR; add tests for behavior changes.

## Non-negotiable product rules
- **No fake mail.**: never generate mock messages or pretend a delivery happened.
- **No fake ads/rewards.**: a reward may only be granted from the real
  `RewardedListener#onRewarded` callback — never on "watched" / "closed" / time.
- **No fake sending.**: if a provider can't send/reply/forward, the UI must not
  expose those actions as if available. Change `ProviderCapabilities` with care.
- **Honest capabilities**: whenever you touch `ProviderCapabilities`, keep
  `supportsReceiving`/`supportsSending` etc. truthful for the real API.
- **Sanitization**: any new renderer for message HTML must go through
  `HtmlSanitizer` and links through `UrlValidator`.

## Conventions
- Package layout is feature-flavored (`ui/…`, `data/…`, `providers/…`).
- No DI framework: extend `AppContainer`.
- Tests: pure logic gets a JVM unit test; DB/UI gets an instrumented test.
- Run before opening a PR:
  ```bash
  ./gradlew testDebugUnitTest lintDebug assembleDebug
  ```

## Commit messages
Short, imperative, prefixed by scope, e.g.:
`providers: avoid hammering mail.tm on repeated sync`