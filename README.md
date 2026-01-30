# MuscuNombre - Bodyland Tracker 🏋️

Application Android pour tracker tes séances à la salle de sport Bodyland.

## Fonctionnalités

- **Onglet Suivi des Séances** : Bouton pour pointer chaque visite à la salle
- **Compteur automatique** : Affiche le nombre total de séances
- **Calcul du coût par séance** : Prix de l'abonnement ÷ Nombre de séances
- **Onglet Réglages** : Configuration du prix de l'abonnement annuel
- **Période personnalisable** : Choix des dates de début et fin (par défaut: 1er août 2025 - 30 septembre 2026)
- **Persistance des données** : Tes données sont sauvegardées localement

## Technologies

- Kotlin
- Jetpack Compose (Material Design 3)
- DataStore pour la persistance
- Architecture MVVM

## Build l'APK

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou plus récent
- JDK 17
- Android SDK 34

### Étapes

1. **Ouvrir le projet dans Android Studio**
   ```bash
   cd muscunombre
   ```
   Ouvrir le dossier avec Android Studio

2. **Synchroniser Gradle**
   Android Studio synchronisera automatiquement les dépendances

3. **Générer l'APK Debug**
   ```bash
   ./gradlew assembleDebug
   ```
   L'APK sera dans `app/build/outputs/apk/debug/app-debug.apk`

4. **Générer l'APK Release (signé)**
   ```bash
   ./gradlew assembleRelease
   ```

### Installation sur téléphone

1. Activer les "Sources inconnues" dans les paramètres Android
2. Transférer l'APK sur le téléphone
3. Installer l'APK

## Utilisation

1. **Premier lancement** : Va dans l'onglet "Réglages"
2. **Configure le prix** : Entre le prix de ton abonnement annuel (ex: 400€)
3. **Définis la période** : Clique sur le bouton pour définir la période par défaut ou choisis tes dates
4. **Pointe tes séances** : Chaque fois que tu vas à la salle, clique sur "Allé à la salle aujourd'hui !"
5. **Suis tes stats** : Observe ton coût par séance diminuer à chaque visite !

## Calcul

```
Coût par séance = Prix de l'abonnement ÷ Nombre de séances

Exemple:
- Abonnement: 400€
- Séances: 10
- Coût par séance: 40€
```

## Structure du projet

```
app/
├── src/main/
│   ├── java/com/bodyland/muscunombre/
│   │   ├── MainActivity.kt          # UI principale avec Compose
│   │   ├── GymViewModel.kt           # Logique métier et persistance
│   │   └── ui/theme/
│   │       ├── Theme.kt              # Thème Material 3
│   │       └── Type.kt               # Typographie
│   ├── res/
│   │   ├── values/                   # Strings, colors, themes
│   │   ├── drawable/                 # Icônes
│   │   └── xml/                      # Règles de backup
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Licence

MIT
APK Android qui compte le nombre de fois où je vais à la salle par mois et le ratio que j'ai payé
