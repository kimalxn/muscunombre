# CONTEXT.md — Muscunombre

Ce fichier sert de mémoire entre sessions AI. À réinjecter au début de chaque conversation.

## Projet

Application Android de suivi sportif. Kotlin + Jetpack Compose + Material 3. Single-activity (~1670 lignes dans MainActivity.kt, ~426 dans GymViewModel.kt). Room v5 pour la persistence, DataStore Preferences pour la config.

- **Package** : `com.bodyland.muscunombre`
- **GitHub** : `kimalxn/muscunombre`
- **Version courante** : versionCode=4, versionName="4.0" (tag v4.0.0)
- **Build** : `export JAVA_HOME=/opt/homebrew/opt/openjdk@17 && ./gradlew assembleDebug`
- **Servir l'APK** : `cd app/build/outputs/apk/debug && python3 -m http.server 8080` → `http://192.168.1.106:8080/app-debug.apk`
- **minSdk** : 26, **targetSdk** : 34, **compileSdk** : 34

## Architecture

### Room (GymDatabase.kt)
- Entity `GymSession` : `id`, `date` (LocalDate), `activity` (String), `loggedAt` (Long), `note` (String)
- DAO : `getAllSessions()`, `getSessionsInPeriod(start, end)`, `getSessionByDateAndActivity()`, `insertSession()`, `deleteSessionByDateAndActivity()`, `updateNoteForDate()`, `getNoteForDate()`, etc.
- Migrations : 3→4 (ajout `loggedAt`), 4→5 (ajout `note`)

### DataStore Preferences
- `activities_json` : liste des ActivityDefinition en JSON
- `start_date`, `end_date` : période de suivi (ISO strings)
- `onboarding_completed` : booléen
- `standalone_notes_json` : notes indépendantes (sans séance), map date→texte en JSON

### GymViewModel.kt
- **Tiers** : `GamificationTier` data class. 7 niveaux : Niveau 7 (0-10 séances, pire) → Niveau 1 (251+, meilleur). `displayLevel = 8 - tier`, `displayName = "Niveau $displayLevel"`.
- **Flows principaux** : `allSessions`, `sessionsInPeriod` (combine startDate+endDate → flatMapLatest → DAO), `sessionCount` (allSessions.size, pour les tiers), `subscriptionPrice` (somme des prix > 0), `datesWithStandaloneNotes`
- **Fonctions suspend** : `addSessionSuspend`, `removeActivitySuspend`, `updateNoteSuspend`, `getNoteForDate`, `getStandaloneNoteForDate`
- **CRUD activités** : `addActivity`, `updateActivity`, `removeActivity`
- **Dates** : `updateStartDateWithAutoEnd` (recalcule fin = début + 365j), `updateStartDateOnly`, `updateEndDate`
- **Export/Import** : JSON v2 avec activities, config, sessions

### MainActivity.kt — 4 onglets (HorizontalPager)
1. **Suivi** (`SessionTrackingTab`) : Pointer les activités du jour. Stats en haut (séances dans la période, coût/séance, % écoulé). Cards : AUJOURD'HUI, PÉRIODE, COÛT PAR SÉANCE, NIVEAU (tier all-time), ACTIVITÉS (comptées sur la période), COMPTE RENDU.
   - `sessionsInPeriod` utilisé pour les stats, coûts, activités, compte rendu
   - `sessionCount` (all-time) utilisé uniquement pour le tier/niveau
   - `allSessions` utilisé uniquement pour les activités du jour (todayActivities)
2. **Calendrier** (`CalendarTab`) : Grille Column+Row (pas LazyVerticalGrid). Swipe horizontal entre mois. Click sur un jour → dialog pour pointer des séances ou écrire une note. Jours avec note standalone ont un fond pâle.
3. **Profil** (`UserTab`) : Tier actuel avec nom + description + nombre de séances total. Progression vers le tier suivant avec barre + jours restants. Liste de tous les niveaux (badge "toi" sur le niveau actuel).
4. **Réglages** (`SettingsTab`) : Gestion inline des activités (ajout/suppression double-clic). Période (dates début/fin, toggle 365j auto). "Comment ça marche" (3 points). Export/Import JSON. À Propos (v4.0).

### UI / Design
- Palette Revolut/Linear : Slate50 background, Blue600 primary
- Typographie SemiBold avec letter-spacing négatif
- Navigation : `AppNavBar` custom avec toggle pills
- `beyondBoundsPageCount = 1`, `userScrollEnabled = true`
- Tutoiement partout (pas de vouvoiement)

## Conventions
- Pas d'emoji dans les textes UI (sauf emojis d'activités)
- Tutoiement systématique
- Français partout (dates `dd MMMM yyyy` avec `Locale.FRENCH`)
- Pluralisation correcte ("1 jour", "2 jours")
- README sobre et factuel
- Toast messages sobres, dialog titles sans emoji

## Fichiers principaux
```
app/src/main/java/com/bodyland/muscunombre/
├── MainActivity.kt          (~1670 lignes, tout le UI Compose)
├── GymViewModel.kt           (~426 lignes, logique métier + DataStore + Room)
├── data/
│   └── GymDatabase.kt        (Entity, DAO, Room DB, migrations)
└── ui/theme/
    ├── Theme.kt              (Revolut palette, dark/light)
    └── Type.kt               (Typographie SemiBold)
```

## Problèmes résolus (historique)
- Emoji encoding dans les edits de fichier → contournement via script Python
- HTTP 400 sur git push (fichiers trop gros) → `git config http.postBuffer` + .gitignore
- Race condition sur sauvegarde de notes → fonctions suspend ordonnées
- Swipe calendrier qui consommait le swipe d'onglet → Column+Row au lieu de LazyVerticalGrid
- Suivi qui ne filtrait pas par période → utilisation de `sessionsInPeriod` au lieu de `allSessions`
