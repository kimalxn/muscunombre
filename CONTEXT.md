# Contexte de développement — Muscunombre 🐀💪

> Ce fichier sert de mémoire entre les sessions de développement avec l'IA.
> Dernière mise à jour : 27 février 2026

---

## 📁 Architecture du projet

- **Langage** : Kotlin
- **UI** : Jetpack Compose + Material Design 3
- **Base de données** : Room (SQLite)
- **Préférences** : DataStore Preferences
- **Build** : Gradle + KSP (pour Room)
- **Min SDK** : 26 | **Target SDK** : 34 | **Java** : 17
- **Pas de DI** (ni Hilt, ni Koin)
- **Pas de Navigation Component** — TabRow + HorizontalPager manuels
- **Single Activity** — tout le UI est dans `MainActivity.kt`

### Fichiers clés
| Fichier | Rôle |
|---------|------|
| `app/src/main/java/.../MainActivity.kt` | Toute l'UI Compose (~1300 lignes) : 4 onglets, onboarding, dialogues |
| `app/src/main/java/.../GymViewModel.kt` | ViewModel unique + DataStore + gamification tiers |
| `app/src/main/java/.../data/GymDatabase.kt` | Entity `GymSession`, DAO, Room Database, constantes activités |
| `app/src/main/java/.../ui/theme/Theme.kt` | Thème Material 3 (light + dark) |
| `app/build.gradle.kts` | Dépendances et config build |
| `README.md` | Documentation utilisateur + dev |

### Structure des données
- **`GymSession`** (Room) : `id`, `date: LocalDate`, `activity: String`
- **`ActivityDefinition`** (data class) : `name: String`, `emoji: String`, `price: Double = 0.0`
- **DataStore keys** : `activities_json` (JSON array of ActivityDefinition), `start_date`, `end_date`, `onboarding_completed`
- **Legacy DataStore keys** (migration only) : `gymlib_price`, `workout_price`, `running_price`
- **6 activités par défaut** : Dynamo 🚴, Circuit Training 💪, Cardio Boxing 🥊, Workout 🏋️, Running 👟, Autres ➕
- **Activités dynamiques** : l'utilisateur peut ajouter, modifier et supprimer ses propres activités avec emoji et prix individuel
- **7 tiers de gamification** : Vieux Rongeur → Mini Mouse → Knight Mouse → King Rat → Oonga Bouna → Meep Meep → Légende

---

## ✅ Ce qui a été fait (session du 27/02/2026)

### 1. Export/Import JSON des données
- **Export** : bouton dans Réglages → génère un fichier JSON via `ActivityResultContracts.CreateDocument` contenant toutes les sessions + la config
- **Import** : bouton dans Réglages → ouvre un fichier JSON via `ActivityResultContracts.OpenDocument`, dialogue de confirmation, puis reset + réimport des données
- Format JSON **v2** : `{ version: 2, exportDate, config: { activities: [{ name, emoji, price }], startDate, endDate }, sessions: [{ date, activity }] }`
- Rétro-compatible : import v1 (legacy 3 prix) automatiquement converti en v2
- Utilise `org.json` (natif Android, zéro dépendance ajoutée)

### 2. Navigation par swipe entre onglets
- Remplacé le `TabRow` + `when` statique par un `HorizontalPager` (Compose Foundation)
- Les 4 onglets (Suivi, Calendrier, Utilisateur, Réglages) sont maintenant accessibles par swipe horizontal
- Les clics sur les tabs fonctionnent toujours (avec `animateScrollToPage`)
- Import : `androidx.compose.foundation.pager.HorizontalPager` + `rememberPagerState`
- Nécessite `@OptIn(ExperimentalFoundationApi::class)`

### 3. Activités dynamiques avec CRUD complet
- **Remplacé** les 6 activités hardcodées et 3 catégories de prix par un système entièrement dynamique
- **`ActivityDefinition`** : data class avec `name`, `emoji`, `price` — stockée en JSON dans DataStore (`activities_json`)
- **CRUD** : `addActivity()`, `updateActivity(oldName, newDef)`, `removeActivity(name)` dans le ViewModel
- **SettingsTab** : prix modifiable inline par activité, bouton ➕ pour ajouter, clic sur le nom pour éditer, 🗑️ pour supprimer
- **EditActivityDialog** : composable dédié pour ajouter/modifier une activité (emoji, nom, prix annuel)
- **Migration automatique** : au démarrage, les anciennes clés `gymlib_price`/`workout_price`/`running_price` sont converties en `ActivityDefinition` avec prix individuel
- **Export/Import v2** : le JSON inclut le tableau `activities` complet ; l'import v1 (legacy) est rétro-compatible
- **SessionTrackingTab** : affiche le prix par séance individuellement pour chaque activité
- **CalendarDay** : utilise les `activityDefs` dynamiques pour résoudre les emojis

### Commits
```
9e9f675 feat: activités dynamiques avec prix individuel et CRUD complet
4473b7a docs: add CONTEXT.md for AI development sessions
497c6b8 feat: ajout export/import JSON + navigation par swipe entre onglets
2eae943 v1.0.0 - Release Muscunombre
b0c89e5 🐀 Gym Rat v2: onboarding, activités, gamification
96774ac feat: Bodyland Tracker - app de suivi des séances de sport
```

---

## 🔧 Build & Déploiement

```bash
# Compiler
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew assembleDebug

# APK généré dans :
app/build/outputs/apk/debug/app-debug.apk

# Serveur local pour télécharger l'APK sur téléphone (même Wi-Fi)
cd app/build/outputs/apk/debug && python3 -m http.server 8080
# Puis ouvrir http://<IP_DU_MAC>:8080/app-debug.apk sur le téléphone
```

- Le repo GitHub est : `https://github.com/kimalxn/muscunombre`
- Les releases sont publiées via l'onglet Releases GitHub

---

## 📋 Idées / À faire (backlog)

- [ ] Refactorer `MainActivity.kt` (~1300 lignes) en fichiers séparés par écran
- [ ] Ajouter des tests unitaires (ViewModel, DAO)
- [ ] Signer l'APK pour une release propre
- [ ] Publier automatiquement l'APK via GitHub Actions
- [ ] Ajouter des statistiques graphiques (charts par mois, par activité)
- [ ] Mode sombre amélioré
- [ ] Notifications de rappel pour les séances
- [ ] Historique des exports/imports
- [ ] Migration Room propre (au lieu de `fallbackToDestructiveMigration`)

---

## 👨‍💻 Développeurs

- **Alexandre Kim** — kim.alxn@gmail.com
- **Jade Senterre** — senterrejade@gmail.com
