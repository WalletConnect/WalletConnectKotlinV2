buildscript {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    repositories {
        google()
        maven(url = "https://jitpack.io")
        mavenLocal()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}