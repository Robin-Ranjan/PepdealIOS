import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //alias(libs.plugins.googleServices)
    kotlin("plugin.serialization")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    val ktorVersion = "2.3.7"
    sourceSets {
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            api(libs.datastore.preferences)
            api(libs.datastore)

            implementation("dev.gitlive:firebase-database:2.1.0")
            implementation("dev.gitlive:firebase-storage:2.1.0")
            implementation("dev.gitlive:firebase-crashlytics:2.1.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")


           // implementation("io.ktor:ktor-client-json:2.2.3")

            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

            //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
//            implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

            implementation("com.github.skydoves:landscapist-coil3:2.4.6")

            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

            implementation("io.github.ismai117:kottie:2.0.1")

            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            implementation("dev.icerock.moko:permissions-compose:0.18.1")
            implementation("dev.icerock.moko:media:0.10.0")

            implementation("com.russhwolf:multiplatform-settings:1.0.0")
        }

        iosMain.dependencies {
            implementation("dev.gitlive:firebase-database:2.1.0")
            implementation("dev.gitlive:firebase-storage:2.1.0")
            implementation("dev.gitlive:firebase-crashlytics:2.1.0")

            implementation("io.ktor:ktor-client-darwin:3.0.3")

            //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        }
    }
}
dependencies{
    commonMainImplementation("androidx.core:core:1.10.1")
    commonMainApi("dev.icerock.moko:mvvm-core:0.16.1")
    commonMainApi("dev.icerock.moko:mvvm-compose:0.16.1")
    commonMainApi("dev.icerock.moko:mvvm-flow:0.16.1")
    commonMainApi("dev.icerock.moko:mvvm-flow-compose:0.16.1")
    // Compose Multiplatform
    commonMainApi("dev.icerock.moko:media-compose:0.11.0")
    commonMainImplementation("com.attafitamim.krop:ui:0.1.6")
}


