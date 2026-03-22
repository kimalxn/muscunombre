# Contexte de développement — Muscunombre 🐀💪

> Ce fichier sert de mémoire entre les sessions de développement avec l'IA.
> Dernière mise à jour : 22 mars 2026 (session 2)

---

## 📁 Architecture du projet

- **Langage** : Kotlin
- **UI** : Jetpack Compose + Material Design 3
- **Base de données** : Room (SQLite)
- **Préférences** : DataStore Preferences
- **Build** : Gradle + KSP (pour Room)
- **Min SDK** : 26 | **Target SDK** : 34 | **Java** : 17
- **Pas de DI** (ni Hilt, ni Koin)
- **Pas de Navigation Component** — NavigationBar (bottom) + HorizontalPager manuels
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
- **`GymSession`** (Room v5) : `id`, `date: LocalDate`, `activity: String`, `loggedAt: Long`, `note: String = ""`
- **`ActivityDefinition`** (data class) : `name: String`, `emoji: String`, `price: Double = 0.0`
- **DataStore keys** : `activities_json` (JSON array of ActivityDefinition), `start_date`, `end_date`, `onboarding_completed`
- **Legacy DataStore keys** (migration only) : `gymlib_price`, `workout_price`, `running_price`
- **6 activités par défaut** : Dynamo 🚴, Circuit Training 💪, Cardio Boxing 🥊, Workout 🏋️, Running 👟, Autres ➕
- **Activités dynamiques** : l'utilisateur peut ajouter, modifier et supprimer ses propres activités avec tag et prix individuel
- **7 tiers de gamification** : Vieux Rongeur → Mini Mouse → Knight Mouse → King Rat → Oonga Bouna → Meep Meep → Légende
- **Room migrations** : 3→4 `loggedAt` (ALTER TABLE), 4→5 `note` (ALTER TABLE)

---

## ✅ Ce qui a été fait (session du 27/02/2026)

### 1. Export/Import JSON des données
- **Export** : bouton dans Réglages → génère un fichier JSON via `ActivityResultContracts.CreateDocument` contenant toutes les sessions + la config
- **Import** : bouton dans Réglages → ouvre un fichier JSON via `ActivityResultContracts.OpenDocument`, dialogue de confirmation, puis reset + réimport des données
- Format JSON **v2** : `{ version: 2, exportDate, config: { activities: [{ name, emoji, price }], startDate, endDate }, sessions: [{ date, activity, loggedAt }] }`
- Rétro-compatible : import v1 (legacy 3 prix) automatiquement converti en v2
- Utilise `org.json` (natif Android, zéro dépendance ajoutée)

### 2. Navigation bottom bar + swipe
- Navigation bar en bas de l'écran avec `NavigationBar` + `NavigationBarItem`
- Icônes Material : Home (Suivi), DateRange (Calendrier), Person (Profil), Settings (Réglages)
- Swipe horizontal entre onglets via `HorizontalPager` (Compose Foundation)
- Nécessite `@OptIn(ExperimentalFoundationApi::class)`

### 3. Activités dynamiques avec CRUD complet
- **`ActivityDefinition`** : data class avec `name`, `emoji`, `price` — stockée en JSON dans DataStore (`activities_json`)
- **CRUD** : `addActivity()`, `updateActivity(oldName, newDef)`, `removeActivity(name)` dans le ViewModel
- **SettingsTab** : prix modifiable inline, bouton + pour ajouter, clic sur le nom pour éditer, icône Delete pour supprimer
- **Migration automatique** des anciennes clés `gymlib_price`/`workout_price`/`running_price`

### 4. UI épurée + timelog
- `GymSession.loggedAt: Long` ajouté (timestamp ms, migration Room 3→4)
- JSON export v2 inclut `loggedAt` par session

---

## ✅ Ce qui a été fait (session du 22/03/2026)

### 5. Désélection d'activités sur la page Suivi
- Les activités déjà pointées (vertes ✅) sont maintenant **pré-cochées et modifiables**
- Décocher une activité déjà enregistrée la supprime de la DB au clic sur **Valider**
- Le bouton dit toujours "Valider" (actif uniquement si la sélection diffère du réel)
- Logique : diff `selectedActivities` vs `todayActivities` → ajouts + suppressions en une passe
- `LaunchedEffect(todayActivities)` synchronise automatiquement les toggles avec la base

### 6. Compte rendu sur la période (Suivi)
- Nouvelle card tout en bas de `SessionTrackingTab`, sous "Activités"
- Format : *"Entre le DD/MM/YYYY et le DD/MM/YYYY, tu as fait : · N séances de X 🚴, ..."*
- Visible uniquement si `startDate != null && activityCounts.isNotEmpty()`

