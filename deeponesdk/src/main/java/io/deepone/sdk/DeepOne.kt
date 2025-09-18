package io.deepone.sdk

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.deepone.networking.DeepOneNetworking
import io.deepone.networking.DeepOneConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handler for processing incoming deep link attribution with type-safe data model
 */
typealias AttributionHandler = (attributionData: DeepOneAttributionData?, error: Exception?) -> Unit

/**
 * DeepOne provides comprehensive deep linking and deferred deep linking capabilities for Android applications.
 * This modern SDK offers attribution tracking, link generation, and seamless user experience management.
 * Main entry point for all attribution and deep linking functionality.
 */
class DeepOne private constructor() {
    
    // MARK: - Attribution Parameters
    companion object {
        /**
         * Parameter keys for routing and attribution information
         */
        const val ATTRIBUTION_PARAMETER_ORIGIN_URL = DeepOneConfig.PARAM_ORIGIN_URL
        const val ATTRIBUTION_PARAMETER_ROUTE_HOST = DeepOneConfig.PARAM_ROUTE_HOST
        const val ATTRIBUTION_PARAMETER_ROUTE_PATH = DeepOneConfig.PARAM_ROUTE_PATH
        const val ATTRIBUTION_PARAMETER_QUERY_PARAMETERS = DeepOneConfig.PARAM_QUERY_PARAMETERS
        const val ATTRIBUTION_PARAMETER_IS_FIRST_SESSION = DeepOneConfig.PARAM_IS_FIRST_SESSION
        const val ATTRIBUTION_PARAMETER_ATTRIBUTION_DATA = DeepOneConfig.PARAM_ATTRIBUTION_DATA
        
        /**
         * Error domain for MyBrand operations
         */
        const val MY_BRAND_ERROR_DOMAIN = DeepOneConfig.ERROR_DOMAIN
        
        /**
         * Error codes for MyBrand operations
         */
        object ErrorCode {
            const val INVALID_CONFIGURATION = 1001
            const val NETWORK_ERROR = 1002
            const val ATTRIBUTION_FAILED = 1003
            const val INVALID_URL = 1004
            const val MISSING_CREDENTIALS = 1005
        }
        
        /**
         * Singleton instance
         */
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        val shared = DeepOne()
    }
    
    
    // MARK: - Properties
    private var isDevelopmentMode = false
    private var attributionHandler: AttributionHandler? = null
    private var context: Context? = null
    private var lifecycleCallbacks: ActivityLifecycleMonitor? = null
    private var isMonitoringEnabled = false
    private var hasProcessedIntent = false
    
