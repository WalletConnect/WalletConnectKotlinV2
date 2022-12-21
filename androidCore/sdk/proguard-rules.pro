#-keep public class com.walletconnect.android.** { *; }
#-keep public class com.walletconnect.foundation.** { *; }
#
##-repackageclasses 'com.walletconnect.android'
##-allowaccessmodification
##-keeppackagenames doNotKeepAThing

-dontobfuscate
-dontshrink
-dontoptimize
-dontusemixedcaseclassnames