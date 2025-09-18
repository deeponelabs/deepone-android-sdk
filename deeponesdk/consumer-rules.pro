# DeepOneSDK Consumer ProGuard Rules
# Applied automatically to apps using the DeepOneSDK

# =============================================================================
# PRESERVE PUBLIC SDK API FOR CONSUMERS
# =============================================================================

# Keep all public SDK classes without obfuscation
-keep public class io.deepone.sdk.** {
    public *;
}

# =============================================================================
# PRESERVE ANDROID APIS USED BY SDK
# =============================================================================

# Keep Context methods needed for API key retrieval
-keepclassmembers class android.content.Context {
    android.content.pm.PackageManager getPackageManager();
    android.content.Context getApplicationContext();
}

# Keep PackageManager methods for metadata access
-keepclassmembers class android.content.pm.PackageManager {
    android.content.pm.ApplicationInfo getApplicationInfo(java.lang.String, int);
}

# Keep ApplicationInfo for meta-data access
-keepclassmembers class android.content.pm.ApplicationInfo {
    android.os.Bundle metaData;
}

# =============================================================================
# KOTLIN SUPPORT
# =============================================================================

# Keep Kotlin metadata for proper function signatures
-keepattributes *Annotation*

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# =============================================================================
# WARNING SUPPRESSIONS
# =============================================================================

# Suppress warnings for dependencies
-dontwarn io.deepone.sdk.**
-dontwarn io.deepone.networking.**
