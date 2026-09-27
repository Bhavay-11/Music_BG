# Music_BG — native iOS app (SwiftUI)

A native SwiftUI port of the games. This is a **real iOS app project**, but it can only be built on
**macOS with Xcode** — there is no way to build or run an iOS app on Windows/Linux.

## What's here
- `project.yml` — [XcodeGen](https://github.com/yonatankarni/XcodeGen) spec (avoids committing a fragile `.xcodeproj`).
- `Sources/App.swift` — app entry, theme, games hub.
- `Sources/Games.swift` — SwiftUI games: Tic-Tac-Toe, Connect Four, Memory Match, Reaction Duel,
  Rock Paper Scissors, This or That. (More can be ported from the Android/`docs` versions.)

## Build & run (on a Mac)
```bash
# 1. Install tools (once)
brew install xcodegen

# 2. Generate the Xcode project
cd ios
xcodegen generate

# 3. Open it
open MusicBG.xcodeproj

# 4. In Xcode: pick a Simulator (or your iPhone) and press ⌘R.
```

Running on a **physical iPhone** additionally needs a free Apple ID signing team (Xcode →
target → Signing & Capabilities → select your team), and for sharing/TestFlight/App Store an
**Apple Developer Program** membership ($99/yr).

## Status / honesty
This scaffold was written on Windows and has **not been compiled** — Swift needs a Mac to build,
so expect to fix a few small issues in Xcode on first build. The **music/streaming** half is not
included here: NewPipe/Media3 are Android-only and YouTube scraping can't pass App Store review.
For music on iOS today, use the PWA at <https://bhavay-11.github.io/Music_BG/> (Music tab).
