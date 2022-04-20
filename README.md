![WalletConnect V2](docs/walletconnect-banner.svg)

# **WalletConnect V2 - Kotlin**

Kotlin implementation of WalletConnect v2 protocol for Android applications.

[![](https://jitpack.io/v/WalletConnect/WalletConnectKotlinV2.svg)](https://jitpack.io/#WalletConnect/WalletConnectKotlinV2)


## Requirements
* Android min SDK 24
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

app/build.gradle

```gradle
implementation("com.github.WalletConnect:WalletConnectKotlinV2:release_version")
```

&nbsp;

## **Usage**

### **Initialize WalletConnect Client**

```kotlin
val appMetaData = WalletConnect.Model.AppMetaData(
   name = "Wallet Name", 
   description = "Wallet Description", 
   url = "Wallet Url", 
   icons = listOfIconUrlStrings
)
val init = WalletConnect.Params.Init(
   application = application,
   relayServerUrl = /*websocket server with scheme, authority, and projectId as query parameter*/
   appMetaData = appMetaData
)

// or

val init = WalletConnect.Params.Init(
   application = application,
   useTls = /*true or false*/,
   hostName = /*websocket server with scheme and authority*/,
   projectId = /*projectId*/,
   appMetaData = appMetaData
)

WalletConnectClient.initalize(init)
```

The wallet client will always be responsible of exposing accounts(CAPI10) to a Dapp and therefore is also in charge of signing. To
initialize the WalletConnect client, create a `WalletConnect.Params.Init` object in the Android Application class. The Init object will need the
application class, the projectId, and the wallet's AppMetaData. The `WalletConnect.Params.Init` object will then be passed to
the `WalletConnectClient` initialize function. `WalletConnect.Params.Init` also allows for custom URLs by passing URL string into the `hostName`
property. Above, there are two example on how to create the initalizing parameters.

&nbsp;

## **Wallet**

### **WalletConnectClient.WalletDelegate**

```kotlin
val walletDelegate = object : WalletConnectClient.WalletDelegate {
    override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
        // Triggered when wallet receives the session proposal sent by a Dapp
    }

    override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
        // Triggered when a Dapp sends SessionRequest to sign a transaction or a message
    }

    override fun onSessionDelete(deletedSession: DWalletConnect.Model.DeletedSession) {
        // Triggered when the session is deleted by the peer
   }

   override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {
        // Triggered when the peer emits events that match the list of events agreed upon session settlement
   }

    override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
         // Triggered when wallet receives the session settlement response from Dapp
    }

    override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {
         // Triggered when wallet receives the update accounts response from Dapp
    }

    override fun onSessionUpdateMethodsResponse(response: WalletConnect.Model.SessionUpdateMethodsResponse) {
         // Triggered when wallet receives the update methods response from Dapp
    }

    fun onSessionUpdateEventsResponse(response: WalletConnect.Model.SessionUpdateEventsResponse) {
         // Triggered when wallet receives the update events response from Dapp
    }
}
WalletConnectClient.setWalletDelegate(walletDelegate)
```

The WalletConnectClient needs a `WalletConnectClient.WalletDelegate` passed to it for it to be able to expose asynchronously updates sent from
the Dapp.

&nbsp;

### **Pair Clients**
```kotlin
val pair = WalletConnect.Params.Pair("wc:...")
WalletConnectClient.pair(pair)
```

To pair the wallet with the Dapp, call the WalletConnectClient.pair function which needs a ` WalletConnect.Params.Pair` parameter.
` WalletConnect.Params.Pair` is where the WC Uri will be passed.

&nbsp;

### **Session Approval**
NOTE: addresses provided in `accounts` array should follow [CAPI10](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md) semantics.
```kotlin
val proposerPublicKey: String = /*Proposer publicKey from SessionProposal object*/
val accounts: List<String> = /*List of accounts on chains*/
val methods: List<String> = /*List of methods that wallet approves*/
val events: List<String> = /*List of events that wallet approves*/

val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(proposerPublicKey, accounts, methods, events)
WalletConnectClient.approveSession(approveParams) { error -> /*optional callback for error while sending session approval*/ }
```

To send an approval, pass a proposerPublicKey along with the list of accounts, methods and events to the `WalletConnectClient.approveSession` function.

&nbsp;

### **Session Rejection**
```kotlin
val proposerPublicKey: String = /*Proposer publicKey from SessionProposal object*/
val rejectionReason: String = /*The reason for rejecting the Session Proposal*/
val rejectionCode: String = /*The code for rejecting the Session Proposal*/
For reference use CAIP-25: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-25.md

val rejectParams:  WalletConnect.Params.Reject = Reject(proposerPublicKey, rejectionReason, rejectionCode)
WalletConnectClient.rejectSession(rejectParams) { error -> /*optional callback for error while sending session rejection*/ }
```
To send a rejection for the Session Proposal, pass a proposerPublicKey, rejection reason and rejection code to the `WalletConnectClient.rejectSession` function.

&nbsp;

### **Session Disconnect**
```kotlin
val disconnectionReason: String = /*The reason for disconnecting the Session*/
val disconnectionCode: String = /*The code for for disconnecting the Session*/
val sessionTopic: String = /*Topic from the Session*/
For reference use CAIP-25: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-25.md
val disconnectParams = WalletConnect.Params.Disconnect(sessionTopic, disconnectionReason, disconnectionCode)

WalletConnectClient.disconnect(disconnectParams)
```
To disconnect from a settled session, pass a disconnection reason with code and the Session topic to the `WalletConnectClient.disconnect` function.

&nbsp;

### **Respond Request**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val jsonRpcResponse: WalletConnect.Model.JsonRpcResponse.JsonRpcResult = /*Settled Session Request ID along with request data*/
val result = WalletConnect.Params.Response(sessionTopic = sessionTopic, jsonRpcResponse = jsonRpcResponse)

WalletConnectClient.respond(result) { error -> /*optional callback for error while responding session request*/ }
```
To respond to JSON-RPC methods that were sent from Dapps for a session, submit a `WalletConnect.Params.Response` with the session's topic and request ID along with the respond data to the `WalletConnectClient.respond` function.

### **Reject Request**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val jsonRpcResponseError: WalletConnect.Model.JsonRpcResponse.JsonRpcError = /*Session Request ID along with error code and message*/
val result = WalletConnect.Params.Response(sessionTopic = sessionTopic, jsonRpcResponse = jsonRpcResponseError)

WalletConnectClient.respond(result) { error -> /*optional callback for error while responding session request*/ }
```
To reject a JSON-RPC method that was sent from a Dapps for a session, submit a `WalletConnect.Params.Response` with the settled session's topic and request ID along with the rejection data to the `WalletConnectClient.respond` function.

&nbsp;

### **Session Update Accounts**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val accounts: List<String> = /*List of accounts to update*/
val updateParams = WalletConnect.Params.Update(sessionTopic = sessionTopic, accounts = accounts)

WalletConnectClient.updateAccounts(updateParams) { error -> /*optional callback for error while sending update accounts request*/ }
```
To update a session with accounts, create a `WalletConnect.Params.UpdateAccounts` object with the session's topic and accounts to update session with to `WalletConnectClient.updateAccounts`.

&nbsp;

### **Session Update Methods**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val methods: List<String> = /*List of methods to update*/
val updateParams = WalletConnect.Params.UpdateMethods(sessionTopic = sessionTopic, methods = methods)

WalletConnectClient.updateMethods(updateParams) { error -> /*optional callback for error while sending update methods request*/ }
```
To update a session with methods, create a `WalletConnect.Params.UpdateMethods` object with the session's topic and methods to update the session with to `WalletConnectClient.updateMethods`.

&nbsp;

### **Session Update Events**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val events: List<String> = /*List of methods to update*/
val updateParams = WalletConnect.Params.UpdateEvents(sessionTopic = sessionTopic, events = events)

WalletConnectClient.updateEvents(updateParams) { error -> /*optional callback for error while sending update events request*/ }
```
To update a session with events, create a `WalletConnect.Params.UpdateEvents` object with the session's topic and events to update the session with to `WalletConnectClient.updateEvents`.

&nbsp;

### **Session Update Expiry**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val newExpiration: Long = /*New session timestamp expiration in seconds. Must be greater than current session expiration and max 7 days*/
val updateParams = WalletConnect.Params.UpdateExpiry(sessionTopic = sessionTopic, newExpiration = newExpiration)

WalletConnectClient.updateExpiry(updateParams) { error -> /*optional callback for error while sending update session expiry request*/ }
```
To update a session with new expiration, create a `WalletConnect.Params.UpdateExpiry` object with the session's topic and new expiration to update the session with to `WalletConnectClient.updateExpiry`.

&nbsp;

### **Session Ping**
```kotlin
val sessionTopic: String = /*Topic of Session*/
val pingParams = WalletConnect.Params.Ping(sessionTopic)
val listener = object : WalletConnect.Listeners.SessionPing {
   override fun onSuccess(pingSuccess: Model.Ping.Success) {
      // Topic being pinged
   }

    override fun onError(pingError: Model.Ping.Error) {
        // Error
    }
}

WalletConnectClient.ping(pingParams, listener)
```

To ping a peer with a session, call `WalletConnectClient.ping` with the `WalletConnect.Params.Ping` with a session's topic. If
ping is successful, topic is echo'd in listener.

&nbsp;

&nbsp;


## **Daap**

### **WalletConnectClient.DappDelegate**

```kotlin
val dappDelegate = object : WalletConnectClient.DappDelegate {
    override fun onSessionApproved(approvedSession: WalletConnect.Model.ApprovedSession) {
        // Triggered when Dapp receives the session approval from wallet
    }

    override fun onSessionRejected(rejectedSession: WalletConnect.Model.RejectedSession) {
        // Triggered when Dapp receives the session rejection from wallet
    }

    override fun onSessionUpdateAccounts(updatedSession: WalletConnect.Model.UpdatedSessionAccounts) {
        // Triggered when Dapp receives the session update accounts from wallet
    }

    fun onSessionUpdateMethods(updatedSession: WalletConnect.Model.UpdatedSessionMethods) {
        // Triggered when Dapp receives the session update methods from wallet
    }

    fun onSessionUpdateEvents(updatedSession: WalletConnect.Model.UpdatedSessionEvents) {
           // Triggered when Dapp receives the session update events from wallet
    }

    fun onUpdateSessionExpiry(session: WalletConnect.Model.Session) {
           // Triggered when Dapp receives the session update expiry from wallet
    }

    fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
           // Triggered when Dapp receives the session delete from wallet
    }

    fun onSessionRequestResponse(response: WalletConnect.Model.SessionRequestResponse) {
           // Triggered when Dapp receives the session request response from wallet
    }
}
WalletConnectClient.setWalletDelegate(dappDelegate)
```

The WalletConnectClient needs a `WalletConnectClient.DappDelegate` passed to it for it to be able to expose asynchronously updates sent from the
Wallet.

&nbsp;

### **Connect**

```kotlin
val chains: List<String> = /*List of chains that wallet will be requested for*/
val methods: List<String> = /*List of methods that wallet will be requested for*/
val events: List<String> = /*List of events that wallet will be requested for*/
val pairingTopic: String? =  /* Optional parameter, use it when the pairing between peers is already established*/
val connectParams = WalletConnect.Params.Connect(chains, methods, events, pairingTopic)

fun WalletConnectClient.connect(connectParams, {proposedSequence -> /*callback that returns the WalletConnect.Model.ProposedSequence*/}, {error -> /*optional callback for error while sending session proposal*/})
```

The `WalletConnectClient.connect` asynchronously exposes the pairing URI that is shared with wallet out of bound, as qr code or mobile linking. The WalletConnect.Model.ProposedSequence
is Session when there is already an established pairing between peers. To establish a session between peers, pass the existing pairing's topic to the connect
method. The SDK will send the SessionProposal under the hood for the given topic and expect session approval or rejection in onSessionApproved and onSessionRejected in DappDelegate accordingly.

&nbsp;

### **Get List of Settled Sessions**

```kotlin
WalletConnectClient.getListOfSettledSessions()
```

To get a list of the most current settled sessions, call `WalletConnectClient.getListOfSettledSessions()` which will return a list of
type `Session`.

&nbsp;

### **Get List of Settled Pairings**

```kotlin
WalletConnectClient.getListOfSettledPairings()
```

To get a list of the most current settled pairings, call `WalletConnectClient.getListOfSettledPairings()` which will return a list of
type `Pairing`.

&nbsp;

### **Get list of pending session requests for a topic**

```kotlin
WalletConnectClient.getPendingRequests(topic: String)
```
To get a list of pending session requests for a topic, call `WalletConnectClient.getPendingRequests()` and pass a topic which will return a `PendingRequest` object containing requestId, method, chainIs and params for pending request.

&nbsp;

### **Shutdown SDK**
```kotlin
WalletConnectClient.shutdown()
```
To make sure that the internal coroutines are handled correctly when leaving the application, call `WalletConnectClient.shutdown()` before exiting from the application.

&nbsp;

## Project ID

For the Project ID look at [Project ID](../../api/project-id.md).
