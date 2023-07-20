-dontwarn java8.util.**
-dontwarn jnr.posix.**
-dontwarn com.kenai.**

#-keep class org.bouncycastle.**
-dontwarn org.bouncycastle.jce.provider.X509LDAPCertStoreSpi
-dontwarn org.bouncycastle.x509.util.LDAPStoreHelper
-keep class org.bouncycastle.** { *; }

# Web3j
-keep class org.web3j.** { *; }

-keep class * extends org.web3j.abi.TypeReference
-keep class * extends org.web3j.abi.datatypes.Type

#-dontwarn java.lang.SafeVarargs
-dontwarn org.slf4j.**