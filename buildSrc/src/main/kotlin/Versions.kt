import org.gradle.api.JavaVersion

const val KEY_PUBLISH_VERSION = "PUBLISH_VERSION"
const val KEY_PUBLISH_ARTIFACT_ID = "PUBLISH_ARTIFACT_ID"
const val KEY_SDK_NAME = "SDK_NAME"

//Latest versions
const val BOM_VERSION = "1.33.0"
const val FOUNDATION_VERSION = "1.17.3"
const val CORE_VERSION = "1.33.0"
const val SIGN_VERSION = "2.33.0"
const val AUTH_VERSION = "1.28.4"
const val CHAT_VERSION = "1.0.0-beta31"
const val NOTIFY_VERSION = "1.3.5"
const val WEB_3_WALLET_VERSION = "1.33.0"
const val WEB_3_MODAL_VERSION = "1.6.0"
const val WC_MODAL_VERSION = "1.5.5"
const val MODAL_CORE_VERSION = "1.6.0"

//Artifact ids
const val ANDROID_BOM = "android-bom"
const val FOUNDATION = "foundation"
const val ANDROID_CORE = "android-core"
const val SIGN = "sign"
const val AUTH = "auth"
const val CHAT = "chat"
const val NOTIFY = "notify"
const val WEB_3_WALLET = "web3wallet"
const val WEB_3_MODAL = "web3modal"
const val WC_MODAL = "walletconnect-modal"
const val MODAL_CORE = "modal-core"

val jvmVersion = JavaVersion.VERSION_11
const val MIN_SDK: Int = 23
const val TARGET_SDK: Int = 34
const val COMPILE_SDK: Int = TARGET_SDK
val SAMPLE_VERSION_CODE = BOM_VERSION.replace(".", "").toInt()
const val SAMPLE_VERSION_NAME = BOM_VERSION