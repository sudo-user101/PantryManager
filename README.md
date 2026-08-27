# Pantry Basic

A small Java Android app for practicing the same core idea as Smart Pantry Manager, but kept
deliberately simple: track pantry ingredients (name, quantity, unit) using SQLite and basic
CRUD.

This is a personal practice project - stock Material widgets, no custom design work, no
recipe matching yet. The point is understanding the fundamentals (SQLiteOpenHelper, a
RecyclerView + Adapter, Activities talking to each other through Intents) without any of the
polish layered on top.

## What's here right now

- Pantry list screen (RecyclerView + custom Adapter), backed by SQLite
- Add/Edit screen shared for Create and Update, with basic input validation
- Delete with a confirmation dialog
- Data genuinely persists (SQLite file on disk, verified across force-stop/relaunch)

## Running it

Open the project folder in Android Studio, let Gradle sync, run on an emulator or device
(API 24+).

```
./gradlew assembleDebug
```
