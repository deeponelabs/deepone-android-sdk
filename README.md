# DeepOne Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.deepone.sdk/deeponesdk)](https://central.sonatype.com/artifact/io.deepone.sdk/deeponesdk)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

The official Android SDK for DeepOne - a powerful attribution and deep linking platform for mobile apps.

## Features

- 🔗 **Attribution Tracking** - Track user acquisition and attribution across campaigns
- 🚀 **Deep Link Generation** - Create trackable deep links with custom parameters
- 📊 **Real-time Analytics** - Monitor link performance and user behavior
- 📱 **Universal Link Support** - Handle incoming deep links automatically
- 🎯 **Campaign Attribution** - Track marketing campaigns and user sources

## Installation

### Gradle

Add the following to your app-level `build.gradle` file:

```gradle
dependencies {
    implementation 'io.deepone.sdk:deeponesdk:1.0.15'
}
```

### Kotlin DSL

```kotlin
dependencies {
    implementation("io.deepone.sdk:deeponesdk:1.0.15")
}
```

## Setup

### 1. Get Your API Keys

Get your **FREE** API keys from [https://deepone.io](https://deepone.io):
- **Test Key**: For development and testing
- **Live Key**: For production releases

### 2. Add API Keys to AndroidManifest.xml

Add your API keys to your app's `AndroidManifest.xml`:

```xml
<application>
    <!-- DeepOne API Keys -->
    <meta-data
        android:name="DeepOne.test_key"
        android:value="YOUR_TEST_API_KEY" />
    <meta-data
        android:name="DeepOne.live_key"
        android:value="YOUR_LIVE_API_KEY" />
</application>
```

### 3. Add Deep Link Intent Filters

Configure your main Activity to handle deep links:

```xml
<activity android:name=".MainActivity">
    <!-- Your existing intent filters -->
    
    <!-- Deep Link Intent Filter -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="yourapp.deepone.io" />
    </intent-filter>
</activity>
```

## Quick Start

### Initialize the SDK

```kotlin
import io.deepone.sdk.DeepOne

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize DeepOne SDK
        DeepOne.shared.configure(
            context = this,
            developmentMode = BuildConfig.DEBUG
        ) { attributionData, error ->
            if (error != null) {
                // Handle attribution error
                Log.e("DeepOne", "Attribution error: ${error.message}")
            } else if (attributionData != null) {
                // Handle successful attribution
                Log.i("DeepOne", "Attribution received: ${attributionData.originUrl}")
                
                // Access attribution data
                val campaign = attributionData.campaign
                val source = attributionData.source
                val isFirstSession = attributionData.isFirstSession
                
                // Your attribution logic here
                handleUserAttribution(attributionData)
            }
        }
    }
    
    private fun handleUserAttribution(data: io.deepone.sdk.DeepOneAttributionData) {
        // Process attribution data
        // Update UI, send to analytics, etc.
    }
}
```

### Generate Attribution Links

```kotlin
import io.deepone.sdk.DeepOneCreateLinkBuilder

// Create a link builder
val linkBuilder = DeepOneCreateLinkBuilder(
    destinationPath = "/product/123",
    linkIdentifier = "summer_campaign"
).apply {
    // Marketing parameters
    campaign = "summer_sale"
    source = "email"
    medium = "newsletter"
    
    // Social sharing
    socialTitle = "Check out this amazing product!"
    socialDescription = "Special summer discount available"
    socialImageURL = "https://yourapp.com/images/product.jpg"
    
    // Custom parameters
    addCustomParameter("product_id", "123")
    addCustomParameter("discount", "20")
}

// Generate the link
DeepOne.shared.createAttributedLink(linkBuilder) { url, error ->
    if (error != null) {
        Log.e("DeepOne", "Link creation failed: ${error.message}")
    } else if (url != null) {
        Log.i("DeepOne", "Generated link: $url")
        
        // Share the link
        shareLink(url)
    }
}
```

### Manual Link Processing

If you need to manually process deep links:

```kotlin
// Process a deep link URL
val success = DeepOne.shared.trackAttributionURL("https://yourapp.com/product/123?campaign=summer")

// Process an Intent
val intentProcessed = DeepOne.shared.processUniversalLink(intent)
```

## Advanced Configuration

### Clear Attribution Data

Reset first session tracking and clear stored attribution data:

```kotlin
DeepOne.shared.clearAttributionData()
```

### Attribution Data Model

The `DeepOneAttributionData` class provides access to all attribution information:

```kotlin
class DeepOneAttributionData {
    val originUrl: String?           // Original deep link URL
    val isFirstSession: Boolean      // True if user's first app session
    val campaign: String?           // Marketing campaign name
    val source: String?             // Traffic source (e.g., "google", "facebook")
    val medium: String?             // Marketing medium (e.g., "cpc", "email")
    val content: String?            // Ad content identifier
    val term: String?               // Paid search keyword
    val clickId: String?            // Click identifier for attribution
    val customParameters: Map<String, String> // Custom attribution parameters
}
```

## Requirements

- **Android API Level**: 23+ (Android 6.0)
- **Kotlin**: 1.8.0+
- **Compile SDK**: 34+

## Permissions

The SDK automatically includes these permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Documentation

- [API Documentation](https://docs.deepone.io/android)
- [Deep Link Guide](https://docs.deepone.io/guides/deep-links)
- [Attribution Guide](https://docs.deepone.io/guides/attribution)

## Support

- 📧 **Email**: contact@deepone.io

## License

```
MIT License

Copyright (c) 2024 DeepOne Labs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

**Get started for FREE at [deepone.io](https://deepone.io)** 🚀