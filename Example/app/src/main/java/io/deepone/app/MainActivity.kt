package io.deepone.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.deepone.app.ui.theme.ExampleTheme
import io.deepone.sdk.DeepOne
import io.deepone.sdk.DeepOneCreateLinkBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDeepOne()
        enableEdgeToEdge()

        setContent {
            ExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    fun setupDeepOne() {
        // Initialize DeepOne SDK with attribution handler
        DeepOne.shared.configure(
            context = this, // Use Activity context for UI operations
            developmentMode = true // Set to false in production
        ) { attributionData, error ->
            if (error != null) {
                updateStatus("Attribution failed: ${error.message}")
            } else if (attributionData != null) {
                updateStatus("Attribution received!")
                updateStatus("Origin URL: ${attributionData.originURL ?: "N/A"}")
                updateStatus("Route host: ${attributionData.routeHost ?: "N/A"}")
                updateStatus("Route Path: ${attributionData.routePath ?: "N/A"}")
                updateStatus("Query Params: ${attributionData.queryParameters}")
                updateStatus("Is First Session: ${attributionData.isFirstSession}")
                if (attributionData.marketingSource != null) {
                    updateStatus("Marketing: ${attributionData.marketingSource}/${attributionData.marketingMedium}")
                }
                // Handle attribution data here
            } else {
                updateStatus("No attribution data")
            }
        }
    }

    private fun testCreateLinkFunction() {
        updateStatus("Testing createLink function...")

        // Create link configuration using builder pattern
        val linkBuilder = DeepOneCreateLinkBuilder(
            destinationPath = "/product/123",
            linkIdentifier = "example_link"
        ).setMarketingAttribution(
            source = "android_app",
            medium = "mobile",
            campaign = "example_campaign",
            content = "test_link"
        ).setSocialPreview(
            title = "Check out this product!",
            description = "Amazing product you'll love"
        )

        DeepOne.shared.createAttributedLink(linkBuilder) { url, error ->
            runOnUiThread {
                if (error != null) {
                    updateStatus("Link creation failed: ${error.message}")
                    Toast.makeText(this, "Link creation failed", Toast.LENGTH_SHORT).show()
                } else if (url != null) {
                    updateStatus("Link created successfully: $url")
                    Toast.makeText(this, "Link created!", Toast.LENGTH_SHORT).show()
                } else {
                    updateStatus("Link creation returned null")
                }
            }
        }
    }

    private fun updateStatus(message: String) {
        // log to console for debugging
        println("MainActivity: $message")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExampleTheme {
        Greeting("Android")
    }
}