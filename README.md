![WalletConnect V2](docs/walletconnect-banner.svg)

# **WalletConnect V2 - Kotlin**

Kotlin implementation of WalletConnect v2 protocol for Android applications.

## Requirements
* Android min SDK 23
* Java 11

## Installation
root/build.gradle.kts:

```gradle
allprojects {
 repositories {
    maven(url = "https://jitpack.io")
 }
}
```

<br>

app/build.gradle

```gradle
implementation("com.github.WalletConnect:WalletConnectKotlinV2:1.0.0-beta01")
```

## **Usage**

### **Initialize WalletConnect Client**
```kotlin
val appMetaData = AppMetaData(name = "Wallet Name", description = "Wallet Description", url = "Wallet Url", icons = listOfIconUrlStrings)
val init = Init(application = application, projectId = "project id", appMetaData = appMetaData)
WalletConnectClient.initalize(init)
```

The controller client will always be the wallet which is exposing blockchain accounts to a Dapp and therefore is also in charge of signing.
To initialize the WalletConnect client, create a `ClientTypes.InitialParams` object in the Android Application class. The InitialParams object will need at least the application class, the ProjectID and the wallet's AppMetaData. The InitialParams object will then be passed to the `WalletConnectClient` initialize function. IntitalParams also allows for custom URLs by passing URL string into the `hostName` property.

### **Session WalletConnectClient.Delegate**
```kotlin
val listener = object: WalletConnectClient.Delegate {
   override fun onSessionProposal(sessionProposal: SessionProposal) {
      // Session Proposal object sent by Dapp after pairing was successful
   }

   override fun onSessionRequest(sessionRequest: SessionRequest) {
      // JSON-RPC methods wrapped by SessionRequest object sent by Dapp
   }

   override fun onSessionDelete(deletedSession: DeletedSession) {
      // Triggered when the session is deleted by the peer
   }

   override fun onSessionNotification(sessionNotification: SessionNotification) {
      // Triggered when the peer emits events as notifications that match the list of types agreed upon session settlement
   }
}
WalletConnectClient.setDelegate(listener)
```

The WalletConnectClient needs a `WalletConnectClient.Delegate` passed to it for it to be able to expose asynchronously updates sent from the Dapp.

### **Pair Clients**
```kotlin
val pair = Pair("wc:...")
val pairListener = object: Pairing {
   override fun onSuccess(settledPairing: SettledPairing) {
      // Settled pairing
   }

   override fun onError(error: Throwable) {
      // Pairing approval error
   }
}
WalletConnectClient.pair(pair, pairListener)
```

To pair the wallet with the Dapp, call the WalletConnectClient.pair function which needs a `Pair` and `Pairing`. 
ClientTypes.Params is where the Dapp Uri will be passed. 
WalletConnectClientListeners.Pairing is the callback that will be asynchronously called once there a pairing has been made with the Dapp.

### **Session Approval**
NOTE: addresses provided in `accounts` array should follow [CAPI10](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md) semantics.
```kotlin
val accounts: List<String> = /*list of accounts on chains*/
val sessionProposal: WalletConnectClientData = /*Session Proposal object*/
val approve: Approve = Approve(sessionProposal, accounts)
val listener: SessionApprove {
   override fun onSuccess(settledSession: SettledSession) {
      // Approve session success
   }

   override fun onError(error: Throwable) {
      // Approve session error
   }
}
WalletConnectClient.approve(approveParams, listener)
```

To send an approval, pass a Session Proposal object along with the list of accounts to the `WalletConnectClient.approve` function. Listener will asynchronously expose the settled session if the operation is successful.

### **Session Rejection**
```kotlin
val rejectionReason: String = /*The reason for rejecting the Session Proposal*/
val proposalTopic: String = /*Topic from the Session Proposal*/
val rejectParams: Reject = Reject(rejectionReason, proposalTopic)
val listener: SessionReject {
   override fun onSuccess(rejectedSession: RejectedSession) {
      // Rejection proposal
   }

   override fun onError(error: Throwable) {
      //Rejected proposal error
   }
}
WalletConnectClient.reject(rejectParams, listener)
```
To send a rejection for the Session Proposal, pass a rejection reason and the Session Proposal topic to the `WalletConnectClient.reject` function. Listener will asynchronously expose a `RejectedSession` object that will mirror the data sent for rejection.

### **Session Disconnect**
```kotlin
val disconnectionReason: String = /*The reason for disconnecting the Settled Session*/
val sessionTopic: String = /*Topic from the Settled Session*/
val disconnectParams = Disconnect(sessionTopic, disconnectionReason)
val listener = object : SessionDelete {
   override fun onSuccess(deletedSession: DeletedSession) {
      // DeleteSession object with topic and reason
   }

   override fun onError(error: Throwable) {
      // Session disconnect error
   }
}

WalletConnectClient.disconnect(disconnectParams, listener)
```
To disconnect from a settle session, pass a disconnection reason and the Settled Session topic to the `WalletConnectClient.disconnect` function. Listener will asynchronously expose a DeleteSession object that will mirror the data sent for rejection.

