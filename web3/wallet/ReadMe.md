# **WalletConnect Sign - Kotlin**

Kotlin implementation of web3wallet for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/com.walletconnect/web3wallet)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

* [web3wallet installation](TBD)
* [web3wallet usage](TBD)

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

## Migration guide from SignClient and AuthClient to Web3Wallet

Web3Wallet SDK introduces a new interface for all the wallet users that wraps the Sign and Auth clients internally. Only one dependence is needed to
enable new use cases for wallets. Down below you can find a migration guide for Sing and Auth clients.

## Migration from SignClient

### Initialization

```kotlin
//CoreClient
CoreClient.initialize(relayServerUrl, connectionType, application, metaData) { error -> }

//SignClient
val initParams = Sign.Params.Init(CoreClient)
SignClient.initialize(initParams) { error -> }

//Web3Wallet
val initParams = Wallet.Params.Init(CoreClient)
Web3Wallet.initialize(initParams) { error -> }
```

### Pair with a Dapp

```kotlin
//CoreClient
val pairingParams = Core.Params.Pair(pairingUri)
CoreClient.Pairing.pair(pairingParams) { error -> }

//Web3Wallet
val pairingParams = Wallet.Params.Pair(pairingUri)
Web3Wallet.pair(pairingParams) { error -> }
```

### Approve a session

```kotlin
//SignClient
val approveParams: Sign.Params.Approve = Sign.Params.Approve(proposerPublicKey, namespaces)
SignClient.approveSession(approveParams) { error -> }

//Web3Wallet
val approveParams = Wallet.Params.SessionApprove(proposerPublicKey namespaces)
Web3Wallet.approveSession(approveProposal) { error -> }
```

### Reject a session

```kotlin
//SignClient
val rejectParams = Sign.Params.Reject = Reject(proposerPublicKey, rejectionReason, rejectionCode)
SignClient.rejectSession(rejectParams) { error -> }

//Web3Wallet
val reject = Wallet.Params.SessionReject(proposerPublicKey, reason)
Web3Wallet.rejectSession(reject) { error -> }
```

### Respond to a session request

```kotlin
//SignClient
val jsonRpcResponse = Sign.Model.JsonRpcResponse.JsonRpcResult(requestId, result)
val result = Sign.Params.Response(sessionTopic, jsonRpcResponse)
SignClient.respond(result) { error -> }

//Web3Wallet
val jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(requestId, result)
val response = Wallet.Params.SessionRequestResponse(sessionTopic, jsonRpcResponse)
Web3Wallet.respondSessionRequest(response) { error -> }
```

### Reject a session request

```kotlin
//SignClient
val jsonRpcResponseError = Sign.Model.JsonRpcResponse.JsonRpcError(requestId, code, message) /*Session Request ID along with error code and message*/
val result = Sign.Params.Response(sessionTopic, jsonRpcResponse)
SignClient.respond(result) { error -> }

//Web3Wallet
val jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(requestId, code, message)
val result = Wallet.Params.SessionRequestResponse(sessionTopic, jsonRpcResponse)
Web3Wallet.respondSessionRequest(result) { error -> }
```

### Update a session

```kotlin
//SignClient
val updateParams = Sign.Params.Update(sessionTopic, namespaces)
SignClient.update(updateParams) { error -> }

//Web3Wallet
val update = Wallet.Params.SessionUpdate(sessionTopic, namespaces)
Web3Wallet.updateSession(update) { error -> }
```

### Extend a session

```kotlin
//SignClient
val extendParams = Sign.Params.Extend(sessionTopic)
WalletConnectClient.extend(exdendParams) { error -> }

//Web3Wallet
val extend = Wallet.Params.SessionExtend(sessionTopic)
Web3Wallet.extendSession(extend) { error -> }
```

### Disconnect a session

