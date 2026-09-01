# Pantry Manager

A Java Android app that keeps track of the ingredients you already have at home and tells
you which recipes you can actually make right now - not recipes that need three more things
from the shop.

## About the App

You add what's in your pantry (name, quantity, unit, optional expiry date, and a food emoji
icon), and the app checks that against a built-in collection of recipes. Only recipes where
you have everything, in the amount needed, get suggested as "Ready to make" - anything missing
exactly one ingredient shows up separately under "Almost there".

This project was built incrementally, one feature at a time, as a practice rebuild of an
earlier, more academic pantry-tracking app of mine (Smart Pantry Manager) - re-implementing
the same core ideas from scratch, screen by screen, rather than copying the project wholesale.
A few things here (Tutorial Mode, the "?" help tips) are original to this app and don't exist
in that earlier project.

## Main Features

- Add, edit and delete pantry ingredients (name, quantity, unit, optional expiry date)
- Pantry list with search-as-you-type (by name) and swipe-to-delete
- 19 recipes seeded into the database on first launch, each with its own ingredients and method
- Suggested Recipes screen, split into "Ready to make" (strict match) and "Almost there"
  (missing exactly one ingredient)
- Recipe Detail screen showing every ingredient marked as available or missing, plus the
  numbered method steps
- Strict quantity and unit checking, not just "do I have this ingredient"
- A food emoji icon per pantry item and per recipe, with a picker so you can change it
- Optional expiry dates, with "expiring soon" / "expired" indicators on the pantry list
- Settings screen: Dark Mode, expiring-soon alerts toggle, preferred unit system
  (Metric/Imperial), Tutorial Mode, and a "reset sample recipes" option
- Tutorial Mode - loads a small set of clearly-flagged demo pantry items and shows a "?" tip
  button on every screen, without ever touching your real data
- Input validation on the add/edit form (empty name, empty/invalid/zero quantity)
- Data is saved to SQLite, so it's still there after you close and reopen the app

## How It Works

The flow is basically:

**Pantry → ingredients stored in SQLite → recipes checked against the pantry → only recipes
you can actually make are shown.**

Every time you open the Recipes screen (or change something in your pantry), the app reloads
your current ingredients from the database and runs them against every seeded recipe. Nothing
is cached from an old state - it always checks against what's actually saved right now.

## Strict Matching

This is the main rule the whole app is built around: a recipe is only shown as "Ready to make"
if you have every single ingredient it needs, in at least the quantity it needs.

So if a recipe needs 5 ingredients and your pantry only has 4 of them, that recipe does not
show up there. Not even if you're only missing one small thing - there's a separate "Almost
there" list for recipes missing exactly one ingredient, so you're not just told "no" with no
explanation, but it's kept clearly separate from the actual suggestions.

A few things the matching handles so it's not just a dumb string comparison:

- **Singular/plural** - "Tomatoes" in your pantry will satisfy a recipe that needs "tomato".
  Same for things like "onions"/"onion" or "berries"/"berry". This is done with a small set of
  rules (strip -s, handle -ies → -y, -oes → -o, etc.), not a full NLP library.
- **Units** - if a recipe needs 500 g of flour and you've got 1 kg of flour logged in your
  pantry, that still counts, because both are mass units and the app converts between them
  before comparing. Same idea for volume (ml/l), and for imperial units (oz/lb, fl oz/cup/
  pint/gallon) - a recipe written in grams is still satisfied by an amount you logged in
  ounces, and vice versa. Units that don't have a sensible numeric conversion (like "pcs" vs
  "clove") just fall back to checking that something is there.
- **Quantity** - having 3 onions when a recipe needs 1 is fine. Having 1 onion when it needs 3
  is not, and the recipe won't be suggested.

The Settings screen's "Preferred unit system" is a separate, display-only layer on top of
this - it only changes what's shown on screen (e.g. 500 g rendered as 17.6 oz); the strict
matching above and everything actually stored in the database always uses the units you
originally entered.

## Technologies Used

- Java (no Kotlin anywhere in the project)
- Android Studio
- SQLite via `SQLiteOpenHelper` (no Room, no external database library)
- AndroidX (AppCompat, RecyclerView, ConstraintLayout, Material Components)
- JUnit 4 for unit tests
- Espresso for instrumented UI tests

No networking libraries, no Maps/location SDK, nothing cloud-based. Everything runs and
stores data locally on the device.

## Database

SQLite, using `SQLiteOpenHelper` directly rather than Room, so every CREATE TABLE / INSERT /
UPDATE / DELETE statement is plain, explainable SQL rather than something an annotation
generates. There's no cloud/networking need either - it's one user's own pantry, no syncing
between devices, no accounts - so a local database is the obvious fit and the app works with
no internet connection at all.

There are three tables:

- `pantry_items` - the user's own data: `_id`, `name`, `quantity`, `unit`, `icon_emoji`,
  `expiry_date`, `is_demo`. Full CRUD happens here.
- `recipes` - the seeded recipe collection: `_id`, `name`, `steps`.
- `recipe_ingredients` - one row per ingredient a recipe needs: `_id`, `recipe_id` (foreign
  key back to `recipes`), `name`, `quantity`, `unit`.

