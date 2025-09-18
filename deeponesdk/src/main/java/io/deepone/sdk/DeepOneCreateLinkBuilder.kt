package io.deepone.sdk

/**
 * Configuration builder for creating attributed deep links with marketing parameters
 * Provides a fluent interface for setting up deep link attribution and routing
 */
public class DeepOneCreateLinkBuilder(
    val destinationPath: String,
    val linkIdentifier: String
) {
    // MARK: - Content Properties
    var linkDescription: String? = null
    var socialTitle: String? = null
    var socialDescription: String? = null
    var socialImageURL: String? = null
    
    // MARK: - Marketing Attribution
    var marketingSource: String? = null
    var marketingMedium: String? = null
    var marketingCampaign: String? = null
    var marketingTerm: String? = null
    var marketingContent: String? = null
    
    // MARK: - Custom Parameters
    private val customParameters = mutableMapOf<String, Any>()
    
    /**
     * Creates a link builder with comprehensive configuration
     */
    constructor(
        destinationPath: String,
        linkIdentifier: String,
        linkDescription: String? = null,
        socialTitle: String? = null,
        socialDescription: String? = null,
        socialImageURL: String? = null,
        marketingSource: String? = null,
        marketingMedium: String? = null,
        marketingCampaign: String? = null,
        marketingTerm: String? = null,
        marketingContent: String? = null
    ) : this(destinationPath, linkIdentifier) {
        this.linkDescription = linkDescription
        this.socialTitle = socialTitle
        this.socialDescription = socialDescription
        this.socialImageURL = socialImageURL
        this.marketingSource = marketingSource
        this.marketingMedium = marketingMedium
        this.marketingCampaign = marketingCampaign
        this.marketingTerm = marketingTerm
        this.marketingContent = marketingContent
    }
    
    /**
     * Adds a custom parameter to the link configuration
     * @param key Parameter key
     * @param value Parameter value
     * @return Self for method chaining
     */
    fun addCustomParameter(key: String, value: Any): DeepOneCreateLinkBuilder {
        customParameters[key] = value
        return this
    }
    
    /**
     * Sets social media preview content
     * @return Self for method chaining
     */
    fun setSocialPreview(title: String, description: String, imageURL: String? = null): DeepOneCreateLinkBuilder {
        socialTitle = title
        socialDescription = description
        socialImageURL = imageURL
        return this
    }
    
    /**
     * Sets social media preview content (convenience method without image URL)
     * @return Self for method chaining
     */
    fun setSocialPreview(title: String, description: String): DeepOneCreateLinkBuilder {
        return setSocialPreview(title, description, null)
    }
    
    /**
     * Sets marketing attribution parameters
     * @return Self for method chaining
     */
    fun setMarketingAttribution(
        source: String? = null,
        medium: String? = null,
        campaign: String? = null,
        term: String? = null,
        content: String? = null
    ): DeepOneCreateLinkBuilder {
        marketingSource = source
        marketingMedium = medium
        marketingCampaign = campaign
        marketingTerm = term
        marketingContent = content
        return this
    }
    
    /**
     * Builds the parameter dictionary for API submission
     * @return Map containing all link parameters, or null if invalid
     */
    fun buildParameters(): Map<String, Any>? {
        return try {
            val parameters = mutableMapOf<String, Any>()
            
            // Core parameters
            parameters["path"] = destinationPath
            parameters["name"] = linkIdentifier
            
            // Optional content parameters
            linkDescription?.let { parameters["description"] = it }
            socialTitle?.let { parameters["previewTitle"] = it }
            socialDescription?.let { parameters["previewDescription"] = it }
            socialImageURL?.let { parameters["previewImageUrl"] = it }
            
            // Marketing attribution parameters
            marketingSource?.let { parameters["utmSource"] = it }
            marketingMedium?.let { parameters["utmMedium"] = it }
            marketingCampaign?.let { parameters["utmCampaign"] = it }
            marketingTerm?.let { parameters["utmTerm"] = it }
            marketingContent?.let { parameters["utmContent"] = it }
            
            // Add custom parameters
            parameters.putAll(customParameters)
            
            parameters
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Legacy method for backward compatibility
     * @return Map containing all link parameters, or null if invalid
     */
    fun toDic(): Map<String, Any>? = buildParameters()
    
    /**
     * Validates the builder configuration
     * @return true if configuration is valid
     */
    fun isValid(): Boolean {
        return destinationPath.isNotEmpty() && linkIdentifier.isNotEmpty()
    }
    
    override fun toString(): String {
        return "DeepOneCreateLinkBuilder(destinationPath='$destinationPath', linkIdentifier='$linkIdentifier')"
    }
}

