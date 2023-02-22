# **WalletConnect Web3Inbox - Kotlin**

Kotlin implementation of Web3Inbox for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/web3inbox)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage
* [Web3Inbox installation](https://docs.walletconnect.com/2.0/kotlin/web3inbox/installation)
* [Web3Inbox usage](https://docs.walletconnect.com/2.0/kotlin/web3inbox/wallet-usage)

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
implementation("com.walletconnect:web3inbox")
```

&nbsp;

## Sample app

* For sample wallet run `web3inbox module`
