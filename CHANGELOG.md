# Journal des Modifications (Changelog) 📦

Toutes les modifications notables apportées au projet **AxSound** seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/) et ce projet adhère à la [Gestion Sémantique de Version](https://semver.org/lang/fr/).

---

## [1.0.0] - 2026-10-07

### Ajouté (Added)
- **Moteur de Lecture Media3** : Intégration d'ExoPlayer et de `MediaSessionService` avec commandes de notification et gestion du détachement du casque.
- **Amplification de Volume jusqu'à 400%** : Moteur de gain `LoudnessEnhancer` (+12 dB max) avec limiteur de protection `DynamicsProcessing` (-0.5 dBFS) sur Android 9+.
- **Mode Efficacité Énergétique (Audio Offload)** : Décodage matériel direct sur DSP basse consommation lorsque les effets logiciels sont inactifs.
- **Égaliseur Graphique** : Égaliseur 5 bandes avec préréglages studio, amplification des basses (*Bass Boost*) et spatialisation (*Virtualizer*).
- **Thématisation & Personnalisation** :
  - Thèmes Clair, Sombre et **Noir Pur AMOLED**.
  - Nuancier de 16+ couleurs d'accentuation et sélecteur personnalisé HEX/HSV.
  - 8 thèmes de dégradés d'arrière-plan (Midnight, Cyberpunk, Sunset, Ocean, etc.).
  - Support de Material You (papier peint dynamique) et couleurs adaptatives de la pochette d'album.
  - Exportation et importation de configurations de thèmes en JSON.
- **Gestion Complète de la Bibliothèque** : 8 onglets de navigation (Titres, Albums, Artistes, Dossiers, Playlists, Favoris, Récents, Meilleurs titres).
- **Paroles Synchronisées** : Prise en charge des fichiers `.lrc` et des métadonnées de paroles intégrées.
- **Mini Lecteur Flottant** : Barre de contrôle persistante avec gestes de balayage pour changer de piste.
- **Persistance Locale** : Base de données Room pour les playlists, favoris, historique de lecture et cache de métadonnées.
- **Support des Intentions Externes** : Capacité d'ouvrir et de lire des fichiers audio directement depuis les gestionnaires de fichiers.
