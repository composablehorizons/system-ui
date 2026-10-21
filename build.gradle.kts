import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    id("com.android.kotlin.multiplatform.library") version "9.2.0"
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose.compiler)
    id("com.composables.kpm") version "0.1.0"
}

tasks.withType<Test>().configureEach {
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
}

kotlin {
    jvmToolchain(17)
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        namespace = "com.composables.systemui"
        compileSdk = 37
        minSdk = 23
        withHostTest { isIncludeAndroidResources = true }
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
        }
        androidMain.dependencies { implementation("androidx.core:core-ktx:1.17.0") }
        getByName("androidHostTest").dependencies {
            implementation(kotlin("test"))
            implementation("androidx.compose.ui:ui-test-junit4:1.12.0")
            implementation("androidx.compose.ui:ui-test-manifest:1.12.0")
            implementation("org.robolectric:robolectric:4.16")
        }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}
