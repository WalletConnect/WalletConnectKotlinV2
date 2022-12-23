# **WalletConnect Chat - Kotlin**

Kotlin implementation of WalletConnect v2 Chat protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/chat)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/chat/installation)
* [Usage](https://docs.walletconnect.com/2.0/kotlin/chat/usage)
* [Protocol specification](https://docs.walletconnect.com/2.0/specs/chat/)

&nbsp;

## Installation

root/build.gradle.kts:

```gradle
allprojects {
 repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
 }
}
```

app/build.gradle.kts

```gradle
implementation(platform("com.walletconnect:android-bom:{BOM version}"))
implementation("com.walletconnect:android-core")
implementation("com.walletconnect:chat:release_version")
```

&nbsp;

## Sample apps

* For sample app run `chat sample module`
