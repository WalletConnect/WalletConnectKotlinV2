# **WalletConnect Sign - Kotlin**

Kotlin implementation of WalletConnect v2 Sign protocol for Android applications.

[![](https://jitpack.io/v/WalletConnect/WalletConnectKotlinV2.svg)](https://jitpack.io/#WalletConnect/WalletConnectKotlinV2)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/sign/installation)
* [Usage guide](https://docs.walletconnect.com/2.0/kotlin/sign/usage)
* [Protocol specification](https://github.com/WalletConnect/walletconnect-specs)
* [Beginner Guide to WalletConnect v2.0 Sign Protocol](https://medium.com/walletconnect/beginner-guide-to-walletconnect-v2-0-sign-protocol-for-android-developers-936293e30700)
* [Glossary](https://docs.walletconnect.com/2.0/introduction/glossary)

&nbsp;

## Installation

root/build.gradle.kts:

```gradle
allprojects {
 repositories {
    maven(url = "https://jitpack.io")
 }
}
```

app/build.gradle

```gradle
implementation("com.github.WalletConnect:WalletConnectKotlinV2:release_version")
```

&nbsp;

## Sample apps

* For sample wallet run `wallet module`
* For sample Dapp run `dapp module`

&nbsp;

## Project ID

For the Project ID look at [Project ID](https://walletconnect.com/).
