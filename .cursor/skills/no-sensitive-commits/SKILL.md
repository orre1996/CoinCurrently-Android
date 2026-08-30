---
name: no-sensitive-commits
description: >-
  Blocks committing secrets or other sensitive data in this open-source repo.
  Use before every git add, git commit, git push, or pull request, and whenever
  staging files, reviewing diffs, or adding config/properties/key files.
---

# No Sensitive Commits

This repository is **open source**. Never commit, stage, or push secrets. If the user asks to commit sensitive data, refuse.

## Before every commit or push

1. Inspect `git status` and `git diff` (staged and unstaged).
2. Scan every added or modified file for secrets.
3. If anything sensitive is present, **stop**. Do not `git add` or `git commit` those files. Warn the user and list the files.

## Never commit these files

These are gitignored for a reason. Do not force-add them, and do not copy their real values into tracked files.

- `Config/secrets.properties`
- Any `google-services.json` (except `Config/google-services.json.example`)
- `local.properties`
- Signing material: `*.jks`, `*.keystore`, `*.pepk`
- `.env`, credentials JSON, private keys, keystores

Tracked templates are allowed: `Config/secrets.example.properties`, `Config/google-services.json.example`. Keep them as placeholders only.

## Never introduce secrets into tracked files

Real values must stay in local `Config/secrets.properties`. They are loaded into `BuildConfig` (`API_BASE_URL`, `BACKEND_API_KEY`, `REVENUECAT_*`, `APTABASE_APP_KEY`, `SCHEDULER_PRICE_ALERT_SECRET`).

Refuse to add live values for:

- Signing passwords, keystore paths, or Play Console / upload-key material in Gradle files
- Backend / CoinCurrently API keys
- RevenueCat, Aptabase, Firebase, Google, or other third-party keys
- Auth tokens, session cookies, passwords, private keys
- User data, emails, device tokens, or production endpoints that are not already public

Placeholder / example strings (as in the `*.example.*` files) are fine.

## If the user insists

Do not commit. Explain that this repo is public and that secrets belong in the gitignored local files, not git history.
