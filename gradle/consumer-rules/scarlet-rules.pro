-if interface * { @com.tinder.scarlet.ws.* <methods>; }
-keep,allowobfuscation interface <1>

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @com.tinder.scarlet.ws.* <methods>;
}