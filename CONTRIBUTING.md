# AxSound Contribution Guide 🤝

Thank you for your interest in contributing to **AxSound**! This project is an open-source audio player developed with passion by and for the community.

This document details the rules and best practices for contributing effectively.

---

## 📋 Table of Contents
1. [Code of Conduct](#1-code-of-conduct)
2. [How to Participate?](#2-how-to-participate)
3. [Pull Request (PR) Process](#3-pull-request-pr-process)
4. [Commit & Branch Conventions](#4-commit--branch-conventions)
5. [Kotlin & Compose Code Standards](#5-kotlin--compose-code-standards)
6. [Running Tests](#6-running-tests)

---

## 1. Code of Conduct
By participating in this project, you agree to abide by the rules of courtesy and inclusivity defined in our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

---

## 2. How to Participate?

### 🐛 Report a Bug
- First check the GitHub **Issues** tab to make sure the bug has not already been reported.
- If the bug is new, open an issue using the bug template:
  - **Android version** and device model.
  - **Audio format** involved (e.g. FLAC 24-bit, MP3 320 kbps).
  - **Clear steps** to reproduce the problem.
  - Expected vs. actual behavior.
  - Logs (Logcat) if applicable.

### 💡 Propose a New Feature
- Open an issue describing the idea, the use case and any trade-offs regarding battery life or performance.
- Wait for preliminary approval before writing a large amount of code.

---

## 3. Pull Request (PR) Process

1. **Fork** the repository and clone your local copy.
2. Create a new branch for your changes:
   ```bash
   git checkout -b feature/feature-name
   ```
3. Make your changes while following the project's standards.
4. Make sure the whole test suite passes:
   ```bash
   gradle testDebugUnitTest
   ```
5. Verify the full build:
   ```bash
   gradle assembleDebug
   ```
6. Push your branch to your fork and open a **Pull Request** against the `main` branch of the upstream repository.
7. Clearly describe your changes in the PR description.

---

## 4. Commit & Branch Conventions

### Branch Naming
- `feature/<description>`: new features (e.g. `feature/replaygain-scanner`)
- `bugfix/<description>`: bug fixes (e.g. `bugfix/flac-artwork-crash`)
- `docs/<description>`: documentation
- `refactor/<description>`: refactoring without functional changes

### Commit Messages (Conventional Commits)
Use the conventional format:
- `feat: add 10-band equalizer support`
- `fix: correct format badge display on WAV files`
- `perf: optimize progress bar throttling to 1 Hz`
- `docs: update README with Audio Offload details`
- `test: add unit test for volume boost calculation`

---

## 5. Kotlin & Compose Code Standards

- **Clean Architecture & MVVM**:
  - Business and data-access logic must live in the `data/` and `playback/` packages.
  - Composables must stay decoupled and live in `ui/`.
- **Jetpack Compose**:
  - Always use `Modifier.testTag("...")` for major interactive elements.
  - Respect the minimum 48 dp touch target size for accessibility.
  - Use `collectAsStateWithLifecycle()` to observe state flows in Composables.
- **Audio Resource Management**:
  - Never hold a `WakeLock` outside of active playback.
  - Make sure to release audio effect instances (`LoudnessEnhancer`, `Equalizer`, etc.) when the service stops.

---

## 6. Running Tests

Before submitting any code, run the test suite:
```bash
gradle testDebugUnitTest
```

If you add a complex feature, make sure to ship corresponding unit tests or Robolectric tests in `app/src/test/java/`.

Thanks for helping make **AxSound** even better for every music lover! 🎶
