package com.guidedfitness.app.data.remote.youtube

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class YoutubePlaylistScrapeClient(
    private val http: OkHttpClient = OkHttpClient()
) {
    sealed class Result {
        data class Success(
            val playlistUrl: String,
            val videoIds: List<String>
        ) : Result()

        data class Error(val message: String) : Result()
    }

    fun normalizePlaylistUrl(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return null

        // Allow raw list id like "PLxxxx"
        if (!trimmed.contains("http", ignoreCase = true) && trimmed.startsWith("PL")) {
            return "https://www.youtube.com/playlist?list=$trimmed"
        }

        val url = trimmed.toHttpUrlOrNull() ?: return null
        val listId = url.queryParameter("list")
        return if (!listId.isNullOrBlank()) {
            "https://www.youtube.com/playlist?list=$listId"
        } else {
            // Some shared links use /playlist?list=... or /watch?v=...&list=...
            null
        }
    }

    fun extractVideoIdsFromHtml(html: String): List<String> {
        // YouTube pages often include "videoId":"XXXXXXXXXXX" in embedded JSON.
        val idRegex = Regex(""""videoId":"([A-Za-z0-9_-]{11})"""")
        val fallbackRegex = Regex("""watch\?v=([A-Za-z0-9_-]{11})""")

        val out = ArrayList<String>(256)
        fun add(id: String) {
            if (id.length == 11 && !out.contains(id)) out += id
        }

        idRegex.findAll(html).forEach { m -> add(m.groupValues[1]) }
        if (out.isEmpty()) {
            fallbackRegex.findAll(html).forEach { m -> add(m.groupValues[1]) }
        }
        return out
    }

    fun videoThumbnailUrl(videoId: String): String =
        "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

    fun videoWatchUrl(videoId: String): String =
        "https://www.youtube.com/watch?v=$videoId"

    suspend fun fetchPlaylistVideoIds(playlistUrl: String): Result {
        val url = normalizePlaylistUrl(playlistUrl) ?: return Result.Error("Invalid playlist URL.")

        val req = Request.Builder()
            .url(url)
            // Without a UA, YouTube sometimes returns minimal/blocked responses.
            .header(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
            )
            .get()
            .build()

        val body: String
        val httpCode: Int
        val ok: Boolean
        try {
            val resp = http.newCall(req).execute()
            ok = resp.isSuccessful
            httpCode = resp.code
            body = resp.use { it.body?.string().orEmpty() }
        } catch (e: IOException) {
            return Result.Error("No internet connection or network error. Please try again.")
        } catch (_: Throwable) {
            return Result.Error("Failed to load playlist. Please try again.")
        }

        if (!ok) return Result.Error("Failed to load playlist page (HTTP $httpCode).")

        val ids = extractVideoIdsFromHtml(body)
        if (ids.isEmpty()) {
            // Private playlists / consent pages / region blocks will often produce no IDs.
            return Result.Error("Couldn’t extract videos. The playlist may be private, age-restricted, or blocked.")
        }
        return Result.Success(url, ids)
    }
}

