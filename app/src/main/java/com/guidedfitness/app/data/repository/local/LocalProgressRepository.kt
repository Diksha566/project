package com.guidedfitness.app.data.repository.local

import com.guidedfitness.app.data.local.dao.ProgressDao
import com.guidedfitness.app.data.local.entity.ProgressLogEntity
import com.guidedfitness.app.data.model.DayFocus
import com.guidedfitness.app.data.model.WorkoutDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class LocalProgressRepository(
    private val progressDao: ProgressDao
) {
    fun observeLogs(userId: String): Flow<List<ProgressLogEntity>> =
        progressDao.observeAllLogs(userId)

    suspend fun recordCompletion(
        userId: String,
        day: WorkoutDay,
        focus: DayFocus,
        minutes: Int
    ) {
        val now = System.currentTimeMillis()
        val log = ProgressLogEntity(
            logId = UUID.randomUUID().toString(),
            userId = userId,
            dateEpochDay = LocalDate.now().toEpochDay(),
            day = day.name,
            focus = focus.name,
            minutes = minutes.coerceAtLeast(0),
            createdAt = now
        )
        progressDao.insert(log)
    }

    fun totalSessions(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { it.size }

    fun totalMinutes(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { it.sumOf { l -> l.minutes } }

    fun currentStreak(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { computeCurrentStreak(distinctDays(it)) }

    fun longestStreak(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { computeLongestStreak(distinctDays(it)) }

    fun weeklyCompletionPercent(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { list ->
            val days = distinctDays(list).toSet()
            val start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()
            val end = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toEpochDay()
            val completedThisWeek = (start..end).count { days.contains(it.toLong()) }
            ((completedThisWeek / 7.0) * 100.0).toInt().coerceIn(0, 100)
        }

    fun breathingSessions(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { it.count { l -> l.focus == DayFocus.BREATHING.name } }

    fun workoutSessions(logs: Flow<List<ProgressLogEntity>>): Flow<Int> =
        logs.map { it.count { l -> l.focus != DayFocus.BREATHING.name } }

    fun dailyMinutesSeries(logs: Flow<List<ProgressLogEntity>>, daysBack: Int = 14): Flow<List<Pair<Long, Int>>> =
        logs.map { list ->
            val end = LocalDate.now().toEpochDay()
            val start = end - (daysBack - 1)
            val grouped = list.groupBy { it.dateEpochDay }.mapValues { (_, v) -> v.sumOf { it.minutes } }
            (start..end).map { d -> d to (grouped[d] ?: 0) }
        }

    fun weeklyMinutesSeries(logs: Flow<List<ProgressLogEntity>>, weeksBack: Int = 12): Flow<List<Pair<Long, Int>>> =
        logs.map { list ->
            val today = LocalDate.now()
            val startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val starts = (0 until weeksBack).map { i -> startOfThisWeek.minusWeeks((weeksBack - 1 - i).toLong()) }
            val grouped = list.groupBy { log ->
                LocalDate.ofEpochDay(log.dateEpochDay).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()
            }.mapValues { (_, v) -> v.sumOf { it.minutes } }
            starts.map { d -> d.toEpochDay() to (grouped[d.toEpochDay()] ?: 0) }
        }

    fun monthlyMinutesSeries(logs: Flow<List<ProgressLogEntity>>, monthsBack: Int = 12): Flow<List<Pair<Long, Int>>> =
        logs.map { list ->
            val thisMonth = YearMonth.now()
            val months = (0 until monthsBack).map { i -> thisMonth.minusMonths((monthsBack - 1 - i).toLong()) }
            val grouped = list.groupBy { log ->
                val d = LocalDate.ofEpochDay(log.dateEpochDay)
                YearMonth.of(d.year, d.month)
            }.mapValues { (_, v) -> v.sumOf { it.minutes } }
            months.map { ym ->
                // represent month as first day epochDay for ordering
                ym.atDay(1).toEpochDay() to (grouped[ym] ?: 0)
            }
        }

    fun yearlyMinutesSeries(logs: Flow<List<ProgressLogEntity>>, yearsBack: Int = 5): Flow<List<Pair<Long, Int>>> =
        logs.map { list ->
            val thisYear = LocalDate.now().year
            val years = (thisYear - (yearsBack - 1) .. thisYear).toList()
            val grouped = list.groupBy { log -> LocalDate.ofEpochDay(log.dateEpochDay).year }
                .mapValues { (_, v) -> v.sumOf { it.minutes } }
            years.map { y ->
                LocalDate.of(y, 1, 1).toEpochDay() to (grouped[y] ?: 0)
            }
        }

    private fun distinctDays(list: List<ProgressLogEntity>): List<Long> =
        list.map { it.dateEpochDay }.distinct().sorted()

    private fun computeCurrentStreak(sortedEpochDays: List<Long>): Int {
        if (sortedEpochDays.isEmpty()) return 0
        val today = LocalDate.now().toEpochDay()
        val set = sortedEpochDays.toSet()
        // streak counts up to today; if no workout today, allow yesterday as end.
        val end = when {
            set.contains(today) -> today
            set.contains(today - 1) -> today - 1
            else -> return 0
        }
        var streak = 0
        var d = end
        while (set.contains(d)) {
            streak += 1
            d -= 1
        }
        return streak
    }

    private fun computeLongestStreak(sortedEpochDays: List<Long>): Int {
        if (sortedEpochDays.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until sortedEpochDays.size) {
            if (sortedEpochDays[i] == sortedEpochDays[i - 1] + 1) {
                current += 1
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }
}

