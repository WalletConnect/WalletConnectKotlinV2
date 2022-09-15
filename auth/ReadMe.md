# **WalletConnect Auth - Kotlin**

Kotlin implementation of WalletConnect Auth protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/sign)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* Installation guide TBD
* Responder guide TBD
* Requester guide TBD
* [Protocol specification](https://docs.walletconnect.com/2.0/specs/auth/)

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
