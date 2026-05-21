package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    // --- Goals Queries ---
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoalsFlow(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE type = :type AND targetDateOrPeriod = :period ORDER BY id DESC")
    fun getGoalsByPeriodFlow(type: String, period: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE type = :type AND targetDateOrPeriod = :period")
    suspend fun getGoalsByPeriodSync(type: String, period: String): List<Goal>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Int): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Int)

    // --- Daily Grades Queries ---
    @Query("SELECT * FROM daily_grades WHERE date = :date")
    fun getDailyGradeFlow(date: String): Flow<DailyGrade?>

    @Query("SELECT * FROM daily_grades WHERE date = :date")
    suspend fun getDailyGradeSync(date: String): DailyGrade?

    @Query("SELECT * FROM daily_grades ORDER BY date DESC")
    fun getAllDailyGradesFlow(): Flow<List<DailyGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyGrade(dailyGrade: DailyGrade)

    @Query("DELETE FROM daily_grades WHERE date = :date")
    suspend fun deleteDailyGradeByDate(date: String)

    // --- Schedule Task Queries ---
    @Query("SELECT * FROM schedule_tasks WHERE date = :date ORDER BY startTime ASC")
    fun getScheduleTasksByDateFlow(date: String): Flow<List<ScheduleTask>>

    @Query("SELECT * FROM schedule_tasks WHERE date = :date ORDER BY startTime ASC")
    suspend fun getScheduleTasksByDateSync(date: String): List<ScheduleTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleTask(task: ScheduleTask): Long

    @Update
    suspend fun updateScheduleTask(task: ScheduleTask)

    @Query("DELETE FROM schedule_tasks WHERE id = :id")
    suspend fun deleteScheduleTaskById(id: Int)
}
