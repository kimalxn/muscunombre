# Gym Rat 🐀

Application Android pour tracker tes séances à la salle de sport.

---

## 📱 Installation sur Android (SUPER SIMPLE)

### Méthode 1 : Télécharger l'APK déjà compilée
Si quelqu'un t'envoie le fichier `app-debug.apk` :
1. Copie le fichier sur ton téléphone (par email, Drive, câble USB...)
2. Sur ton téléphone, va dans **Paramètres → Sécurité**
3. Active **"Sources inconnues"** ou **"Installer applis inconnues"**
4. Ouvre le fichier APK et clique **Installer**
5. C'est prêt ! 🎉

---

### Méthode 2 : Compiler toi-même (Mac)

#### Étape 1 : Installer les outils (une seule fois)
Ouvre le **Terminal** (cherche "Terminal" dans Spotlight) et copie-colle ces commandes :

```bash
# Installer Homebrew (gestionnaire de paquets)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Installer Java 17
brew install openjdk@17

# Ajouter Java au PATH
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
source ~/.zshrc
```

#### Étape 2 : Compiler l'APK
Dans le Terminal, va dans le dossier du projet et compile :

```bash
cd ~/Projects/muscunombre
./gradlew assembleDebug
```

⏳ Attends que ça finisse (ça peut prendre 1-2 minutes la première fois).

#### Étape 3 : Récupérer l'APK
L'APK est ici : `app/build/outputs/apk/debug/app-debug.apk`

Pour l'envoyer sur ton téléphone :
```bash
# Lance un serveur web temporaire
cd app/build/outputs/apk/debug
python3 -m http.server 8080
```

Puis sur ton téléphone, ouvre Chrome et va sur :
```
http://[IP-DE-TON-MAC]:8080/app-debug.apk
```

💡 Pour trouver l'IP de ton Mac : `ipconfig getifaddr en0`

---

## 🎮 Fonctionnalités

- **Onboarding** : Choisis ta date de début (fin = +365 jours auto)
- **4 Onglets** : Suivi, Calendrier, Utilisateur, Réglages
- **Activités** : Dynamo, Circuit Training 1 & 2, Cardio Boxing, Workout, Running
- **Gamification** : 6 tiers de 🐀 Vieux Rongeur à 🏆 Légende
- **Prix/séance** : Calcul automatique basé sur ton abonnement

## 🏆 Tiers

| Tier | Nom | Séances |
|------|-----|---------|
| 1 | 🐀 Vieux Rongeur | 0-10 |
| 2 | 🐭 Mini Mouse | 11-25 |
| 3 | 🐭⚔️ Knight Mouse | 26-50 |
| 4 | 👑🐀 King Rat | 51-100 |
| 5 | 🦍 Oonga Boonga | 101-200 |
| 6 | 🏆✨ Légende | 201+ |

---

## ❓ Problèmes fréquents

**"Command not found: ./gradlew"**
→ Tu n'es pas dans le bon dossier. Fais `cd ~/Projects/muscunombre`

**"JAVA_HOME is not set"**
→ Relance le terminal ou fais `source ~/.zshrc`

**L'APK ne s'installe pas**
→ Active "Sources inconnues" dans les paramètres Android

---

MIT License
