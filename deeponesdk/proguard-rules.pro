# DeepOneSDK ProGuard Rules
# Public SDK - no obfuscation needed, keep everything readable

# =============================================================================
# PRESERVE ALL PUBLIC SDK CLASSES AND METHODS
# =============================================================================

# Keep all public SDK classes without obfuscation
-keep public class io.deepone.sdk.** {
    public *;
}

# =============================================================================
# BASIC KOTLIN SUPPORT
# =============================================================================

# Keep Kotlin metadata for proper function signatures
-keepattributes *Annotation*

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# =============================================================================
# DEPENDENCY WARNINGS
# =============================================================================

# Suppress warnings for networking dependency (will be resolved at runtime)
-dontwarn io.deepone.networking.**