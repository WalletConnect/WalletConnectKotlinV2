import org.gradle.api.JavaVersion

const val KEY_PUBLISH_VERSION = "PUBLISH_VERSION"
const val KEY_PUBLISH_ARTIFACT_ID = "PUBLISH_ARTIFACT_ID"
const val KEY_SDK_NAME = "SDK_NAME"

//Latest versions
const val BOM_VERSION = "1.7.0"
const val FOUNDATION_VERSION = "1.7.0"
const val CORE_VERSION = "1.12.0"
const val SIGN_VERSION = "2.10.0"
const val AUTH_VERSION = "1.10.0"
const val CHAT_VERSION = "1.0.0-beta04"
const val WEB_3_WALLET = "1.5.0"
const val WEB_3_INBOX = "1.0.0-alpha04"

val jvmVersion = JavaVersion.VERSION_11
const val MIN_SDK: Int = 23
const val TARGET_SDK: Int = 33
const val COMPILE_SDK: Int = TARGET_SDK
const val agpVersion = "7.3.1" // when changing, remember to change version in build.gradle.kts in buildSrc module
const val kotlinVersion = "1.7.21"
const val kspVersion = "$kotlinVersion-1.0.8"
const val dokkaVersion = "1.7.10" // when changing, remember to change version in build.gradle.kts in buildSrc module
const val googleServiceVersion = "4.3.14"

const val sqlDelightVersion = "1.5.4"
const val moshiVersion = "1.14.0"
const val coroutinesVersion = "1.6.4"
const val scarletVersion = "1.0.0"
const val scarletPackage = "com.github.WalletConnect.Scarlet"
const val koinVersion = "3.3.2"
const val mlKitBarcode = "17.0.3"
const val camera2Version = "1.1.0-alpha01"
const val lifecycleVersion = "2.5.1"
const val navVersion = "2.5.3"
const val retrofitVersion = "2.9.0"
const val okhttpVersion = "4.10.0"
const val bouncyCastleVersion = "1.70"
const val sqlCipherVersion = "4.5.3@aar"
const val multibaseVersion = "1.1.1"
const val jUnit5Version = "5.9.2"
const val androidxTestVersion = "1.5.0"
const val robolectricVersion = "4.9.2"
const val mockkVersion = "1.13.3"
const val jsonVersion = "20220924"
const val timberVersion = "5.0.1"
const val androidSecurityVersion = "1.1.0-alpha04"
const val web3jVersion = "4.9.5"
const val wsRestJavaVersion = "3.1.0"
const val fcmVersion = "23.1.1"