Recipes get seeded once, the first time the app runs (`onCreate()`), so the app isn't empty on
first launch and there's something to demonstrate straight away.

Every column added after the database already existed (`icon_emoji`, `expiry_date`,
`is_demo`) was added through an additive `onUpgrade()` migration (`ALTER TABLE ... ADD
COLUMN`) rather than dropping and recreating the database, so a real user's existing pantry
is never wiped by an app update. `is_demo` in particular is what makes Tutorial Mode safe: demo
rows are inserted flagged, and turning Tutorial Mode off deletes only rows carrying that flag -
a real item never gets touched by it.

## Project Structure

Not documenting every class here, just the ones that matter:

- `DatabaseHelper` - the `SQLiteOpenHelper`, all the CRUD methods, schema, and migrations
- `RecipeSeeder` - the hardcoded list of 19 recipes seeded on first run
- `IngredientMatcher` - the strict-matching logic described above, its own standalone class so
  it can be unit tested directly rather than sitting inside an Activity
- `UnitUtils` - name normalisation (plural → singular, lowercase, trimming), unit conversion
  (g/kg, ml/l, oz/lb, fl oz/cup/pint/gallon), and the display-only Preferred-unit-system
  conversion used by the Settings toggle
- `PantryListActivity` / `AddEditIngredientActivity` / `SuggestedRecipesActivity` /
  `RecipeDetailActivity` / `SettingsActivity` - the five main screens
- `PantryAdapter` / `RecipeAdapter` - the RecyclerView adapters for the pantry list and the
  recipe lists
- `FoodIconResolver` - maps ingredient/recipe names to a default emoji icon
- `AppPreferences` - small wrapper around SharedPreferences for the settings (dark mode,
  expiry alerts, unit system, Tutorial Mode)

## UI/UX Direction

A neutral background with one muted green accent instead of default Material colours, a
proper type scale instead of one font size for everything, and a real dark theme instead of
just inverted colours. Food emoji avatars on the pantry list, recipe list, and recipe detail
screen make it feel less like rows in a spreadsheet and more like an actual list of food, and
the standard bottom navigation is replaced with a rounded floating pill-shaped bar.

## Testing

There are two kinds of tests in this project:

**Unit tests** (`app/src/test`, run with `./gradlew test`, no emulator needed):
- `IngredientMatcherTest` - covers the strict-matching rule directly: full match, ingredients
  missing, insufficient quantity, singular/plural name matching, and unit conversion across
  compatible units.
- `UnitUtilsTest` - covers the unit conversion and normalisation logic: metric and imperial
  mass/volume conversion, that mass and volume units can't be mixed together, and that
  count-based units like "pcs" are never converted.

**Instrumented tests** (`app/src/androidTest`, run on a device/emulator with
`./gradlew connectedAndroidTest`): `StrictMatchingInstrumentedTest` covers the strict-matching
rule end to end through the actual screens, not just the matcher class in isolation.

## Setup

1. Clone the repository and open the project folder in Android Studio.
2. Let Gradle sync - it'll pull the Gradle distribution and Android Gradle Plugin
   automatically the first time, so you need an internet connection for that one-time sync.
3. Make sure Android SDK Platform 34 and matching Build-Tools are installed (Android Studio
   will prompt you if anything's missing).
4. Run it on an emulator or a physical device running Android 7.0 (API 24) or newer.
5. Click Run. The app installs, launches, and seeds its database with recipes automatically -
   there's nothing else to configure.

To run the unit tests from the command line:

```
./gradlew test
```

To build a debug APK:

```
./gradlew assembleDebug
```

## GitHub Development History

Built incrementally, one feature at a time - core CRUD and persistence first, then the
recipe/matching engine, then each screen, then a UI pass, then the smaller quality-of-life
features layered on top once the fundamentals were solid.

| Version | What was added |
|---|---|
| 0.1 – 0.2 | Initial project setup, basic CRUD screens, Pantry List UI foundation |
| 0.3 | Add/Edit Ingredient UI foundation |
| 0.4 | Recipe data foundation (`Recipe`, `RecipeIngredient` models, seeded recipes) |
| 0.5 | Strict ingredient-matching engine (`IngredientMatcher`, `UnitUtils`) + unit tests |
| 0.6 | Suggested Recipes screen (Ready to make / Almost there) |
| 0.7 | Recipe Detail screen |
| 0.8 | Settings screen (minimal) |
| 0.9 | Instrumented tests for strict matching, end to end through the real screens |
| 0.95 | Food emoji icons per pantry item, with a picker |
| 0.96 | Full UI overhaul - colour/type system, every screen redesigned, floating nav bar |
| 0.97 | Working Dark Mode toggle |
| 0.98 | Expiry dates on ingredients, with expiring-soon/expired alerts |
| 0.99 | Tutorial Mode - demo data plus "?" help tips on every screen |
| 1.0 | Recipe dish icons (avatars on the recipe list and detail screen) and a Preferred unit system setting (Metric/Imperial, display-only) |

## Author

Built by siyanda ([@sudo-user101](https://github.com/sudo-user101)).
