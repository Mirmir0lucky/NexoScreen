NexoScreens 26.2 compatibility overlay

Target:
- Paper/Leaf 26.2
- Java 25 build toolchain
- Nexo 1.25.0+
- ProtocolLib 5.4.0 API
- PlaceholderAPI 2.12.3 (optional at runtime)

Apply preferred:
  git apply NexoScreens-26.2.patch

The patch also deletes:
- src/main/java/com/github/mrjimin/nexoscreens/NexoScreensLoader.java
- src/main/resources/plugin.yml

Why:
- fixes unresolved Git merge markers in build.gradle.kts
- migrates Paper dependency from 1.21.x-R0.1-SNAPSHOT to 26.2.build.+
- moves build to a Java 25 toolchain / Gradle 9.1
- raises Nexo to the first line with 26.2 support (1.25.0)
- updates PlaceholderAPI to 26.2-capable 2.12.3
- removes brittle NMS-field reflection from GAME_STATE_CHANGE packet handling
- declares PAPI in Paper's plugin classpath graph
- removes runtime Kotlin Maven loader and builds a self-contained shaded jar
- fixes the invulnerability restore bug
