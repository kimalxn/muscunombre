# Muscunombre

Application Android de suivi de séances de sport et de calcul du coût par séance.

---

## Télécharger

[Dernière version (APK)](https://github.com/kimalxn/muscunombre/releases/latest/download/muscunombre.apk)\
Ou depuis l'onglet **[Releases](https://github.com/kimalxn/muscunombre/releases)**.

### Installation sur Android

1. Télécharger le fichier APK sur le téléphone
2. Ouvrir le fichier — Android demandera d'autoriser l'installation depuis cette source
3. Autoriser, puis installer

---

## Fonctionnalités

### Suivi
- Pointer les activités du jour par pression, ou désélectionner avec un second appui
- Bouton "Valider" pour confirmer les changements
- Reconfirmation des séances prévues : les activités pré-cochées pour une date future apparaissent "En prévision" le jour J et doivent être validées
- Coût par séance global et par activité
- Niveau actuel et progression vers le niveau suivant
- Résumé de la période en bas de page

### Calendrier
- Vue mensuelle, navigation par swipe ou boutons fléchés
- Grille à hauteur fixe (6 lignes) pour une expérience stable
- Jours avec séance : fond coloré ; séances en prévision : fond atténué ; jours avec note seule : fond grisé
- Cliquer sur n'importe quel jour (passé, présent, futur) ouvre un dialogue pour enregistrer des activités et une note
- Les notes peuvent être enregistrées sans sélectionner d'activité
- Résumé du mois sélectionné en bas : nombre total de séances, détail par activité, comparaison vs mois précédent (vert/rouge)

### Profil
- Niveau actuel parmi 7 (Niveau 1 = meilleur, Niveau 7 = débutant)
- Seuils ajustés automatiquement au pro-rata de la durée de la période de suivi (référence : 365 jours)
- Barre de progression, séances restantes avant le niveau suivant, jours restants avant la fin de période
- Liste complète des niveaux avec seuils adaptés

### Réglages
- Activités : ajout inline, modification du tag / nom / prix annuel, suppression avec double confirmation
- Période de suivi : dates de début et de fin libres, ou 365 jours automatiques depuis le début
- Apparence : mode Clair, Sombre ou Système
- Export / Import des données en JSON avec horodatage (ex: muscunombre_2026-04-20.json)

### Thème dynamique
- La couleur principale de l'app s'adapte au niveau actuel : gris, bleu, vert, violet, orange, rouge ou bleu foncé
- S'applique à la navigation, boutons, toggles, calendrier, prix et tous les éléments interactifs

---

## Niveaux (référence 365 jours)

| Niveau | Séances |
|--------|---------|
| 1 | 251+ |
| 2 | 176–250 |
| 3 | 101–175 |
| 4 | 51–100 |
| 5 | 26–50 |
| 6 | 11–25 |
| 7 | 0–10 |

Pour une période différente, les seuils sont ajustés : `seuil = round(seuil_base × durée / 365)`.

---

## Calcul du coût

- **Coût par séance d'une activité** = prix annuel de l'activité / nombre de séances de cette activité sur la période
- **Coût global** = total des prix annuels / nombre total de séances payantes sur la période

Les activités sans prix (0 €) ne sont pas prises en compte dans le calcul.

---

## Développeurs

- Jade Senterre — senterrejade@gmail.com
- Alexandre Kim — kim.alxn@gmail.com

---

## Compiler depuis les sources

```bash
git clone https://github.com/kimalxn/muscunombre.git
cd muscunombre
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
```

Stack : Kotlin · Jetpack Compose · Room · Material 3 · DataStore

---

## Licence

MIT
