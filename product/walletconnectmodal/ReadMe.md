# **WalletConnect Modal - Kotlin**

# Installation

Kotlin implementation of WalletConnectModal for Android applications.

Android Core ![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/android-core)

WalletConnectModal ![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/walletconnect-modal)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage
* [WalletConnectModal installation](https://docs.walletconnect.com/2.0/android/walletconnectmodal/installation)
* [WalletConnectModal usage](https://docs.walletconnect.com/2.0/android/walletconnectmodal/usage)

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
implementation(platform("com.walletconnect:android-bom:$BOM_VERSION"))
implementation("com.walletconnect:android-core")
implementation("com.walletconnect:walletconnect-modal")
```

