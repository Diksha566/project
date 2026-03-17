package com.guidedfitness.app.data.remote.youtube

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class YoutubePlaylistClient(
    private val apiKey: String,
    private val http: OkHttpClient = OkHttpClient()
) {
    sealed class Result {
        data class Success(val videos: List<PlaylistVideo>) : Result()
        data class Error(val message: String) : Result()
    }

    fun parsePlaylistId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return null
        // Accept raw playlist ID too (e.g. "PLxxxx")
        if (!trimmed.contains("http", ignoreCase = true) && trimmed.startsWith("PL")) return trimmed

        val url = trimmed.toHttpUrlOrNull() ?: return null
        return url.queryParameter("list")
            ?: url.pathSegments.firstOrNull { it.startsWith("PL") }
    }

    suspend fun fetchAllPlaylistVideos(playlistId: String): Result {
        if (apiKey.isBlank()) return Result.Error("Missing YouTube API key.")

        val videos = mutableListOf<PlaylistVideo>()
        var pageToken: String? = null
        var loops = 0

        while (loops < 50) { // hard stop to avoid infinite loops
            loops += 1
            val url = "https://www.googleapis.com/youtube/v3/playlistItems"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("part", "snippet")
                ?.addQueryParameter("maxResults", "50")
                ?.addQueryParameter("playlistId", playlistId)
                ?.addQueryParameter("key", apiKey)
                ?.apply { if (pageToken != null) addQueryParameter("pageToken", pageToken) }
                ?.build()
                ?: return Result.Error("Failed to build request.")

            val req = Request.Builder().url(url).get().build()
            val resp = http.newCall(req).execute()
            val isSuccessful = resp.isSuccessful
            val body = resp.use { it.body?.string().orEmpty() }

            if (!isSuccessful) {
                // Common cases: private playlist, quota exceeded, invalid key.
                val msg = runCatching {
                    val err = JSONObject(body).optJSONObject("error")
                    val errors = err?.optJSONArray("errors")
                    val reason = errors?.optJSONObject(0)?.optString("reason")
                    when (reason) {
                        "playlistNotFound" -> "Playlist not found or private."
                        "quotaExceeded" -> "YouTube API quota exceeded."
                        "keyInvalid", "forbidden" -> "YouTube API key is invalid or not authorized."
                        else -> err?.optString("message") ?: "YouTube request failed."
                    }
                }.getOrNull() ?: "YouTube request failed."
                return Result.Error(msg)
            }

            val json = JSONObject(body)
            val items = json.optJSONArray("items")
            if (items != null) {
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val snippet = item.optJSONObject("snippet") ?: continue
                    val title = snippet.optString("title").orEmpty()
                    val resourceId = snippet.optJSONObject("resourceId")
                    val videoId = resourceId?.optString("videoId").orEmpty()
                    if (title.isBlank() || videoId.isBlank()) continue

                    val thumbs = snippet.optJSONObject("thumbnails")
                    val thumbUrl =
                        thumbs?.optJSONObject("medium")?.optString("url")
                            ?: thumbs?.optJSONObject("default")?.optString("url")

                    videos += PlaylistVideo(
                        title = title,
                        thumbnailUrl = thumbUrl,
                        videoUrl = "https://www.youtube.com/watch?v=$videoId"
                    )
                }
            }

            pageToken = json.optString("nextPageToken").takeIf { it.isNotBlank() }
            if (pageToken == null) break
        }

        if (videos.isEmpty()) return Result.Error("No videos found. The playlist may be empty or private.")
        return Result.Success(videos)
    }
}

