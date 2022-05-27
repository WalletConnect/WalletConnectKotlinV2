![WalletConnect V2](docs/walletconnect-banner.svg)

# **WalletConnect V2 - Kotlin**

Kotlin implementation of WalletConnect v2 Sign protocol for Android applications.

[![](https://jitpack.io/v/WalletConnect/WalletConnectKotlinV2.svg)](https://jitpack.io/#WalletConnect/WalletConnectKotlinV2)

## Requirements

* Android min SDK 24
* Java 11

## Documentation and usage

* [Installation guide](https://docs.walletconnect.com/2.0/kotlin/sign/installation)
* [Usage guide](https://docs.walletconnect.com/2.0/kotlin/sign/usage)
* [Protocol specification](https://github.com/WalletConnect/walletconnect-specs)
* [Beginner Guide to WalletConnect v2.0 Sign Protocol] // Update URI (https://medium.com/walletconnect/beginner-guide-to-walletconnect-v2-0-for-android-developers-fd0fd3d9ec5f)

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

For sample wallet run `wallet module`
For sample Dapp run `dapp module`

&nbsp;

## Project ID

For the Project ID look at [Project ID](https://walletconnect.com/).
