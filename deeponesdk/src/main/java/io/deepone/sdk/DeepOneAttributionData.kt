package io.deepone.sdk

import android.net.Uri

/**
 * Attribution data model providing type-safe access to attribution information
 * Contains deep link routing data, user session details, and marketing attribution
 */
public class DeepOneAttributionData(private val rawData: Map<String, Any>) {
    
    // MARK: - Core Attribution Properties
    
    /**
     * The original URL that triggered the attribution
     */
    val originURL: String? = rawData[DeepOne.ATTRIBUTION_PARAMETER_ORIGIN_URL] as? String

    /**
     * The host component of the attribution URL
     */
    var routeHost: String? = rawData[DeepOne.ATTRIBUTION_PARAMETER_ROUTE_HOST] as? String

    /**
     * The path component of the attribution URL
     */
    val routePath: String? = rawData[DeepOne.ATTRIBUTION_PARAMETER_ROUTE_PATH] as? String
    
    /**
     * Whether this is the user's first session ever (persists across app reinstalls)
     */
    val isFirstSession: Boolean = (rawData[DeepOne.ATTRIBUTION_PARAMETER_IS_FIRST_SESSION] as? Boolean) ?: false
    
    /**
     * All query parameters from the attribution URL
     */
    val queryParameters: Map<String, String> by lazy {
        @Suppress("UNCHECKED_CAST")
        (rawData[DeepOne.ATTRIBUTION_PARAMETER_QUERY_PARAMETERS] as? Map<String, String>) ?: emptyMap()
    }
    
    // MARK: - Marketing Attribution Properties
    
    private val marketingData: Map<String, Any> by lazy {
        @Suppress("UNCHECKED_CAST")
        (rawData[DeepOne.ATTRIBUTION_PARAMETER_ATTRIBUTION_DATA] as? Map<String, Any>) ?: emptyMap()
    }
    
    /**
     * UTM source parameter
     */
    val marketingSource: String? = marketingData["utm_source"] as? String
    
    /**
     * UTM medium parameter
     */
    val marketingMedium: String? = marketingData["utm_medium"] as? String
    
    /**
     * UTM campaign parameter
     */
    val marketingCampaign: String? = marketingData["utm_campaign"] as? String
    
    /**
     * UTM term parameter
     */
    val marketingTerm: String? = marketingData["utm_term"] as? String
    
    /**
     * UTM content parameter
     */
    val marketingContent: String? = marketingData["utm_content"] as? String
    
    /**
     * Custom referrer parameter
     */
    val referrer: String? = marketingData["referrer"] as? String
    
    /**
     * Campaign identifier
     */
    val campaignIdentifier: String? = marketingData["campaign_identifier"] as? String
    
    // MARK: - Convenience Methods
    
    /**
     * Checks if this attribution contains marketing data
     */
    val hasMarketingData: Boolean
        get() = marketingSource != null || marketingMedium != null || marketingCampaign != null
    
    /**
     * Checks if this attribution contains UTM parameters
     */
    val hasUTMParameters: Boolean
        get() = marketingSource != null || marketingMedium != null || marketingCampaign != null || 
                marketingTerm != null || marketingContent != null
    
    /**
     * Gets a custom parameter value
     */
    fun customParameter(key: String): Any? = rawData[key]
    
    /**
     * Gets a custom string parameter
     */
    fun customStringParameter(key: String): String? = rawData[key] as? String
    
    /**
     * Gets all UTM parameters as a map
     */
    val utmParameters: Map<String, String>
        get() {
            val utm = mutableMapOf<String, String>()
            marketingSource?.let { utm["utm_source"] = it }
            marketingMedium?.let { utm["utm_medium"] = it }
            marketingCampaign?.let { utm["utm_campaign"] = it }
            marketingTerm?.let { utm["utm_term"] = it }
            marketingContent?.let { utm["utm_content"] = it }
            return utm
        }
    
    /**
     * URL of the attributed link
     */
    val url: Uri?
        get() = originURL?.let { Uri.parse(it) }
    
    /**
     * Checks if the attribution indicates a specific route
     */
    fun matches(route: String): Boolean = routePath == route
    
    /**
     * Checks if the attribution has a route with a specific prefix
     */
    fun hasRoute(withPrefix: String): Boolean = routePath?.startsWith(withPrefix) ?: false
    
    /**
     * Extracts an ID from a route path (e.g., "/product/123" -> "123")
     */
    fun extractID(fromRoute: String): String? {
        return if (routePath?.startsWith(fromRoute) == true) {
            routePath?.substring(fromRoute.length)
        } else {
            null
        }
    }
    
    override fun toString(): String {
        val components = mutableListOf<String>()
        
        originURL?.let { components.add("originURL: $it") }
        routePath?.let { components.add("routePath: $it") }
        components.add("isFirstSession: $isFirstSession")
        
        if (hasMarketingData) {
            val marketingComponents = mutableListOf<String>()
            marketingSource?.let { marketingComponents.add("source: $it") }
            marketingCampaign?.let { marketingComponents.add("campaign: $it") }
            components.add("marketing: [${marketingComponents.joinToString(", ")}]")
        }
        
        return "DeepOneAttributionData(${components.joinToString(", ")})"
    }
}

