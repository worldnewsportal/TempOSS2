# Privacy Policy

This app ("TempMail") is designed so that your temporary mail activity remains
yours. Below is exactly what the app stores, sends, and never does.

## What the app collects on-device
- **Mailboxes** you create (address, provider, creation/expiry time).
- **Cached messages and attachments** — they are stored locally so the app works
  offline and newsletters remain readable.
- **Settings** (language, theme, notification preferences, optional ad consent).
- **Provider tokens** (e.g. the mail.tm JWT) stored encrypted with the Android
  Keystore — never in plain settings or in git.

## Data lifetime (built into the app)
- Every mailbox expires **7 days** after creation. After expiry the app stops
  syncing it and **deletes the mailbox and its cached messages/attachments** from
  the device.
- Deleting a mailbox deletes its cached data immediately.

## What is sent over the network
- The address you chose, and message fetches, go directly to the temporary-mail
  provider you picked (`1secmail.com` or `mail.tm`). No intermediate server, no
  app-owned backend.
- If you enable ads, the Unity LevelPlay SDK may collect device identifiers for
  ad serving per its own privacy policy (see THIRD_PARTY_SERVICES.md).
- We do not run analytics on this app.

## Things we never do
- We never read spurious unrelated mail; we only fetch mail for addresses **you**
  created in this app.
- We never sell or share cached messages; your cached mail leaves the device only
  through the provider whose API originally delivered it.
- The app does **not** support sending mail (the providers do not), so nothing you
  type is ever transmitted as mail.

## Your controls
- Delete any mailbox (and optionally all mailboxes) from Settings — cached data
  is removed immediately.
- Ads can be disabled in Settings at any time.

Reporting vulnerabilities: see SECURITY.md.
Questions: file an issue at the repository of this app.