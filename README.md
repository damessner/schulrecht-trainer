# Schulrecht Trainer AT

Moderne Android Lern-App für österreichisches Schulrecht (SchUG, LDG, Tiroler Schulorganisationsgesetz, Stand 04.09.2026).

- **Stack:** Kotlin, Jetpack Compose (Material3), Navigation Compose, Room, DataStore, Retrofit/Moshi, OkHttp, Coroutines/Flow
- **Offline-first:** Fragen werden aus [schulrecht-content](https://github.com/damessner/schulrecht-content) geladen und in Room gecacht. Neue Batches brauchen kein App-Update.
- **Didaktik:** Praxis-Fälle statt Paragrafen-Abfragen. Single/Multiple (mit Teilpunkten) + Richtig/Falsch, 4 Level pro Modul (Basis → Handlung → Experte → Transfer), Feedback pro Option, Auflösung mit Quelle zum Nachlesen.

## Bauen

Voraussetzungen: JDK 17, Android SDK (API 35), `sdk.dir` in `local.properties`.

```bash
./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest # Unit-Tests (Scoring-Logik)
```

Lern-App, keine Rechtsberatung.
