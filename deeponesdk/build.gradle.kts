import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("signing")
}

val ciVersion = providers.gradleProperty("version").orNull
val publishedVersion = ciVersion ?: "1.0.134"

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "io.deepone.sdk",
        artifactId = "deeponesdk",
        version = publishedVersion
    )

    // Ensure Android release variant is published, with sources and an empty javadoc jar
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true
        )
    )

    pom {
        name.set("DeepOne SDK")
        description.set("The main sdk of DeepOne library")
        url.set("https://github.com/deeponelabs/deepone-android-sdk")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("deeponelabs")
                name.set("deeponelabs")
                url.set("https://github.com/deeponelabs")
            }
        }
        scm {
            // keep these three aligned to the same repo
            url.set("https://github.com/deeponelabs/deepone-android-sdk")
            connection.set("scm:git:https://github.com/deeponelabs/deepone-android-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/deeponelabs/deepone-android-sdk.git")
        }
    }
}
android {
    namespace = "io.deepone.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
        allWarningsAsErrors = false
        suppressWarnings = true
    }
    
    // Suppress lint warnings for library
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        warningsAsErrors = false
        quiet = true
    }
}

dependencies {
    implementation("io.deepone.sdk:deeponenetworking:1.0.19")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}