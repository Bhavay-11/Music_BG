# Enabling online duet games (Firebase)

Duetify's online games work through Firebase Firestore. The app builds and runs **without** any of
this — local/bot games and Shields all work — but rooms only sync across two separate phones once
you add your own Firebase project. Nothing here costs money on Firebase's free (Spark) tier for
normal use.

## One-time setup (~10 minutes)

1. **Create a project** at <https://console.firebase.google.com> → *Add project*.
2. **Register the Android app**: in the project, *Add app → Android*.
   - Package name: `com.duetify.app` (must match exactly).
   - Download the generated **`google-services.json`**.
3. **Drop the file in**: put `google-services.json` into the `app/` folder
   (next to `app/build.gradle.kts`). The build auto-detects it and turns Firebase on.
4. **Enable Anonymous auth**: Console → *Authentication → Sign-in method → Anonymous → Enable*.
   (Each phone signs in silently; no login screen.)
5. **Create Firestore**: Console → *Firestore Database → Create database* → start in *production* mode.
6. **Publish the rules**: copy the contents of [`firestore.rules`](firestore.rules) into
   Console → *Firestore → Rules → Publish*.

## Verify

- Rebuild in Android Studio. On the **Play → This or That** lobby, "Create a room" now says
  *"play live with your partner anywhere."*
- Install on two phones (or a phone + emulator). One taps **Create a room** and reads out the
  4-letter code; the other taps **Join** and types it. Answers should sync live.

## Notes

- `google-services.json` is git-ignored by default in most Android setups — keep it out of any public
  repo. It isn't a secret per se, but there's no reason to publish it.
- Analytics stays **off**: only Firestore + Auth are used, and `app-measurement.com` is on the Shields
  blocklist, so Duetify remains a no-tracking app even with Firebase enabled.
