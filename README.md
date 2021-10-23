![WalletConnect V2](docs/walletconnect-banner.svg)
<br/>
<br/>
# WalletConnect V2 - Kotlin

Kotlin implementation of WalletConnect v2 protocol for Android applications.

## Installation

To get started, add the WalletConnect library as a module to your project

#### Project build.gradle.kts
```gradle
implementation(project(":walletconnectV2"))
```

## Requirements
* Java 11

## Usage
### Using WalletConnect

#### Initialize WalletConnect Client
```kotlin
val initializeParams = ClientTypes.InitialParams(useTls = true, hostName = "relay.walletconnect.org", apiKey = "sample key", isController = true)
WalletConnectClient.initalize(initalizeParams)
```
The controller client will always be the "wallet" which is exposing blockchain accounts to a "dapp" and therefore is also in charge of signing.

#### Pair Clients
```kotlin
val pairParams = ClientTypes.PairParams("wc:...")
val pairListener = WalletConnectClientListeners.Pairing { sessionProposal -> /* handle session proposal */ }
WalletConnectClient.pair(pairParams, pairListener)
```


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html)