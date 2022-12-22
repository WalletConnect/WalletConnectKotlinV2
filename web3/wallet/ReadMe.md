# **WalletConnect Sign - Kotlin**

Kotlin implementation of web3wallet for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/web3wallet)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [Installation guide](TBD)
* [web3wallet usage](TBD)
* [Migration guide](TBD)

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
implementation("com.walletconnect:web3wallet:release_version")
```

&nbsp;

## Sample app

* For sample wallet run `web3wallet module`