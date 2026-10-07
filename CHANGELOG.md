# Changelog 📦

**English | [Français](CHANGELOG.fr.md)**

All notable changes made to the **AxSound** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-07

### Added
- **Media3 Playback Engine**: Integration of ExoPlayer and `MediaSessionService` with notification controls and headset disconnect handling.
- **Volume Boost up to 400%**: `LoudnessEnhancer` gain engine (+12 dB max) with a protective `DynamicsProcessing` limiter (-0.5 dBFS) on Android 9+.
- **Power Efficiency Mode (Audio Offload)**: Direct hardware decoding on the low-power DSP when software effects are inactive.
- **Graphic Equalizer**: 5-band equalizer with studio presets, Bass Boost and Virtualizer.
- **Theming & Customization**:
  - Light, Dark and **Pure Black AMOLED** themes.
  - 16+ accent colors and a custom HEX/HSV picker.
  - 8 background gradient themes (Midnight, Cyberpunk, Sunset, Ocean, etc.).
  - Material You support (dynamic wallpaper) and adaptive album-art colors.
  - Theme configuration export and import as JSON.
- **Complete Library Management**: 8 navigation tabs (Tracks, Albums, Artists, Folders, Playlists, Favorites, Recent, Top Tracks).
- **Synced Lyrics**: Support for `.lrc` files and embedded lyrics tags.
- **Floating Mini Player**: Persistent control bar with swipe gestures to change track.
- **Local Persistence**: Room database for playlists, favorites, play history and metadata cache.
- **External Intent Support**: Ability to open and play audio files directly from file managers.
