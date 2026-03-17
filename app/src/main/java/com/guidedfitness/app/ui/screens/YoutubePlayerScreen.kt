package com.guidedfitness.app.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YoutubePlayerScreen(
    videoId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                createPlayerWebView(context, videoId)
            },
            update = { webView ->
                webView.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    buildEmbedHtml(videoId),
                    "text/html",
                    "utf-8",
                    null
                )
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createPlayerWebView(context: android.content.Context, videoId: String): WebView {
    val wv = WebView(context)
    wv.settings.javaScriptEnabled = true
    wv.settings.domStorageEnabled = true
    wv.settings.mediaPlaybackRequiresUserGesture = false
    wv.settings.cacheMode = WebSettings.LOAD_DEFAULT
    wv.webChromeClient = WebChromeClient()
    wv.loadDataWithBaseURL(
        "https://www.youtube.com",
        buildEmbedHtml(videoId),
        "text/html",
        "utf-8",
        null
    )
    return wv
}

private fun buildEmbedHtml(videoId: String): String {
    // Basic iframe embed. Works without API key.
    return """
        <!DOCTYPE html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <style>
              html, body { margin:0; padding:0; height:100%; background:#000; }
              .wrap { position:fixed; top:0; left:0; right:0; bottom:0; }
              iframe { width:100%; height:100%; border:0; }
            </style>
          </head>
          <body>
            <div class="wrap">
              <iframe
                src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&rel=0"
                allow="autoplay; encrypted-media; picture-in-picture"
                allowfullscreen>
              </iframe>
            </div>
          </body>
        </html>
    """.trimIndent()
}

