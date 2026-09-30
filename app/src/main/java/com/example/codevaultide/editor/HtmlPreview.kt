package com.example.codevaultide.editor

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * HTML Preview for CodeVault IDE.
 *
 * Supports:
 * - HTML
 * - CSS
 * - JavaScript
 * - Images
 * - Links
 * - Forms
 * - Responsive layouts
 * - Local HTML rendering
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {

            setBackgroundColor(Color.WHITE)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true

                allowFileAccess = true
                allowContentAccess = true

                builtInZoomControls = false
                displayZoomControls = false

                loadWithOverviewMode = false
                useWideViewPort = true

                javaScriptCanOpenWindowsAutomatically = true
                mediaPlaybackRequiresUserGesture = false
            }

            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }

            webChromeClient = WebChromeClient()
        }
    }

    AndroidView(
        factory = {
            webView
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->

            val html = buildHtmlDocument(content)

            view.loadDataWithBaseURL(
                "https://codevault.local/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    )

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        }
    }
}

/* Creates a complete HTML document if the user enters only an HTML fragment.
 */
private fun buildHtmlDocument(content: String): String {

    val trimmed = content.trim()

    // If the user already has a complete HTML document,
    // return it with a viewport meta tag when possible.
    if (
        trimmed.contains("<html", ignoreCase = true)
    ) {

        if (
            trimmed.contains(
                "<meta name=\"viewport\"",
                ignoreCase = true
            )
        ) {
            return trimmed
        }

        val headStart = Regex(
            "<head[^>]*>",
            RegexOption.IGNORE_CASE
        ).find(trimmed)

        if (headStart != null) {

            val insertPosition = headStart.range.last + 1

            return buildString {
                append(trimmed.substring(0, insertPosition))

                append(
                    """
                    <meta
                        name="viewport"
                        content="width=device-width, initial-scale=1.0"
                    >
                    """.trimIndent()
                )

                append(
                    trimmed.substring(insertPosition)
                )
            }
        }

        return trimmed
    }

    // HTML fragment.
    return """
        <!DOCTYPE html>
        <html>
        <head>

            <meta charset="UTF-8">

            <meta
                name="viewport"
                content="width=device-width, initial-scale=1.0"
            >

            <style>

                html,
                body {
                    margin: 0;
                    padding: 0;
                    min-height: 100%;
                }

                body {
                    box-sizing: border-box;
                    font-family:
                        system-ui,
                        -apple-system,
                        BlinkMacSystemFont,
                        "Segoe UI",
                        Roboto,
                        sans-serif;

                    padding: 16px;
                    line-height: 1.5;

                    overflow-wrap: anywhere;
                    word-break: break-word;

                    color: #111111;
                    background: #ffffff;
                }

                *,
                *::before,
                *::after {
                    box-sizing: border-box;
                }

                img {
                    max-width: 100%;
                    height: auto;
                }

                video {
                    max-width: 100%;
                    height: auto;
                }

                iframe {
                    max-width: 100%;
                }

                pre {
                    max-width: 100%;
                    overflow-x: auto;
                    white-space: pre-wrap;
                    word-break: break-word;
                }

                code {
                    overflow-wrap: anywhere;
                }

                table {
                    max-width: 100%;
                    overflow-x: auto;
                    display: block;
                    border-collapse: collapse;
                }

                input,
                textarea,
                select,
                button {
                    max-width: 100%;
                }

            </style>

        </head>

        <body>

            $content

        </body>
        </html>
    """.trimIndent()
}