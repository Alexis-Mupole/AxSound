# AxSound 🎵

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/dashboards)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-purple.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

**AxSound** est un lecteur de musique hors-ligne haute-fidélité pour Android conçu pour offrir une pureté sonore sans compromis, un moteur de gain sonore surpuissant jusqu'à 400%, une personnalisation visuelle poussée et une consommation de batterie minimale grâce au décodage DSP matériel (*Audio Offload*).

---

## ⬇️ Téléchargement

[![Download AxSound](https://img.shields.io/badge/Download-AxSound.apk-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Alexis-Mupole/AxSound/raw/main/AxSound.apk)

> **Sûr et vérifiable** : cet APK est construit à partir du code open-source publié dans ce dépôt, sans code malveillant, sans publicité, sans tracking ni collecte de données. Vous pouvez librement l'inspecter, le compiler vous-même et comparer les binaires.
>
> **Empreinte SHA-256** de `AxSound.apk` :
> ```
> 94d575f59c2f51a8ed184780612356a23242e16d680a2e74ed073b959fa70df6
> ```
>
> Vérification : `certutil -hashfile AxSound.apk SHA256` (Windows) ou `sha256sum AxSound.apk` (Linux/macOS).

---

## 📸 Captures d'écran

<table>
  <tr>
    <td align="center"><img src="axs/1.jpeg" alt="AxSound — capture d'écran 1" width="260"/></td>
    <td align="center"><img src="axs/2.jpeg" alt="AxSound — capture d'écran 2" width="260"/></td>
    <td align="center"><img src="axs/3.jpeg" alt="AxSound — capture d'écran 3" width="260"/></td>
  </tr>
  <tr>
    <td align="center"><img src="axs/4.jpeg" alt="AxSound — capture d'écran 4" width="260"/></td>
    <td align="center"><img src="axs/5.jpeg" alt="AxSound — capture d'écran 5" width="260"/></td>
    <td></td>
  </tr>
</table>

---

## 🌟 Fonctionnalités Clés

### 🔊 Moteur de Volume Maximal (Jusqu'à 400% — Plus fort que VLC)
- **Amplification matérielle sûre** : Utilise `android.media.audiofx.LoudnessEnhancer` raccordé directement à la session audio ExoPlayer.
- **Limiteur Brickwall intégré** : Sur Android 9+ (API 28+), un limiteur `DynamicsProcessing` (-0.5 dBFS, attaque 1 ms, relâchement 50 ms) protège les haut-parleurs contre la saturation numérique et l'écrêtage.
- **Profils par sortie** : Mémorisation des niveaux d'amplification distincts pour Haut-parleur, Casque filaire et Bluetooth.
- **Avertissement de protection auditive** : Alerte explicite avant tout dépassement du seuil de 200% (+6 dB).

### 🔋 Efficacité Énergétique & Mode Éco (DSP Audio Offload)
- **Décodage DSP direct** : En *Mode Efficacité*, le décodage s'effectue directement sur le DSP basse consommation (`AudioOffloadPreferences`), permettant au processeur central (CPU) de rester en veille profonde ($\sim 1.8\%$ de batterie/heure écran éteint).
- **Gestion intelligente du WakeLock** : Configuration `setWakeMode(C.WAKE_MODE_LOCAL)` garantissant qu'aucun verrou d'éveil n'est maintenu hors lecture.
- **Libération automatique des ressources** : Mise en veille complète et libération des effets après 10 minutes d'inactivité.
- **Fréquence d'affichage optimisée** : Visualiseur de spectre bridé à 30 fps et barre de progression à 1 Hz, stoppés net lorsque l'écran est éteint.

### 🎨 Personnalisation Graphique Poussée
- **Modes d'affichage** : Thème Clair, Sombre, Système et **Noir Pur AMOLED** (`#000000`).
- **Nuancier de 16+ couleurs d'accentuation** + sélecteur personnalisé HEX/HSV.
- **Gradients immersifs** : Presets Midnight, Cyberpunk, Sunset, Ocean, Aurora, Forest, Rose et Electric.
- **Thématisation Dynamique** : Support de Material You (papier peint système) et extraction adaptative des couleurs de la pochette d'album (*Palette API*).
- **Import / Export de Thèmes** au format JSON sans redémarrage de l'application.

### 🎧 Formats Audio & Moteur de Lecture
- **Prise en charge universelle** : MP3, AAC, M4A, FLAC, WAV, OGG, Opus, AMR, MIDI, MKA, 3GP, AIFF, WMA et ALAC.
- **Badges Hi-Res** : Détection automatique des flux haute résolution (ex. `HI-RES • FLAC • 24-bit • 96 kHz`).
- **Gestion des paroles** : Paroles synchronisées avec fichiers `.lrc` ou balises intégrées.
- **Robustesse** : Reprise transparente et saut automatique sans crash en cas de fichier endommagé ou corrompu.
- **Égaliseur graphique** : 5 bandes, renforcement des basses (*Bass Boost*), spatialisateur (*Virtualizer*) et préréglages studio.

---

## 🏗️ Architecture & Technologies

AxSound respecte les principes de la **Clean Architecture** et du patron **MVVM** :

- **Langage** : Kotlin
- **Interface Utilisateur** : Jetpack Compose + Material Design 3
- **Moteur Audio** : AndroidX Media3 (ExoPlayer + `MediaSessionService`)
- **Persistance Locale** : Room Database (Favoris, Playlists, Historique, Cache métadonnées et ReplayGain)
- **Préférences** : Jetpack DataStore Preferences
- **Chargement d'Images** : Coil (avec cache mémoire & disque et redimensionnement strict)
- **Navigation** : Navigation Compose avec transitions fluides

---

## 🚀 Compilation & Développement Local

### Prérequis
- **Android Studio** Ladybug (2024.2+) ou supérieur
- **JDK** : Version 17 ou 21
- **Android SDK** : Compile SDK 36, Target SDK 35, Min SDK 26

### Cloner et compiler le projet

```bash
# Cloner le dépôt
git clone https://github.com/votre-compte/AxSound.git
cd AxSound

# Compiler le projet en mode Debug
gradle assembleDebug

# Lancer la suite de tests unitaires et Robolectric
gradle testDebugUnitTest
```

L'APK généré se trouvera dans : `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🤝 Contribuer au Projet

Les contributions sont les bienvenues ! Pour proposer une amélioration, corriger un bug ou soumettre une traduction :
1. Consultez notre guide de contribution : [CONTRIBUTING.md](CONTRIBUTING.md).
2. Respectez notre code de conduite : [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

---

## 📄 Licence

Ce projet est distribué sous licence open-source **Apache 2.0**. Consultez [LICENSE.md](LICENSE.md) pour plus de détails.
