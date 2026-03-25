# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-keep class com.mdm.devicemanager.data.model.** { *; }
-keep class com.mdm.devicemanager.data.api.** { *; }

# Gson
-keep class com.google.gson.** { *; }
