# Task Bloom

Task Bloom is a student planner and to-do list app for Android, available on [Google Play](https://play.google.com/store/apps/details?id=com.smartfamily.studentplanner).

This repository contains two parts:

| Part | Location | What it is |
|------|----------|------------|
| Android app | `app/`, Gradle files | Java app (min SDK 26), Room for local storage, Firebase Auth / Analytics / Messaging, Google Maps |
| Landing site | `index.html`, `privacy.html`, `terms.html`, `style.css`, `script.js`, `logo.svg` | Static marketing site with Privacy Policy and Terms of Service |

## Android app

1. Open the project in Android Studio.
2. Add your `local.properties` with the Maps API key used by the secrets Gradle plugin.
3. Run the `app` configuration.

Release bundles (`.aab`) are built locally and uploaded to Play Console. They are not committed to git.

## Landing site

The site is plain HTML/CSS/JS with no build step. Open `index.html` in a browser, or serve the folder:

```bash
python3 -m http.server 8000
```

It can be hosted as-is on GitHub Pages, Firebase Hosting or any static host.

The Privacy Policy URL (`/privacy.html`) is the one to put in Play Console → App content → Privacy policy.
