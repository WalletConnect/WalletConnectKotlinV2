# **WalletConnect Core - Android**

Kotlin implementation of WalletConnect Core SDK for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/android-core)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/core/installation)
* [Pairing Client](https://docs.walletconnect.com/2.0/kotlin/core/pairing)
* [Relay Client](https://docs.walletconnect.com/2.0/kotlin/core/relay)

&nbsp;

## Installation

root/build.gradle.kts:

```gradle
allprojects {
 repositories {
    mavenCentral()
 }
}
```

app/build.gradle.kts

```gradle
implementation("com.walletconnect:android-core:release_version")
```

## Project ID

For the Project ID look at [Project ID](https://walletconnect.com/).
