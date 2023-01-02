# **WalletConnect Auth - Kotlin**

Kotlin implementation of WalletConnect Auth protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/auth)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/auth/installation)
* [Responder](https://docs.walletconnect.com/2.0/kotlin/auth/wallet-or-responder-usage)
* [Requester](https://docs.walletconnect.com/2.0/kotlin/auth/dapp-or-requester-usage)
* [Protocol specification](https://docs.walletconnect.com/2.0/specs/auth/)

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
implementation("com.walletconnect:auth:release_version")
```

&nbsp;

## Sample apps

* For sample responder/wallet run `responder module`
* For sample requester/Dapp run `requester module`