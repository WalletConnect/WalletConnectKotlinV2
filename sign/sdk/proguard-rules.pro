-keep class com.walletconnect.** { *; }
-keep interface com.walletconnect.** { *; }

-repackageclasses 'com.walletconnect'
-allowaccessmodification
-keeppackagenames doNotKeepAThing

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-if interface * { @com.tinder.scarlet.ws.* <methods>; }
-keep,allowobfuscation interface <1>

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @com.tinder.scarlet.ws.* <methods>;
}



#-keep class com.walletconnect.sign.** { *; }
##-keep class com.walletconnect.** { *; }
##-keep interface com.walletconnect.** { *; }
#
#-repackageclasses 'com.walletconnect.sign'
#-allowaccessmodification
#-keeppackagenames doNotKeepAThing
#
#-keep,includedescriptorclasses class net.sqlcipher.** { *; }
#-keep,includedescriptorclasses interface net.sqlcipher.** { *; }
#
#-if interface * { @com.tinder.scarlet.ws.* <methods>; }
#-keep,allowobfuscation interface <1>
#
#-keepclassmembers,allowshrinking,allowobfuscation interface * {
#    @com.tinder.scarlet.ws.* <methods>;
#}
#
##-dontobfuscate
##-dontshrink
##-dontoptimize
##-dontusemixedcaseclassnames
#-keeppackagenames doNotKeepAThing
