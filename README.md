# Compose System UI

Control system bar icon appearance from Compose Multiplatform.

```kotlin
import com.composables.systemui.SystemBarIcons
import com.composables.systemui.SystemBars

@Composable
fun App() {
    SystemBars(
        statusBar = SystemBarIcons.Dark,
        navigationBar = SystemBarIcons.Dark,
    )
    // Your UI
}
```

`Light` means light-colored icons; `Dark` means dark-colored icons. `Default` leaves the native setting unchanged. On Android the composition must be hosted in an Activity. The library makes the navigation bar transparent, disables navigation-bar contrast enforcement where available, and restores previous settings when the effect leaves composition.

## iOS host

Host the app with the library's controller so UIKit can read the requested status-bar style:

```kotlin
import com.composables.systemui.SystemUiViewController

fun MainViewController() = SystemUiViewController {
    App()
}
```

Calling `SystemBars` on iOS without this host or an explicit `ProvideSystemUi` provider throws. iOS currently applies the status-bar appearance; it does not customize navigation-bar icons. JVM and Wasm implementations are no-ops because they have no native mobile system bars.

`SystemUiState` and `ProvideSystemUi` optionally let a host observe appearance requests instead of applying them natively. Ordinary Android apps do not need that provider.

## Installation

Add Maven Central to your dependency repositories and add the library to `commonMain`:

```kotlin
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.composables:system-ui:0.1.0")
        }
    }
}

## Build locally

Use JDK 17, Xcode for iOS, and Android SDK platform 37. From this directory:

```sh
./gradlew jvmTest publishToMavenLocal
```

Standalone builds include Android, iOS arm64/simulator arm64, JVM, and Wasm.

## Releasing

Install KPM:

```sh
npm install --global @composablehorizons/kpm@0.1.0
```

Set the release notes in `CHANGELOG.md`, then run from this directory:

```sh
./gradlew jvmTest publishToMavenLocal
kpm version patch
git push --follow-tags
```

`package.yml` owns the version and Maven publishing metadata. The tagged GitHub Actions release checks the version, runs JVM tests, builds all platform publications, and publishes signed artifacts with `kpm publish`. After every platform artifact is downloadable from Maven Central, it creates a draft GitHub release from the changelog. Review and publish that draft separately.

Publishing uses the `maven-central` environment and the organization secrets `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `SIGNING_KEY_ID`, `SIGNING_PASSWORD`, and `SIGNING_KEY`.
