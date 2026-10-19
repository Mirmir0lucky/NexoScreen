plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "NexoScreens"

// The old api and nms:v1_21_8 modules contain no runtime code used by NexoScreens.
// Keeping them in the build forces obsolete 1.21.8/1.21.x Paper dependencies to resolve.
