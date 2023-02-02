# **WalletConnect Sign - Kotlin**

Kotlin implementation of WalletConnect v2 Sign protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/sign)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/sign/installation)
* [Wallet guide](https://docs.walletconnect.com/2.0/kotlin/sign/wallet-usage)
* [Dapp guide](https://docs.walletconnect.com/2.0/kotlin/sign/dapp-usage)
* [Protocol specification](https://docs.walletconnect.com/2.0/specs/sign/)
* [Beginner Guide to WalletConnect v2.0 Sign Protocol](https://medium.com/walletconnect/beginner-guide-to-walletconnect-v2-0-sign-protocol-for-android-developers-936293e30700)
* [Glossary](https://docs.walletconnect.com/2.0/introduction/glossary)

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
implementation("com.walletconnect:sign")
```

&nbsp;

## Sample apps

* For sample wallet run `wallet module`
* For sample Dapp run `dapp module`
