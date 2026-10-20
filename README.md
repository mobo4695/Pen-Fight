# Pen Fight

A small Android game: the school-desk pen fight. Flick your pen, knock the
enemy pen off the table, first to 5 points wins.

Kotlin + Jetpack Compose. No permissions, no internet, no accounts — it works
fully offline. Built in phases; Mobinul tests each phase.

## Status

- **Phase 1 (this code):** project skeleton — a wooden table and two pens
  (blue = you at the bottom, red = enemy at the top) at their start spots.
- Phase 2: drag-back flick aiming and sliding.
- Phase 3: bumping, spinning, falling off, points.
- Phase 4: two players on one phone + winner screen.
- Phase 5: computer opponent.
- Later: pen garage (types, upgrades, coins) and 3–6 player party mode.

## How to run it (Windows)

1. Copy `gradle-wrapper.jar` from Pocket Toolkit into this project:
   from `PocketToolkit2\gradle\wrapper\gradle-wrapper.jar`
   to `Pen-Fight\gradle\wrapper\gradle-wrapper.jar`
   (GitHub pushes from the assistant cannot carry that one binary file.)
2. Open the `Pen-Fight` folder in Android Studio.
3. Press Run.

Package: `com.mobin.penfight`
