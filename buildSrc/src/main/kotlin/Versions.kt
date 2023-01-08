import org.gradle.api.JavaVersion

const val KEY_PUBLISH_VERSION = "PUBLISH_VERSION"
const val KEY_PUBLISH_ARTIFACT_ID = "PUBLISH_ARTIFACT_ID"
const val KEY_SDK_NAME = "SDK_NAME"

//Latest versions
const val BOM_VERSION = "1.3.0"
const val FOUNDATION_VERSION = "1.3.0"
const val CORE_VERSION = "1.8.0"
const val SIGN_VERSION = "2.6.0"
const val AUTH_VERSION = "1.6.0"
const val CHAT_VERSION = "1.0.0-alpha07"
const val WEB_3_WALLET = "1.1.0"

val jvmVersion = JavaVersion.VERSION_11
const val MIN_SDK: Int = 23
const val TARGET_SDK: Int = 33
const val COMPILE_SDK: Int = TARGET_SDK
const val agpVersion = "7.3.1" // when changing, remember to change version in build.gradle.kts in buildSrc module
const val kotlinVersion = "1.7.21"
const val kspVersion = "$kotlinVersion-1.0.8"
const val dokkaVersion = "1.7.10" // when changing, remember to change version in build.gradle.kts in buildSrc module

const val sqlDelightVersion = "1.5.4"
const val moshiVersion = "1.13.0"
const val coroutinesVersion = "1.6.4"
const val scarletVersion = "1.0.0"
const val scarletPackage = "com.github.WalletConnect.Scarlet"
const val koinVersion = "3.2.0"
const val mlKitBarcode = "16.0.1"
const val camera2Version = "1.1.0-alpha01"
const val lifecycleVersion = "2.5.1"
const val lifecycleRuntimeKtx = "2.4.1"
const val navVersion = "2.4.1"
const val retrofitVersion = "2.9.0"
const val okhttpVersion = "4.9.0"
const val bouncyCastleVersion = "1.70"
const val sqlCipherVersion = "4.5.0@aar"
const val multibaseVersion = "1.1.0"
const val jUnit5Version = "5.7.2"
const val androidxTestVersion = "1.4.0"
const val robolectricVersion = "4.6"
const val mockkVersion = "1.12.0"
const val jsonVersion = "20220320"
const val timberVersion = "5.0.1"
const val androidSecurityVersion = "1.1.0-alpha03"
const val web3jVersion = "4.9.4"
const val wsRestJavaVersion = "3.1.0"