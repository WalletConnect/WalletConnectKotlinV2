# **WalletConnect Push - Kotlin**

Kotlin implementation of WalletConnect v2 Push protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/push)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/push/installation)
* [Wallet Usage](https://docs.walletconnect.com/2.0/kotlin/push/wallet-usage)
* [Dapp Usage](https://docs.walletconnect.com/2.0/kotlin/push/dapp-usage)
* [Protocol specification](https://docs.walletconnect.com/2.0/specs/push/)

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
implementation("com.walletconnect:push")
```

&nbsp;

## Sample apps

* For sample app run `Sample Wallet module`
