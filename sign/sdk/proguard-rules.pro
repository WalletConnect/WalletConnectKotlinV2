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