package com.example.codevaultide.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown

/**
 * GitHub-style Markdown preview.
 *
 * Handles GitHub's linked-image syntax:
 *
 * [![Video Walkthrough](video-walkthrough-thumbnail.jpg)](https://www.youtube.com/watch?v=K4I9quiJ3Gc)
 *
 * If the image is a local file and the link is YouTube, the local image is
 * replaced with YouTube's public thumbnail URL. The outer YouTube link stays
 * intact, so tapping the thumbnail still opens YouTube.
 */
@Composable
fun MarkdownPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    Markdown(
        content = prepareMarkdownForPreview(content),
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        imageTransformer = Coil3ImageTransformerImpl
    )
}

/**
 * Converts nested GitHub image links into normal Markdown that the renderer
 * can parse reliably.
 *
 * Examples:
 *
 * [![Video](thumbnail.jpg)](https://www.youtube.com/watch?v=ABC123)
 * ->
 * [![Video](https://i.ytimg.com/vi/ABC123/hqdefault.jpg)](https://www.youtube.com/watch?v=ABC123)
 *
 * [![Video](https://example.com/thumb.jpg)](https://example.com/page)
 * ->
 * unchanged
 */
private fun prepareMarkdownForPreview(markdown: String): String {
    // 1. Match complete nested GitHub image-link constructs
    val linkedImageRegex = Regex(
        """\[\!\[([^\]]*)\]\(([^)\r\n]+)\)\]\((https?://[^)\r\n]+)\)"""
    )

    var processed = markdown.replace(linkedImageRegex) { match ->
        val alt = match.groupValues[1]
        val imageUrl = match.groupValues[2].trim()
        val targetUrl = match.groupValues[3].trim()

        val youtubeId = extractYouTubeVideoId(targetUrl)

        if (youtubeId != null && !imageUrl.startsWith("http://") &&
            !imageUrl.startsWith("https://")
        ) {
            val thumbnail = "https://img.youtube.com/vi/$youtubeId/mqdefault.jpg"
            "[![$alt]($thumbnail)]($targetUrl)"
        } else {
            match.value
        }
    }

    // 2. Convert standard Markdown links [Title](https://youtube.com/...) into thumbnails
    val markdownLinkYoutubeRegex = Regex(
        """\[([^\]]+)\]\((https?://(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/|youtube\.com/embed/)([A-Za-z0-9_-]{11})[^\s\r\n\)]*)\)"""
    )

    processed = processed.replace(markdownLinkYoutubeRegex) { match ->
        val title = match.groupValues[1]
        val url = match.groupValues[2]
        val youtubeId = match.groupValues[3]
        val thumbnail = "https://img.youtube.com/vi/$youtubeId/mqdefault.jpg"
        "\n\n### $title\n[![$title]($thumbnail)]($url)\n\n"
    }

    // 3. Convert raw YouTube video links into thumbnails
    val rawYoutubeRegex = Regex(
        """(?<!\()(https?://(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/|youtube\.com/embed/)([A-Za-z0-9_-]{11})[^\s\r\n\)]*)(?!\))"""
    )

    processed = processed.replace(rawYoutubeRegex) { match ->
        val url = match.groupValues[1]
        val youtubeId = match.groupValues[2]
        val thumbnail = "https://img.youtube.com/vi/$youtubeId/mqdefault.jpg"
        "\n\n[![$url]($thumbnail)]($url)\n\n"
    }

    // 4. Handle Playlists and Channels with generic YouTube icons
    val channelOrPlaylistRegex = Regex(
        """(?<!\()(https?://(?:www\.)?youtube\.com/(?:@|playlist\?list=)[A-Za-z0-9_-]+[^\s\r\n\)]*)(?!\))"""
    )

    processed = processed.replace(channelOrPlaylistRegex) { match ->
        val url = match.groupValues[1]
        val placeholder = "https://www.youtube.com/img/desktop/yt_1200.png"
        "\n\n[![$url]($placeholder)]($url)\n\n"
    }

    return processed
}

/**
 * Supports:
 * https://www.youtube.com/watch?v=VIDEO_ID
 * https://youtube.com/watch?v=VIDEO_ID
 * https://youtu.be/VIDEO_ID
 * https://www.youtube.com/shorts/VIDEO_ID
 * https://youtube.com/embed/VIDEO_ID
 */
private fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        Regex("""(?:youtube\.com/watch\?[^#\s]*v=)([A-Za-z0-9_-]{6,})"""),
        Regex("""(?:youtu\.be/)([A-Za-z0-9_-]{6,})"""),
        Regex("""(?:youtube\.com/shorts/)([A-Za-z0-9_-]{6,})"""),
        Regex("""(?:youtube\.com/embed/)([A-Za-z0-9_-]{6,})""")
    )

    return patterns.firstNotNullOfOrNull { pattern ->
        pattern.find(url)?.groupValues?.getOrNull(1)
    }
}
