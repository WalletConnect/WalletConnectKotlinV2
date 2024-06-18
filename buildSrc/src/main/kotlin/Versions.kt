import org.gradle.api.JavaVersion

const val KEY_PUBLISH_VERSION = "PUBLISH_VERSION"
const val KEY_PUBLISH_ARTIFACT_ID = "PUBLISH_ARTIFACT_ID"
const val KEY_SDK_NAME = "SDK_NAME"

//Latest versions
const val BOM_VERSION = "1.32.1-SNAPSHOT"
const val FOUNDATION_VERSION = "1.17.2-SNAPSHOT"
const val CORE_VERSION = "1.32.0-SNAPSHOT"
const val SIGN_VERSION = "2.32.0-SNAPSHOT"
const val AUTH_VERSION = "1.28.3-SNAPSHOT"
const val CHAT_VERSION = "1.0.0-beta30-SNAPSHOT"
const val NOTIFY_VERSION = "1.3.4-SNAPSHOT"
const val WEB_3_WALLET_VERSION = "1.32.1-SNAPSHOT"
const val WEB_3_MODAL_VERSION = "1.5.4-SNAPSHOT"
const val WC_MODAL_VERSION = "1.5.4-SNAPSHOT"
const val MODAL_CORE_VERSION = "1.5.4-SNAPSHOT"

val jvmVersion = JavaVersion.VERSION_11
const val MIN_SDK: Int = 23
const val TARGET_SDK: Int = 34
const val COMPILE_SDK: Int = TARGET_SDK
val SAMPLE_VERSION_CODE = BOM_VERSION.replace("-SNAPSHOT", "").replace(".", "").toInt()
const val SAMPLE_VERSION_NAME = BOM_VERSION