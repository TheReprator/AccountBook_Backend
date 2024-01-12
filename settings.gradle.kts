enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "AccountBook"

include(":lib:commonFeatureImpl")
include(":lib:base")
include(":lib:base-ktor")

include(":lib:testModule")

include(":api:language")
include(":api:splash")
include(":api:country")
include(":api:userIdentity")