### 7. Notes sur les séances + séances en prévision (Calendrier)
- **Room migration 4→5** : ajout colonne `note TEXT NOT NULL DEFAULT ''` à `GymSession`
- Nouvelles méthodes DAO : `updateNoteForDate(date, note)`, `getNoteForDate(date): String?`
- Nouvelles méthodes ViewModel : `updateNoteForDate()`, `getNoteForDate()`, `removeTodaySession()`
- **Dates futures** désormais **cliquables** dans le calendrier → séances en prévision
- `CalendarDay` : couleur bleu pâle (`alpha = 0.35f`) pour les jours futurs avec séances
- `MultiActivitySelectionDialog` : champ `OutlinedTextField` pour la note (par jour)
  - La note est chargée via `LaunchedEffect(date)` → `viewModel.getNoteForDate()`
  - Titre de la dialog adapté si date future
  - Note sauvegardée si au moins une activité est sélectionnée
- Export/Import JSON mis à jour pour inclure le champ `note`

### 8. Swipe du calendrier pour changer de mois
- `HorizontalPager` (tabs) désactivé sur l'onglet Calendrier : `userScrollEnabled = pagerState.currentPage != 1`
- Le `LazyVerticalGrid` du calendrier intercepte les swipes horizontaux via `Modifier.pointerInput` + `detectHorizontalDragGestures`
- Seuil de 100px : swipe gauche → mois suivant, swipe droite → mois précédent
- Navigation entre onglets toujours possible via la barre du bas

### 9. Refonte graphique complète — style Revolut/Linear
**Thème (`Theme.kt`, `Type.kt`)**
- Nouvelle palette : fond `#F8FAFC` (Slate50), surface `#FFFFFF`, bleu `#2563EB`, gris Slate
- Dark mode : fond `#020617`, surface `#1E293B`
- `outlineVariant`, `outline` définis proprement pour les dividers discrets
- Typographie refaite : `letter-spacing` négatif (-0.1 à -0.5), poids SemiBold, famille default

**Navigation custom (`AppNavBar`)**
- Composable `AppNavBar` remplace `NavigationBar` Material stock
- Icône + label animés en couleur via `animateColorAsState(tween(200))`
- Pas d'effet ripple (`indication = null`), hauteur fixe 60dp, fond blanc pur
- Top bar : `Column { Surface + HorizontalDivider }` — titre "Muscunombre" centré, neutre
- `beyondBoundsPageCount = 1` sur le `HorizontalPager` pour pré-rendre les onglets adjacents → supprime les saccades

**SessionTrackingTab redesign**
- Header : date longue (ex: *Dimanche 22 mars*) + "Jour X · Y jours restants"
- Stats row dans une card (séances / €/séance / % période écoulé)
- Activités : **toggle pill animé** remplace les Checkboxes — pill bleu si actif, gris sinon
- Sections en `labelMedium` uppercase gris (`AUJOURD'HUI`, `PÉRIODE`, `ACTIVITÉS`...)
- Toutes les cartes : fond blanc + `elevation = 1.dp`, plus aucun fond coloré

**UserTab redesign**
- Emoji + nom du tier en couleur du tier, grand chiffre de séances (`displayMedium`)
- Progression vers le tier suivant : barre fine (6dp) au lieu de la barre épaisse bordée
- Liste des tiers : **une seule card** avec `HorizontalDivider` entre items (plus de cards individuelles colorées)
- Badge "vous" pill pour le tier actuel, "✓" pour les tiers débloqués

**CalendarTab**
- Stats row identique à SessionTrackingTab (card blanche)
- Header mois : `titleMedium` SemiBold, abréviations "Lun Mar Mer..." au lieu de "L M M"

**SettingsTab**
- Sections en `labelMedium` uppercase (`ACTIVITÉS`, `PÉRIODE DE SUIVI`, `EXPORT / IMPORT`)
- Card "À propos" neutre (plus de fond bleu)
- Bouton reset : fond rouge très léger, hauteur 46dp, style sobre

## ✅ Ce qui a été fait (session du 22/03/2026 — partie 2)

### 10. Refonte graphique — style "Clippy-Core Ligne Claire"
Direction artistique : *Tintin/Hergé × Microsoft Office 97 × mobile flat*

**Palette** (`Theme.kt` — objet `LC`)
| Token | Hex | Rôle |
|-------|-----|------|
| `LC.Blue` | `#1976D2` | Primaire, top bar, valeurs chiffrées |
| `LC.Yellow` | `#FFC107` | Secondaire, headers de section, toggle actif, nav indicator |
| `LC.Red` | `#D32F2F` | Erreur, bouton reset |
| `LC.BgBlue` | `#BBDEFB` | Fond global |
| `LC.Black` | `#000000` | Contours (2dp), textes labels bold |
| `LC.White` | `#FFFFFF` | Fond des cards |
| `LC.LightYellow` | `#FFF9C4` | primaryContainer |
| `LC.DarkBlue` | `#0D47A1` | usage futur |