    /**
     * API credentials for link verification and generation
     */
    private fun getApiKey(): String? {
        val context = this.context ?: return null
        
        return try {
            // Use application context for metadata access (more reliable)
            val appContext = context.applicationContext
            val appInfo = appContext.packageManager.getApplicationInfo(
                appContext.packageName, 
                android.content.pm.PackageManager.GET_META_DATA
            )
            
            val metaData = appInfo.metaData ?: return null
               val keyName = if (isDevelopmentMode) DeepOneConfig.TEST_API_KEY_NAME else DeepOneConfig.LIVE_API_KEY_NAME
            
            metaData.getString(keyName)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Tracks if this is the first session ever (persists across reinstalls via secure storage)
     */
    private var isFirstSession: Boolean
        get() = DeepOneNetworking.getSecureData(DeepOneConfig.FIRST_SESSION_MARKER_KEY, DeepOneConfig.DEFAULT_STORAGE_GROUP) == null
        set(value) {
            if (!value) {
                DeepOneNetworking.setSecureData(byteArrayOf(), DeepOneConfig.FIRST_SESSION_MARKER_KEY, DeepOneConfig.DEFAULT_STORAGE_GROUP)
            } else {
                DeepOneNetworking.deleteSecureData(DeepOneConfig.FIRST_SESSION_MARKER_KEY, DeepOneConfig.DEFAULT_STORAGE_GROUP)
            }
        }
    
    /**
     * Configures the MyBrand SDK with application context and attribution handling.
     *
     * @param context The application or activity context
     * @param developmentMode Enable development mode for testing. Default is false.
     * @param attributionHandler Closure to handle incoming attribution data
     */
    public fun configure(
        context: Context,
        developmentMode: Boolean = false,
        attributionHandler: AttributionHandler?
    ) {
        
        this.context = context
        this.isDevelopmentMode = developmentMode
        this.attributionHandler = attributionHandler
        
        // Initialize the core API with the original context (for UI operations)
        DeepOneNetworking.initialize(context)
        
        // Enable automatic Activity monitoring for Intent processing
        val application = context.applicationContext as? Application
        if (application != null) {
            enableActivityMonitoring(application)
        }
        
        // Check for immediate Intent processing (if Activity context provided)
        if (context is Activity) {
            val intent = context.intent
            if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
                // Immediate intent processing for configure() called from Activity with app link
                hasProcessedIntent = true
                processUniversalLink(intent)
                return // Skip server attribution since we have app link data
            }
        }
        
        // Perform immediate server-side attribution analysis if no app link detected
        performAttributionAnalysis { url, error ->
            CoroutineScope(Dispatchers.Main).launch {
                if (error != null) {
                    attributionHandler?.invoke(null, error)
                } else {
                    processAttributionData(url)
                }
            }
        }
    }
    
    /**
     * Creates a new attributed link with the specified configuration
     *
     * @param configuration Link configuration containing routing and attribution parameters
     * @param completion Completion handler with the generated URL or error
     */
    public fun createAttributedLink(
        configuration: DeepOneCreateLinkBuilder,
        completion: (url: String?, error: Exception?) -> Unit
    ) {
        val parameters = configuration.buildParameters()
        if (parameters == null) {
            val error = Exception("Invalid link configuration provided")
            completion(null, error)
            return
        }
        
        val currentApiKey = getApiKey()
        if (currentApiKey.isNullOrEmpty()) {
            val error = Exception("Missing API credentials in configuration")
            completion(null, error)
            return
        }

        DeepOneNetworking.createLink(parameters, currentApiKey) { result ->
            CoroutineScope(Dispatchers.Main).launch {
                result.fold(
                    onSuccess = { url ->
                        completion(url, null)
                    },
                    onFailure = { error ->
                        completion(null, Exception("Network error: ${error.message}", error))
                    }
                )
            }
        }
    }
    
    /**
     * Processes universal link intent for attribution
     *
     * @param intent The Intent from universal link handling
     * @return Boolean indicating successful attribution processing
     */
    public fun processUniversalLink(intent: Intent): Boolean {
        val data = intent.data ?: return false
        return processAttributionData(data)
    }
    
    /**
     * Clears all persisted attribution data (resets first session tracking)
     */
    public fun clearAttributionData() {
        DeepOneNetworking.deleteSecureData(DeepOneConfig.FIRST_SESSION_MARKER_KEY, DeepOneConfig.DEFAULT_STORAGE_GROUP)
    }
    
    /**
     * Processes an incoming URL for attribution tracking
     *
     * @param url The URL to process for attribution
     * @return Boolean indicating successful processing
     */
    public fun trackAttributionURL(url: Uri): Boolean {
        return processAttributionData(url)
    }
    
    /**
     * Processes an incoming URL string for attribution tracking
     *
     * @param urlString The URL string to process for attribution
     * @return Boolean indicating successful processing
     */
    public fun trackAttributionURL(urlString: String): Boolean {
        return try {
            val uri = Uri.parse(urlString)
            processAttributionData(uri)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun processAttributionData(url: Uri?): Boolean {
        val attributionData = mutableMapOf<String, Any>()
        
        // Mark first session status before changing it
        val currentIsFirstSession = isFirstSession
        attributionData[ATTRIBUTION_PARAMETER_IS_FIRST_SESSION] = currentIsFirstSession
        
        // Update first session flag
        if (currentIsFirstSession) {
            isFirstSession = false
        }
        
        if (url != null) {
            attributionData[ATTRIBUTION_PARAMETER_ORIGIN_URL] = url.toString()
            attributionData[ATTRIBUTION_PARAMETER_ROUTE_HOST] = url.host ?: ""
            attributionData[ATTRIBUTION_PARAMETER_ROUTE_PATH] = url.path ?: ""

            // Extract query parameters
            val queryParams = mutableMapOf<String, String>()
            url.queryParameterNames.forEach { name ->
                url.getQueryParameter(name)?.let { value ->
                    queryParams[name] = value
                }
            }
            attributionData[ATTRIBUTION_PARAMETER_QUERY_PARAMETERS] = queryParams
            
            // Extract marketing attribution data
            val marketingData = extractMarketingAttribution(queryParams)
            if (marketingData.isNotEmpty()) {
                attributionData[ATTRIBUTION_PARAMETER_ATTRIBUTION_DATA] = marketingData
            }
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            val attributionObject = DeepOneAttributionData(attributionData)
            attributionHandler?.invoke(attributionObject, null)
        }
        
        return url != null
    }
    
    private fun performAttributionAnalysis(callback: (url: Uri?, error: Exception?) -> Unit) {
        val currentContext = context
        if (currentContext == null) {
            callback(null, Exception("Context not initialized"))
            return
        }
        
        val currentApiKey = getApiKey()
        if (currentApiKey.isNullOrEmpty()) {
            callback(null, Exception("Missing API credentials"))
            return
        }
        
        val deviceFingerprint = getDeviceFingerprint(currentContext)

        DeepOneNetworking.verify(deviceFingerprint, currentApiKey) { result ->
            result.fold(
                onSuccess = { response ->
                    // Update first session status if provided by server
                    (response["isFirstSession"] as? Boolean)?.let { serverFirstSession ->
                        isFirstSession = serverFirstSession
                    }
                    
                    val linkString = response["link"] as? String
                    val attributedURL = if (!linkString.isNullOrEmpty()) {
                        Uri.parse(linkString)
                    } else {
                        null
                    }
                    callback(attributedURL, null)
                },
                onFailure = { error ->
                    callback(null, Exception("Attribution analysis failed: ${error.message}", error))
                }
            )
        }
    }
    
    /**
     * Extracts marketing attribution parameters from query parameters
     */
    private fun extractMarketingAttribution(queryParams: Map<String, String>): Map<String, Any> {
        val marketingData = mutableMapOf<String, Any>()
        
        // UTM parameters
        val utmKeys = listOf("utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content")
        for (key in utmKeys) {
            queryParams[key]?.let { value ->
                marketingData[key] = value
            }
        }
        
        // Custom attribution parameters
        queryParams["ref"]?.let { referrer ->
            marketingData["referrer"] = referrer
        }
        
        queryParams["campaign_id"]?.let { campaign ->
            marketingData["campaign_identifier"] = campaign
        }
        
        return marketingData
    }
    
    /**
     * Mark that an Intent has been processed (internal use)
     */
    internal fun markIntentProcessed() {
        hasProcessedIntent = true
    }
    
    /**
     * Check if an Intent has been processed (internal use)
     */
    internal fun hasProcessedIntent(): Boolean {
        return hasProcessedIntent
    }
    
    /**
     * Enable automatic Activity lifecycle monitoring for intent processing
     * This automatically captures app links without requiring manual integration
     */
    private fun enableActivityMonitoring(application: Application) {
        if (isMonitoringEnabled) return
        
        lifecycleCallbacks = ActivityLifecycleMonitor()
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        isMonitoringEnabled = true
    }
    
    /**
     * Disable automatic Activity lifecycle monitoring
     */
    private fun disableActivityMonitoring(application: Application) {
        lifecycleCallbacks?.let { callbacks ->
            application.unregisterActivityLifecycleCallbacks(callbacks)
            lifecycleCallbacks = null
            isMonitoringEnabled = false
        }
    }
}

/**
 * Internal Activity lifecycle monitor for automatic Intent processing
 * Captures app links automatically without manual developer integration
 */
private class ActivityLifecycleMonitor : Application.ActivityLifecycleCallbacks {
    
    private var processedIntents = mutableSetOf<Intent>()
    
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Process initial intent for cold starts
        handleActivityIntent(activity, activity.intent, "onCreate")
    }
    
    override fun onActivityStarted(activity: Activity) {
        // Additional check for intents that might be missed
        handleActivityIntent(activity, activity.intent, "onStarted")
    }
    
    override fun onActivityResumed(activity: Activity) {
        // Handle any resumed intents (warm starts)
        handleActivityIntent(activity, activity.intent, "onResumed")
    }
    
    private fun handleActivityIntent(activity: Activity, intent: Intent?, source: String) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            // Avoid processing the same intent multiple times
            if (processedIntents.contains(intent)) {
                return
            }
            
            // Skip if already processed during configure()
            if (DeepOne.shared.hasProcessedIntent()) {
                return
            }
            
            processedIntents.add(intent)
            
            // Mark that we processed an intent
            DeepOne.shared.markIntentProcessed()
            
            // Process through DeepOne SDK
            DeepOne.shared.processUniversalLink(intent)
        }
    }
    
    // Required lifecycle methods (unused)
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

/**
 * Swift-style Result class for Kotlin
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: Exception) : Result<Nothing>()
    
    inline fun <R> fold(
        onSuccess: (value: T) -> R,
        onFailure: (error: Exception) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}
