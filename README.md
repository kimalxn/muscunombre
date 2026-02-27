# Muscunombre 🐀💪

Application Android pour tracker tes séances de sport et optimiser ton budget fitness.

---

## 📱 Télécharger l'application (SUPER SIMPLE !)

### ➡️ [Clique ici pour télécharger l'APK](https://github.com/kimalxn/muscunombre/releases/latest/download/muscunombre.apk)

Ou va dans l'onglet **[Releases](https://github.com/kimalxn/muscunombre/releases)** et télécharge le fichier `muscunombre.apk`.

---

## 📲 Comment installer sur ton téléphone Android

### Étape 1 : Télécharge l'APK
- Sur ton téléphone, clique sur le lien ci-dessus
- Ou transfère le fichier depuis ton ordinateur (email, Drive, câble USB...)

### Étape 2 : Autorise l'installation
1. Quand tu ouvres le fichier, Android te demande d'autoriser l'installation
2. Clique sur **"Paramètres"** puis active **"Autoriser depuis cette source"**
3. Reviens en arrière et clique **"Installer"**

### Étape 3 : C'est prêt ! 🎉
L'application "Muscunombre" apparaît sur ton écran d'accueil.

---

## 🎮 Fonctionnalités

- **Suivi** : Pointe tes activités quotidiennes, vois le coût par séance
- **Calendrier** : Visualise toutes tes séances du mois
- **Profil** : Suis ta progression et tes tiers de gamification
- **Réglages** : Configure tes activités, prix, exporte/importe tes données

### Activités personnalisables
L'app vient avec 6 activités par défaut (Dynamo, Circuit Training, Cardio Boxing, Workout, Running, Autres) mais tu peux **ajouter, modifier et supprimer** tes propres activités avec un tag, un nom et un prix annuel.

### 💰 Calcul du coût par séance
L'app calcule automatiquement combien te coûte chaque séance — globalement et par activité — en fonction de tes prix d'abonnement.

### 📦 Export / Import
Sauvegarde et restaure toutes tes données (séances + config) en JSON. Format v2 avec timelog.

---

## 🏆 Système de Tiers

| Tier | Nom | Emoji | Séances |
|------|-----|-------|---------|
| 1 | Vieux Rongeur | 🐀💤 | 0-10 |
| 2 | Mini Mouse | 🐭🍼 | 11-25 |
| 3 | Knight Mouse | 🐭⚔️ | 26-50 |
| 4 | King Rat | 🐀👑 | 51-100 |
| 5 | Oonga Bouna | 🦍🔥 | 101-175 |
| 6 | Meep Meep | 🏃💨 | 176-250 |
| 7 | Légende | ⭐ | 251+ |

---

## 👨‍💻 Développeurs

Réalisé avec ❤️ par :
- **Jade Senterre** - senterrejade@gmail.com
- **Alexandre Kim** - kim.alxn@gmail.com

---

## 🛠️ Pour les développeurs

### Compiler l'APK

```bash
# Cloner le projet
git clone https://github.com/kimalxn/muscunombre.git
cd muscunombre

# Compiler (nécessite Java 17)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew assembleDebug

# L'APK se trouve dans :
# app/build/outputs/apk/debug/app-debug.apk
```

### Technologies utilisées
- Kotlin
- Jetpack Compose
- Room Database
- Material Design 3
- DataStore

---

## 📄 Licence

MIT License - Utilise, modifie et partage librement !
