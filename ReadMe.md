![WalletConnect V2](/docs/walletconnect-banner.svg)

# **WalletConnect - Kotlin**

The communications protocol for web3, WalletConnect brings the ecosystem together by enabling hundreds of wallets and apps to securely connect and interact. This repository contains Kotlin implementation of
WalletConnect v2 protocols for Android applications.

####
## SDK Chart

| BOM   | [Core SDK](androidCore) | [Sign SDK](sign) | [Auth SDK](auth) | [Chat SDK](chat) |
|-------|-------------------------|------------------|------------------|------------------|
| 1.1.0 | 1.6.0                   | 2.4.0            | 1.4.0            | 1.0.0-alpha05    |
| 1.0.1 | 1.5.0                   | 2.3.1            | 1.3.0            | 1.0.0-alpha04    |
|       | 1.4.0                   | 2.2.0            | 1.2.0            | 1.0.0-alpha03    |
|       | 1.3.0                   | 2.1.0            | 1.1.0            | 1.0.0-alpha02    |
|       | 1.2.0                   |                  |                  | 1.0.0-alpha01    |
|       | 1.1.0                   | 2.0.0            | 1.0.0            |                  |
|       | 1.0.0                   | 2.0.0-rc.5       | 1.0.0-alpha01    |                  |


## BOM Instructions:
To help manage compatible dependencies stay in sync, we've introduced a [BOM](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import) to the Kotlin SDK. With this, you only need to update the BOM version to get the latest SDKs. Just add the BOM as a dependency and then list the SDKs you want to include into your project.    

### example build.gradle.kts
```kotlin
dependencies {
    implementation(platform("com.walletconnect:android-bom:{BOM version}"))
    implementation("com.walletconnect:android-core")
    implementation("com.walletconnect:sign")
}
```

## License
WalletConnect v2 is released under the Apache 2.0 license. [See LICENSE](/LICENSE) for details.