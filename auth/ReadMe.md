# **WalletConnect Auth - Kotlin**

Kotlin implementation of WalletConnect Auth protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/sign)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/sign/installation)
* [Wallet guide](https://docs.walletconnect.com/2.0/kotlin/sign/wallet-usage)
* [Dapp guide](https://docs.walletconnect.com/2.0/kotlin/sign/dapp-usage)
* [Protocol specification](https://github.com/WalletConnect/walletconnect-specs)
* [Beginner Guide to WalletConnect v2.0 Sign Protocol](https://medium.com/walletconnect/beginner-guide-to-walletconnect-v2-0-sign-protocol-for-android-developers-936293e30700)
* [Glossary](https://docs.walletconnect.com/2.0/introduction/glossary)

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
implementation("com.walletconnect:sign:release_version")
```

&nbsp;

## Sample apps

* For sample responder/wallet run `responder module`
* For sample requester/Dapp run `requester module`

&nbsp;

## Project ID

For the Project ID look at [Project ID](https://walletconnect.com/).
