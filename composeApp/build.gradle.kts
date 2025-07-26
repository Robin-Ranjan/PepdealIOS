import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //alias(libs.plugins.googleServices)
    alias(libs.plugins.jetbrains.kotlin.serialization)
//    kotlin("plugin.serialization")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
//            export(project("io.github.mirzemehdi:kmpnotifier:1.4.0"))
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.pepdeal.infotech.PepdealApp")
        }
    }


    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            api(libs.datastore.preferences)
            api(libs.datastore)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.kotlinx.serialization.json)

            api("io.github.mirzemehdi:kmpnotifier:1.4.0")


            implementation("com.github.skydoves:landscapist-coil3:2.4.6")

            implementation("io.github.ismai117:kottie:2.0.1")

            implementation("dev.icerock.moko:permissions-compose:0.18.1")
            implementation("dev.icerock.moko:media:0.10.0")


            implementation("network.chaintech:compose-multiplatform-media-player:1.0.31")
            implementation("network.chaintech:sdp-ssp-compose-multiplatform:1.0.5")
            implementation(libs.datastore.preferences)
            implementation(libs.datastore)
            implementation(libs.alert.kmp)

            implementation("network.chaintech:cmptoast:1.0.4")

            // Geolocation
            implementation(libs.compass.geolocation)
            implementation(libs.compass.geolocation.mobile)

            // Geocoding
            implementation(libs.compass.geocoder)
            implementation(libs.compass.geocoder.mobile)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)

            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)

            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sonner)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        }

        iosMain.dependencies {
            //ktor
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    commonMainApi("dev.icerock.moko:mvvm-core:0.16.1")
    commonMainApi("dev.icerock.moko:mvvm-compose:0.16.1")
    commonMainApi("dev.icerock.moko:mvvm-flow:0.16.1")
    commonMainApi("dev.icerock.moko:mvvm-flow-compose:0.16.1")
    // Compose Multiplatform
    commonMainApi("dev.icerock.moko:media-compose:0.11.0")
    commonMainImplementation("com.attafitamim.krop:ui:0.1.6")

    commonMainApi("dev.icerock.moko:geo-compose:0.7.0")
}