### **Respond Request**
```kotlin
val sessionRequestTopic: String = /*Topic of Settled Session*/
val jsonRpcResponse: JsonRpcResponse.JsonRpcResult = /*Settled Session Request ID along with request data*/
val result = Response(sessionTopic = sessionRequestTopic, jsonRpcResponse = jsonRpcResponse)
val listener = object : SessionPayload {
   override fun onError(error: Throwable) {
      // Error
   }
}

WalletConnectClient.respond(result, listener)
```
To respond to JSON-RPC methods that were sent from Dapps for a settle session, submit a `Response` with the settled session's topic and request ID along with the respond data to the `WalletConnectClient.respond` function. Any errors would exposed through the `SessionPayload` listener.

### **Reject Request**
```kotlin
val sessionRequestTopic: String = /*Topic of Settled Session*/
val jsonRpcResponseError: JsonRpcResponse.JsonRpcError = /*Settled Session Request ID along with error code and message*/
val result = Response(sessionTopic = sessionRequestTopic, jsonRpcResponse = jsonRpcResponseError)
val listener = object : SessionPayload {
   override fun onError(error: Throwable) {
      // Error
   }
}

WalletConnectClient.respond(result, listener)
```
To reject a JSON-RPC method that was sent from a Dapps for a settle session, submit a `Response` with the settled session's topic and request ID along with the rejection data to the `WalletConnectClient.respond` function. Any errors would exposed through the `SessionPayload` listener.

### **Session Update**
```kotlin
val sessionTopic: String = /*Topic of Settled Session*/
val sessionState: SessionState = /*object with list of accounts to update*/
val updateParams = Update(sessionTopic = sessionTopic, sessionState = sessionState)
val listener = object : SessionUpdate {
   override fun onSuccess(updatedSession: UpdatedSession) {
      // Callback for when Dapps successfully updates settled session
   }

   override fun onError(error: Throwable) {
      // Error
   }
}

WalletConnectClient.update(updateParams, listener)
```
To update a settled session, create a `Update` object with the settled session's topic and accounts to update session with to `WalletConnectClient.update`. Listener will echo the accounts updated on the Dapp if action is successful. 

### **Session Upgrade**
```kotlin
val sessionTopic: String = /*Topic of Settled Session*/
val permissions: SessionPermissions = /*list of blockchains and JSON-RPC methods to upgrade with*/
val upgradeParams = Upgrade(sessionTopic = sessionTopic, permissions = permissions)
val listener = object : SessionUpgrade {
   override fun onSuccess(upgradedSession: UpgradedSession) {
      // Callback for when Dapps successfully upgrades settled session
   }

   override fun onError(error: Throwable) {
      // Error
   }
}

WalletConnectClient.upgrade(upgradeParams, listener)
```
To upgrade a settled session, create a `Upgrade` object with the settled session's topic and blockchains and JSON-RPC methods to upgrade the session with to `WalletConnectClient.upgrade`. Listener will echo the blockchains and JSON-RPC methods upgraded on the Dapp if action is successful. 

### **Session Ping**
```kotlin
val sessionTopic: String = /*Topic of Settled Session*/
val pingParams = Ping(sessionTopic)
val listener = object : SessionPing {
   override fun onSuccess(topic: String) {
      // Topic being pinged
   }

   override fun onError(error: Throwable) {
      // Error
   }
}

WalletConnectClient.ping(pingParams, listener)
```
To ping a Dapp with a settled session, call `WalletConnectClient.ping` with the `Ping` with a settle session's topic. If ping is successful, topic is echo'd in listener.

### **Get List of Settled Sessions**
```kotlin
WalletConnectClient.getListOfSettledSessions()
```
To get a list of the most current setteld sessions, call `WalletConnectClient.getListOfSettledSessions()` which will return a list of type `SettledSession`.

### **Get List of Pending Sessions**
```kotlin
WalletConnectClient.getListOfPendingSession()
```
To get a list of the most current pending sessions, call `WalletConnectClient.getListOfPendingSession()` which will return a list of type `SessionProposal`.

### **Shutdown SDK**
```kotlin
WalletConnectClient.shutdown()
```
To make sure that the internal coroutines are handled correctly when leaving the application, call `WalletConnectClient.shutdown()` before exiting from the application.
<br>

## API Keys

For api keys look at [API Keys](../../api/api-keys.md)
