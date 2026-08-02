# Security

Security is treated seriously in this project. Below is what we guarantee by
design, and how to report an issue.

## Security design
- **Trusted keystore storage**: provider auth tokens (mail.tm JWT) and any
  locally stored credentials are kept in the Android Keystore-backed
  `SecretStore` (AES/GCM in hardware-backed keystore) — never in plaintext files
  or SharedPreferences data store.
- **Sanitized e-mail HTML**: every remote e-mail `text/html` is cleaned with
  jsoup before rendering. Scripts, event handlers and dangerous schemes are
  stripped so untrusted mail can never execute in the WebView.
- **Safe link handling**: links open through `UrlValidator` (http/https/mailto/
  tel only) and are resolved externally via ACTION_VIEW — never inside the app's
  browsing context.
- **Data minimization on expiry**: expired mailboxes purge their local cached
  messages/attachments immediately.
- **Ads are optional & isolated**: the LevelPlay session abstraction means ad
  failures can never crash or block mail; if not configured, ads are inert.

## Reporting a vulnerability
Please do **not** file a public issue for active security bugs.

- Report by opening a **private security advisory** on the repository, or
  email the maintainers (see the repo's documentation for the active address).
- Describe the affected file, the impact, and a step-by-step reproduction.
- We aim to acknowledge reports within 5 business days and to close critical
  issues with a public fix. Allow ~90 days for coordinated disclosure before
  posting publicly.

## Safe providers
- The app only talks to HTTPS endpoints for the temporary-mail providers.
- Never send real PII or credentials into a temporary mailbox; it is disposable
  and may be lost at any time.