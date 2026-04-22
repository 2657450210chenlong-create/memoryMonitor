import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.chen.memory.monitor.service"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

group = System.getenv("GROUP")
    ?: (findProperty("GROUP") as String?)
    ?: "com.github.your_github_owner"
version = System.getenv("VERSION")
    ?: System.getenv("VERSION_NAME")
    ?: (findProperty("VERSION_NAME") as String?)
    ?: "1.0.0-beta1"

dependencies {
    implementation(project(":memory-monitor-core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "memory-monitor-core-service"
                pom {
                    name.set("memory-monitor-core-service")
                    description.set("Foreground service presentation module for memory-monitor-core.")
                }
            }
        }
    }
}