**Règles de design**
- Couleurs plate UNIQUEMENT — aucun dégradé, aucune ombre
- Toutes les surfaces ont `elevation = 0.dp` + `border(2dp, LC.Black)` → `LcCard()`
- Headers de section : bande jaune `LC.Yellow` + texte noir gras + `HorizontalDivider(2.dp, LC.Black)` → `LcSectionHeader()`
- Nav bar : fond blanc + `2dp Black` top border + pastille jaune 28×3dp sous l'icône sélectionnée
- Top bar : fond `LC.Blue` + texte blanc `FontWeight.Black` + `2dp Black` bottom border
- Bouton "Valider" : fond `LC.Yellow` + `border(2dp, LC.Black)` + texte noir `FontWeight.Black`
- Dividers à l'intérieur des cards : `HorizontalDivider(LC.Black, 1dp)`
- Badge "vous" (tier actuel) : fond `LC.Yellow` + `border(1.5dp, LC.Black)` + texte noir
- `StatCell` : valeurs en `LC.Black FontWeight.Black`, labels en `Color(0xFF555555)`
- Emojis : réservés aux activités uniquement

**Typographie** (`Type.kt`)
- Poids augmentés : `displayMedium/Large` → `Black`, `headlineLarge/Medium` → `Black/Bold`
- `titleMedium/Large` → `Bold`, `labelMedium/Large` → `Bold` (au lieu de SemiBold/Medium)
- Letter-spacing : suppression des valeurs négatives, valeurs positives légères pour les labels (0.8sp)
- Body : taille légèrement réduite (15sp au lieu de 16sp pour bodyLarge) pour densité plus BD

**Composables helpers** (ajoutés avant `SessionTrackingTab`)
```kotlin
@Composable fun LcCard(modifier, shape, containerColor, content: ColumnScope.() -> Unit)
@Composable fun LcSectionHeader(title: String)
```

```bash
# Compiler
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew assembleDebug

# APK généré dans :
app/build/outputs/apk/debug/app-debug.apk

# Serveur local pour télécharger l'APK sur téléphone (même Wi-Fi)
cd app/build/outputs/apk/debug && python3 -m http.server 8080
# Puis ouvrir http://<IP_DU_MAC>:8080/app-debug.apk sur le téléphone
# IP locale habituelle : 192.168.1.106
```

- Le repo GitHub est : `https://github.com/kimalxn/muscunombre`
- Les releases sont publiées via l'onglet Releases GitHub

---

## 📋 Idées / À faire (backlog)

- [ ] Refactorer `MainActivity.kt` (~1540 lignes) en fichiers séparés par écran
- [ ] Ajouter des tests unitaires (ViewModel, DAO)
- [ ] Signer l'APK pour une release propre
- [ ] Publier automatiquement l'APK via GitHub Actions
- [ ] Ajouter des statistiques graphiques (charts par mois, par activité)
- [ ] Mode sombre amélioré (vérifier le CalendarDay en dark)
- [ ] Notifications de rappel pour les séances
- [ ] Historique des exports/imports
- [ ] Afficher les notes dans le calendrier (tooltip ou indicateur visuel sur le jour)
- [ ] Distinguer visuellement "séances réalisées" vs "séances en prévision" dans le compte total
- [x] Migration Room propre (3→4 loggedAt, 4→5 note)
- [x] Désélection d'activités sur Suivi + bouton Valider
- [x] Compte rendu sur la période (bas de Suivi)
- [x] Notes sur les séances (par jour, dans le dialog Calendrier)
- [x] Séances en prévision (dates futures cliquables dans Calendrier)
- [x] Swipe calendrier pour changer de mois (sans changer d'onglet)
- [x] Refonte graphique Revolut-like (thème, typo, nav bar custom, cards) — remplacée par Clippy-Core
- [x] Refonte graphique "Clippy-Core Ligne Claire" (Hergé × Office 97, palette plate, black borders)

---

## 🎨 Notes de design actuel — "Clippy-Core Ligne Claire"

- **Fond** : `#BBDEFB` (bleu pastel), **Surface/Cards** : `#FFFFFF`
- **Primaire** : `#1976D2` (bleu Office 97), **Secondaire** : `#FFC107` (jaune vif)
- **Accent/Erreur** : `#D32F2F` (rouge Tintin), **Contours** : `#000000` (2dp partout)
- **AppBar** : fond `LC.Blue` + texte blanc gras + 2dp black bottom border
- **Nav bar** : fond blanc + 2dp black top border + pastille jaune 28×3dp sous l'icône sélectionnée
- **Cards** : fond blanc pur + `border(2dp, Black)` via `LcCard`, elevation 0dp
- **Section headers** : bande `LC.Yellow` pleine + label Bold Black + divider 1.5dp Black via `LcSectionHeader`
- **Dividers dans les cards** : `HorizontalDivider(LC.Black, 1dp)`
- **Bouton primary** : fond `LC.Yellow` + `border(2dp, LC.Black)` + texte noir `FontWeight.Black`
- **Toggle pill** : fond `LC.Yellow` actif / `#DDDDDD` inactif + `border(1.5dp, LC.Black)`
- **Emojis** : réservés aux activités uniquement (pas dans la nav, les titres, les badges)

---

## 👨‍💻 Développeurs

- **Alexandre Kim** — kim.alxn@gmail.com
- **Jade Senterre** — senterrejade@gmail.com
