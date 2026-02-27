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
- **DataStore keys** : `gymlib_price`, `workout_price`, `running_price`, `start_date`, `end_date`, `onboarding_completed`
- **6 activités** : Dynamo 🚴, Circuit Training 💪, Cardio Boxing 🥊, Workout 🏋️, Running 👟, Autres ➕
- **3 catégories de prix** : Gymlib (Dynamo+CT+Boxing), Salle (Workout), Équipement (Running). "Autres" = gratuit.
- **7 tiers de gamification** : Vieux Rongeur → Mini Mouse → Knight Mouse → King Rat → Oonga Bouna → Meep Meep → Légende

---

## ✅ Ce qui a été fait (session du 27/02/2026)

### 1. Export/Import JSON des données
- **Export** : bouton dans Réglages → génère un fichier JSON via `ActivityResultContracts.CreateDocument` contenant toutes les sessions + la config (prix, dates)
- **Import** : bouton dans Réglages → ouvre un fichier JSON via `ActivityResultContracts.OpenDocument`, dialogue de confirmation, puis reset + réimport des données
- Format JSON : `{ version, exportDate, config: { gymlibPrice, workoutPrice, runningPrice, startDate, endDate }, sessions: [{ date, activity }] }`
- Utilise `org.json` (natif Android, zéro dépendance ajoutée)

### 2. Navigation par swipe entre onglets
- Remplacé le `TabRow` + `when` statique par un `HorizontalPager` (Compose Foundation)
- Les 4 onglets (Suivi, Calendrier, Utilisateur, Réglages) sont maintenant accessibles par swipe horizontal
- Les clics sur les tabs fonctionnent toujours (avec `animateScrollToPage`)
- Import : `androidx.compose.foundation.pager.HorizontalPager` + `rememberPagerState`
- Nécessite `@OptIn(ExperimentalFoundationApi::class)`

### Commits
```
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