```kotlin
//SignClient
val disconnectParams = Sign.Params.Disconnect(sessionTopic, disconnectionReason, disconnectionCode)
SignClient.disconnect(disconnectParams) { error -> }

//Web3Wallet
val disconnect = Wallet.Params.SessionDisconnect(sessionTopic)
Web3Wallet.disconnectSession(disconnect) { error -> }
```

### WalletDelegate

```kotlin
//SignClient
val walletDelegate = object : SignClient.WalletDelegate {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
        // Triggered when wallet receives the session proposal sent by a Dapp
    }

    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
        // Triggered when a Dapp sends SessionRequest to sign a transaction or a message
    }

    override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
        // Triggered when the session is deleted by the peer
    }

    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
        // Triggered when wallet receives the session settlement response from Dapp
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
        // Triggered when wallet receives the session update response from Dapp
    }

    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        //Triggered whenever the connection state is changed
    }

    override fun onError(error: Sign.Model.Error) {
        // Triggered whenever there is an issue inside the SDK
    }
}
SignClient.setWalletDelegate(walletDelegate)

//Web3Wallet
val walletDelegate = object : Web3Wallet.WalletDelegate {
    override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
        // Triggered when wallet receives the session proposal sent by a Dapp
    }

    override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
        // Triggered when a Dapp sends SessionRequest to sign a transaction or a message
    }

    override fun onAuthRequest(authRequest: Wallet.Model.AuthRequest) {
        // Triggered when Dapp / Requester makes an authorisation request
    }

    override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
        // Triggered when the session is deleted by the peer
    }

    override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
        // Triggered when wallet receives the session settlement response from Dapp
    }

    override fun onSessionUpdateResponse(sessionUpdateResponse: Sign.Model.SessionUpdateResponse) {
        // Triggered when wallet receives the session update response from Dapp
    }

    override fun onConnectionStateChange(state: Sign.Model.ConnectionState) {
        //Triggered whenever the connection state is changed
    }

    override fun onError(error: Sign.Model.Error) {
        // Triggered whenever there is an issue inside the SDK
    }
}
Web3Wallet.setWalletDelegate(walletDelegate)
```

## Migration from AuthClient

### Initialization

```kotlin
//CoreClient
CoreClient.initialize(relayServerUrl, connectionType, application, metaData) { error -> }

//AuthClient
val initParams = Auth.Params.Init(CoreClient)
AuthClient.initialize(init = Auth.Params.Init(core = CoreClient)) { error -> }

//Web3Wallet
val initParams = Wallet.Params.Init(core = CoreClient)
Web3Wallet.initialize(initParams) { error -> }
```

### Authentication

```kotlin
//AuthClient
val signature = CacaoSigner.sign(message, privateKey, SignatureType.EIP191)
AuthClient.respond(Auth.Params.Respond.Result(requestId, signature, issuer)) { error -> }

//Web3Wallet
val signature = CacaoSigner.sign(message, privateKey, SignatureType.EIP191)
Web3Wallet.respond(Wallet.Params.AuthRequestResponse(requestId, signature, issuer)) { error -> }
```

### Message formatting

```kotlin
//AuthClient
val payloadParams: Auth.Params.PayloadParams = //PayloadParams received in the onAuthRequest callback
val issuer = //MUST be the same as send with the respond methods and follows: https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md
val formatMessage = Auth.Params.FormatMessage(payloadParamspayloadParams, issuer)
AuthClient.formatMessage(formatMessage)

//Web3Wallet
val payloadParams: Wallwt.Model.PayloadParams  = //PayloadParams received in the onAuthRequest callback
val issuer = //MUST be the same as send with the respond methods and follows: https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md
val formatMessage = Auth.Params.FormatMessage(Wallet.Params.FormatMessage(payloadParams, issuer))
Web3Wallet.formatMessage(formatMessage)
```

Test against:

* Live dapp - Sign - https://react-app.walletconnect.com
* Live dapp - Auth - https://react-auth-dapp.walletconnect.com/

## Sample app

* For sample wallet run `web3wallet module`