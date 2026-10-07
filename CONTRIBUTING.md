# Guide de Contribution à AxSound 🤝

Merci pour votre intérêt à contribuer à **AxSound** ! Ce projet est un lecteur audio open-source développé avec passion par et pour la communauté.

Ce document détaille les règles et bonnes pratiques pour contribuer efficacement.

---

## 📋 Table des Matières
1. [Code de Conduite](#code-de-conduite)
2. [Comment Participer ?](#comment-participer-)
3. [Processus de Pull Request (PR)](#processus-de-pull-request-pr)
4. [Conventions de Commits et de Branches](#conventions-de-commits-et-de-branches)
5. [Standards de Code Kotlin & Compose](#standards-de-code-kotlin--compose)
6. [Exécution des Tests](#exécution-des-tests)

---

## 1. Code de Conduite
En participant à ce projet, vous vous engagez à respecter les règles de courtoisie et d'inclusivité définies dans notre [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

---

## 2. Comment Participer ?

### 🐛 Signaler un Bug
- Vérifiez d'abord dans l'onglet **Issues** de GitHub que le bug n'a pas déjà été signalé.
- Si le bug est inédit, ouvrez une issue avec le modèle de bug :
  - **Version d'Android** et modèle de l'appareil.
  - **Format audio** concerné (ex: FLAC 24-bit, MP3 320 kbps).
  - **Étapes claires** pour reproduire le problème.
  - Comportement attendu vs comportement observé.
  - Logs (Logcat) si applicables.

### 💡 Proposer une Nouvelle Fonctionnalité
- Ouvrez une issue en décrivant l'idée, le cas d'usage et les éventuels compromis sur l'autonomie de la batterie ou les performances.
- Attendez une validation préliminaire avant d'écrire une quantité importante de code.

---

## 3. Processus de Pull Request (PR)

1. **Forker** le dépôt et cloner votre copie locale.
2. Créez une nouvelle branche pour vos modifications :
   ```bash
   git checkout -b feature/nom-de-la-fonctionnalite
   ```
3. Apportez vos modifications en respectant les standards du projet.
4. Assurez-vous que l'ensemble des tests passent :
   ```bash
   gradle testDebugUnitTest
   ```
5. Vérifiez la compilation complète :
   ```bash
   gradle assembleDebug
   ```
6. Poussez votre branche sur votre fork et ouvrez une **Pull Request** vers la branche `main` du dépôt principal.
7. Décrivez clairement vos modifications dans la description de la PR.

---

## 4. Conventions de Commits et de Branches

### Nommage des Branches
- `feature/<description>` : pour les nouvelles fonctionnalités (ex: `feature/replaygain-scanner`)
- `bugfix/<description>` : pour la correction de bugs (ex: `bugfix/flac-artwork-crash`)
- `docs/<description>` : pour la documentation
- `refactor/<description>` : pour les refactorisations sans changement fonctionnel

### Messages de Commit (Conventional Commits)
Utilisez le format conventionnel :
- `feat: ajouter le support des égaliseurs 10 bandes`
- `fix: corriger l'affichage du format badge sur les fichiers WAV`
- `perf: optimiser le throttle de la barre de progression à 1 Hz`
- `docs: mettre à jour le README avec les détails d'Audio Offload`
- `test: ajouter un test unitaire pour le calcul du boost de volume`

---

## 5. Standards de Code Kotlin & Compose

- **Clean Architecture & MVVM** :
  - La logique métier et d'accès aux données doit se trouver dans le package `data/` et `playback/`.
  - Les Composables doivent rester découplés et se trouver dans `ui/`.
- **Jetpack Compose** :
  - Toujours utiliser `Modifier.testTag("...")` pour les éléments interactifs majeurs.
  - Respecter la taille de cible tactile minimale de 48 dp pour l'accessibilité.
  - Utiliser `collectAsStateWithLifecycle()` pour observer les flux d'état dans les Composables.
- **Gestion des Ressources Audio** :
  - Ne jamais maintenir de `WakeLock` en dehors de la lecture active.
  - Veiller à libérer les instances d'effets audio (`LoudnessEnhancer`, `Equalizer`, etc.) lors de l'arrêt du service.

---

## 6. Exécution des Tests

Avant toute soumission de code, lancez la suite de tests :
```bash
gradle testDebugUnitTest
```

Si vous ajoutez une nouvelle fonctionnalité complexe, veillez à accompagner votre code de tests unitaires ou de tests Robolectric correspondants dans `app/src/test/java/`.

Merci d'aider à rendre **AxSound** encore meilleur pour tous les amateurs de musique ! 🎶
