pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PyxisSapiens"

// --- Core (portable, Android-free where possible) ---
include(":core:common")
include(":core:ports")
include(":core:domain")
include(":core:geology")
include(":core:wmm")

// --- Core (Android) ---
include(":core:ui")
include(":core:database")
include(":core:data")
include(":core:sensors")
include(":core:location")
include(":core:media")
include(":core:export")

// --- Features ---
include(":feature:compass")
include(":feature:projects")
include(":feature:measurements")
include(":feature:media")
include(":feature:export")
include(":feature:stereonet")
include(":feature:map")
include(":feature:settings")

// --- App ---
include(":app")
