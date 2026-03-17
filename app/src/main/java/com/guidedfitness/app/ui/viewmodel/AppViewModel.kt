package com.guidedfitness.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guidedfitness.app.data.model.Exercise
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.repository.UserRepository
import com.guidedfitness.app.data.local.AppDatabase
import com.guidedfitness.app.data.remote.youtube.YoutubePlaylistScrapeClient
import com.guidedfitness.app.data.remote.FirestoreSyncRepository
import com.guidedfitness.app.data.repository.local.DefaultPlanSeeder
import com.guidedfitness.app.data.repository.local.LocalPlanRepository
import com.guidedfitness.app.data.repository.local.LocalMonthlyPlanRepository
import com.guidedfitness.app.data.repository.local.MonthlyVideo
import com.guidedfitness.app.data.repository.local.LocalProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository(application)
    private val db = AppDatabase.getInstance(application)
    private val syncRepo = FirestoreSyncRepository(db)
    private val progressRepo = LocalProgressRepository(db.progressDao())
    private val playlistScraper = YoutubePlaylistScrapeClient()

    private fun normalizeUserId(phone: String): String =
        phone.filter { it.isDigit() }.ifBlank { phone.trim() }

    private val userIdFlow = userRepo.userPhone.map { phone ->
        phone?.let { normalizeUserId(it) } ?: "local"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "local"
    )

    private fun planRepo(userId: String): LocalPlanRepository =
        LocalPlanRepository(
            userId = userId,
            planDao = db.planDao(),
            exerciseDao = db.exerciseDao(),
            seeder = DefaultPlanSeeder(db.planDao(), db.exerciseDao())
        )

    private fun monthlyRepo(userId: String): LocalMonthlyPlanRepository =
        LocalMonthlyPlanRepository(
            userId = userId,
            dao = db.monthlyPlanDao()
        )

    val userName = userRepo.userName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val userPhone = userRepo.userPhone.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val planMetadata = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            viewModelScope.launch { planRepo(userId).ensureSeeded() }
            planRepo(userId).observeMetadata()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val weeklyPlan = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            viewModelScope.launch { planRepo(userId).ensureSeeded() }
            planRepo(userId).getWeeklyPlan()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyPlan = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId ->
            viewModelScope.launch { monthlyRepo(userId).ensureSeeded(30) }
            monthlyRepo(userId).observeMonthlyPlan()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    data class DraftPlaylistVideo(
        val videoId: String,
        val title: String,
        val description: String,
        val thumbnailUrl: String?,
        val assignedDay: WorkoutDay
    )

    sealed class PlaylistImportState {
        data object Idle : PlaylistImportState()
        data object Loading : PlaylistImportState()
        data class Error(val message: String) : PlaylistImportState()
        data class Preview(
            val playlistUrl: String,
            val items: List<DraftPlaylistVideo>
        ) : PlaylistImportState()

        data class Success(val savedCount: Int) : PlaylistImportState()
    }

    val playlistImportState = MutableStateFlow<PlaylistImportState>(PlaylistImportState.Idle)

    enum class ImportPlanType { WEEKLY, MONTHLY }

    fun resetPlaylistImportState() {
        playlistImportState.value = PlaylistImportState.Idle
    }

    fun importFromYoutubePlaylist(url: String, type: ImportPlanType) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            playlistImportState.value = PlaylistImportState.Loading

            // Non-API import is currently supported for WEEKLY only.
            if (type != ImportPlanType.WEEKLY) {
                playlistImportState.value = PlaylistImportState.Error("Monthly import without API isn’t supported yet.")
                return@launch
            }

            val result = withContext(Dispatchers.IO) {
                playlistScraper.fetchPlaylistVideoIds(url)
            }

            when (result) {
                is YoutubePlaylistScrapeClient.Result.Error -> {
                    playlistImportState.value = PlaylistImportState.Error(result.message)
                }
                is YoutubePlaylistScrapeClient.Result.Success -> {
                    val ids = result.videoIds
                    val orderedDays = weeklyDaysSundayFirst()
                    val assigned = distributeIdsSundayFirst(ids, orderedDays)

                    val items = assigned.mapIndexed { idx, pair ->
                        val (day, videoId) = pair
                        DraftPlaylistVideo(
                            videoId = videoId,
                            title = "Video ${idx + 1}",
                            description = "",
                            thumbnailUrl = runCatching { playlistScraper.videoThumbnailUrl(videoId) }.getOrNull(),
                            assignedDay = day
                        )
                    }
                    playlistImportState.value = PlaylistImportState.Preview(
                        playlistUrl = result.playlistUrl,
                        items = items
                    )
                }
            }
        }
    }

    fun updateDraftVideo(index: Int, updated: DraftPlaylistVideo) {
        val s = playlistImportState.value
        if (s !is PlaylistImportState.Preview) return
        if (index !in s.items.indices) return
        val next = s.items.toMutableList()
        next[index] = updated
        playlistImportState.value = s.copy(items = next)
    }

    fun removeDraftVideo(index: Int) {
        val s = playlistImportState.value
        if (s !is PlaylistImportState.Preview) return
        if (index !in s.items.indices) return
        val next = s.items.toMutableList()
        next.removeAt(index)
        playlistImportState.value = s.copy(items = next)
    }

    fun moveDraftVideo(fromIndex: Int, toIndex: Int) {
        val s = playlistImportState.value
        if (s !is PlaylistImportState.Preview) return
        if (fromIndex !in s.items.indices) return
        if (toIndex !in s.items.indices) return
        if (fromIndex == toIndex) return
        val next = s.items.toMutableList()
        val item = next.removeAt(fromIndex)
        next.add(toIndex, item)
        playlistImportState.value = s.copy(items = next)
    }

    fun addDraftVideo(videoUrlOrId: String) {
        val s = playlistImportState.value
        if (s !is PlaylistImportState.Preview) return
        val videoId = extractYoutubeVideoId(videoUrlOrId) ?: return
        val next = s.items.toMutableList()
        val day = weeklyDaysSundayFirst().firstOrNull() ?: WorkoutDay.MONDAY
        next.add(
            DraftPlaylistVideo(
                videoId = videoId,
                title = "Video ${next.size + 1}",
                description = "",
                thumbnailUrl = playlistScraper.videoThumbnailUrl(videoId),
                assignedDay = day
            )
        )
        playlistImportState.value = s.copy(items = next)
    }

    fun saveDraftToWeeklyPlan() {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            val s = playlistImportState.value
            if (s !is PlaylistImportState.Preview) return@launch
            planRepo(userId).ensureSeeded()

            val grouped = s.items.groupBy { it.assignedDay }
            weeklyDaysSundayFirst().forEach { day ->
                val list = grouped[day].orEmpty()
                planRepo(userId).replaceExercises(
                    day = day,
                    exercises = list.map { it.toExercise(playlistScraper) }
                )
            }

            syncRepo.syncUp(userId)
            playlistImportState.value = PlaylistImportState.Success(s.items.size)
        }
    }

    private suspend fun applyMonthlyPlaylist(userId: String, videos: List<Any>, days: Int) {
        val repo = monthlyRepo(userId)
        // Legacy API-based path removed; keep method signature to avoid broad refactors.
        val chunks = emptyList<List<Any>>()
        for (i in 1..days) {
            val list = chunks.getOrElse(i - 1) { emptyList() }
            repo.replaceDayVideos(
                dayIndex = i,
                videos = emptyList()
            )
        }
    }

    private fun DraftPlaylistVideo.toExercise(scraper: YoutubePlaylistScrapeClient): Exercise =
        Exercise(
            id = "",
            name = title.ifBlank { "Video" },
            description = description,
            durationSeconds = 0,
            restSeconds = 0,
            imageResId = null,
            imageUrl = thumbnailUrl,
            youtubeLink = scraper.videoWatchUrl(videoId)
        )

    private fun weeklyDaysSundayFirst(): List<WorkoutDay> =
        listOf(
            WorkoutDay.SUNDAY,
            WorkoutDay.MONDAY,
            WorkoutDay.TUESDAY,
            WorkoutDay.WEDNESDAY,
            WorkoutDay.THURSDAY,
            WorkoutDay.FRIDAY,
            WorkoutDay.SATURDAY
        )

    private fun distributeIdsSundayFirst(ids: List<String>, days: List<WorkoutDay>): List<Pair<WorkoutDay, String>> {
        if (days.isEmpty()) return emptyList()
        val out = ArrayList<Pair<WorkoutDay, String>>(ids.size)
        ids.forEachIndexed { idx, id ->
            out += days[idx % days.size] to id
        }
        return out
    }

    private fun extractYoutubeVideoId(urlOrId: String): String? {
        val t = urlOrId.trim()
        if (t.length == 11 && Regex("""[A-Za-z0-9_-]{11}""").matches(t)) return t
        val url = t
        val vParam = Regex("""[?&]v=([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)
        if (!vParam.isNullOrBlank()) return vParam
        val short = Regex("""youtu\.be/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)
        if (!short.isNullOrBlank()) return short
        val embed = Regex("""/embed/([A-Za-z0-9_-]{11})""").find(url)?.groupValues?.getOrNull(1)
        if (!embed.isNullOrBlank()) return embed
        return null
    }

    private fun distributePreservingOrder(
        videos: List<PlaylistVideo>,
        daysCount: Int
    ): List<List<PlaylistVideo>> {
        if (daysCount <= 0) return emptyList()
        if (videos.isEmpty()) return List(daysCount) { emptyList() }

        val total = videos.size
        val base = total / daysCount
        val remainder = total % daysCount
        val raw = (0 until daysCount).map { i -> base + if (i < remainder) 1 else 0 }.toMutableList()

        // Nudge toward 2..5 when possible, but never drop/duplicate videos.
        var safety = 0
        while (raw.any { it < 2 } && raw.any { it > 5 } && safety < 1000) {
            safety += 1
            val from = raw.indexOfFirst { it > 5 }.takeIf { it >= 0 } ?: break
            val to = raw.indexOfFirst { it < 2 }.takeIf { it >= 0 } ?: break
            raw[from] -= 1
            raw[to] += 1
        }
        // If we can, raise <2 up to 2 by borrowing from >2.
        safety = 0
        while (raw.any { it < 2 } && raw.any { it > 2 } && safety < 2000) {
            safety += 1
            val to = raw.indexOfFirst { it < 2 }.takeIf { it >= 0 } ?: break
            val from = raw.indexOfFirst { it > 2 }.takeIf { it >= 0 } ?: break
            raw[from] -= 1
            raw[to] += 1
        }
        // If still some days <2, playlist is too small; we'll accept 0/1 on the tail.

        val out = ArrayList<List<PlaylistVideo>>(daysCount)
        var cursor = 0
        for (i in 0 until daysCount) {
            val n = raw[i].coerceAtLeast(0)
            val end = (cursor + n).coerceAtMost(total)
            out += videos.subList(cursor, end)
            cursor = end
        }
        // Any remaining (due to rounding) append to last day.
        if (cursor < total && out.isNotEmpty()) {
            val last = out.last().toMutableList()
            last += videos.subList(cursor, total)
            out[out.lastIndex] = last
        }
        return out
    }

    private val logsFlow = userIdFlow
        .filterNotNull()
        .flatMapLatest { userId -> progressRepo.observeLogs(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSessions = progressRepo.totalSessions(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalMinutes = progressRepo.totalMinutes(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val streak = progressRepo.currentStreak(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val longestStreak = progressRepo.longestStreak(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val weeklyCompletionPercent = progressRepo.weeklyCompletionPercent(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val breathingSessions = progressRepo.breathingSessions(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val workoutSessions = progressRepo.workoutSessions(logsFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyMinutesSeries = progressRepo.dailyMinutesSeries(logsFlow, daysBack = 14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyMinutesSeries = progressRepo.weeklyMinutesSeries(logsFlow, weeksBack = 12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyMinutesSeries = progressRepo.monthlyMinutesSeries(logsFlow, monthsBack = 12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val yearlyMinutesSeries = progressRepo.yearlyMinutesSeries(logsFlow, yearsBack = 5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasProfile = userRepo.hasProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun getDayWorkout(day: WorkoutDay) =
        userIdFlow.filterNotNull().flatMapLatest { userId ->
            planRepo(userId).getDayWorkout(day)
        }

    fun getMonthlyDay(dayIndex: Int) =
        userIdFlow.filterNotNull().flatMapLatest { userId ->
            monthlyRepo(userId).observeDay(dayIndex)
        }

    fun recordWorkoutCompletion(day: WorkoutDay, focus: DayFocus, minutes: Int) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            progressRepo.recordCompletion(userId, day, focus, minutes)
            syncRepo.syncUp(userId)
        }
    }

    suspend fun upsertProfile(name: String, phone: String) {
        userRepo.upsertProfile(name, phone)
        val userId = normalizeUserId(phone)
        db.userDao().upsert(
            com.guidedfitness.app.data.local.entity.UserEntity(
                userId = userId,
                name = name.trim(),
                phone = phone.trim()
            )
        )
        planRepo(userId).ensureSeeded()
        // Try restore from cloud; safe no-op if Firebase not configured.
        syncRepo.syncDown(userId)
        // Push current local state back to cloud.
        syncRepo.syncUp(userId)
    }

    fun setYoutubeVideoId(day: WorkoutDay, videoId: String?) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).setYoutubeVideoId(day, videoId)
            syncRepo.syncUp(userId)
        }
    }

    fun addExercise(day: WorkoutDay, exercise: Exercise) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).addExercise(day, exercise)
            syncRepo.syncUp(userId)
        }
    }

    fun removeExercise(day: WorkoutDay, exerciseId: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).removeExercise(day, exerciseId)
            syncRepo.syncUp(userId)
        }
    }

    fun reorderExercises(day: WorkoutDay, orderedExerciseIds: List<String>) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).reorderExercises(day, orderedExerciseIds)
            syncRepo.syncUp(userId)
        }
    }

    fun addMonthlyVideo(dayIndex: Int, title: String, url: String, thumbnailUrl: String? = null) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            monthlyRepo(userId).addVideo(
                dayIndex = dayIndex,
                video = MonthlyVideo(
                    id = "",
                    title = title.trim(),
                    thumbnailUrl = thumbnailUrl,
                    videoUrl = url.trim()
                )
            )
            syncRepo.syncUp(userId)
        }
    }

    fun removeMonthlyVideo(videoId: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            monthlyRepo(userId).removeVideo(videoId)
            syncRepo.syncUp(userId)
        }
    }

    fun reorderMonthlyVideos(dayIndex: Int, orderedVideoIds: List<String>) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            monthlyRepo(userId).reorderDayVideos(dayIndex, orderedVideoIds)
            syncRepo.syncUp(userId)
        }
    }

    fun updateDayFocus(day: WorkoutDay, focus: DayFocus) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).updateDayFocus(day, focus)
            syncRepo.syncUp(userId)
        }
    }

    fun updatePlanMetadata(title: String, description: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            planRepo(userId).updateMetadata(title, description)
            syncRepo.syncUp(userId)
        }
    }

    fun updateDayIcon(day: WorkoutDay, iconKey: String) {
        viewModelScope.launch {
            val userId = userIdFlow.value ?: return@launch
            // Update by rewriting current focus + youtube with new icon
            val current = db.planDao().getDayWorkout("${userId}_${day.name}")
            val now = System.currentTimeMillis()
            db.planDao().upsertDayWorkout(
                com.guidedfitness.app.data.local.entity.DayWorkoutEntity(
                    workoutId = "${userId}_${day.name}",
                    userId = userId,
                    day = day.name,
                    focus = current?.focus ?: day.focus.name,
                    iconKey = iconKey,
                    youtubeVideoId = current?.youtubeVideoId,
                    updatedAt = now
                )
            )
            syncRepo.syncUp(userId)
        }
    }
}
