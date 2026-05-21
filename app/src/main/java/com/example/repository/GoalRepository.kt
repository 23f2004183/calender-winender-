package com.example.repository

import com.example.data.Goal
import com.example.data.GoalDao
import com.example.data.DailyGrade
import com.example.data.ScheduleTask
import com.example.network.GeminiClient
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class GoalRepository(private val goalDao: GoalDao) {

    // --- Database Flows & Suspend functions ---
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoalsFlow()

    fun getGoalsByPeriod(type: String, period: String): Flow<List<Goal>> {
        return goalDao.getGoalsByPeriodFlow(type, period)
    }

    suspend fun insertGoal(goal: Goal): Long {
        return goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Int) {
        goalDao.deleteGoalById(id)
    }

    fun getDailyGrade(date: String): Flow<DailyGrade?> {
        return goalDao.getDailyGradeFlow(date)
    }

    suspend fun getDailyGradeSync(date: String): DailyGrade? {
        return goalDao.getDailyGradeSync(date)
    }

    val allDailyGrades: Flow<List<DailyGrade>> = goalDao.getAllDailyGradesFlow()

    suspend fun saveDailyGrade(dailyGrade: DailyGrade) {
        goalDao.insertDailyGrade(dailyGrade)
    }

    suspend fun deleteDailyGrade(date: String) {
        goalDao.deleteDailyGradeByDate(date)
    }

    fun getScheduleTasks(date: String): Flow<List<ScheduleTask>> {
        return goalDao.getScheduleTasksByDateFlow(date)
    }

    suspend fun insertScheduleTask(task: ScheduleTask): Long {
        return goalDao.insertScheduleTask(task)
    }

    suspend fun updateScheduleTask(task: ScheduleTask) {
        goalDao.updateScheduleTask(task)
    }

    suspend fun deleteScheduleTask(id: Int) {
        goalDao.deleteScheduleTaskById(id)
    }

    // --- AI Features ---

    /**
     * Ask Gemini to optimize the day's time-blocks based on goals and priorities.
     */
    suspend fun optimizeSchedule(date: String): String {
        val tasks = goalDao.getScheduleTasksByDateSync(date)
        val dailyGoals = goalDao.getGoalsByPeriodSync("DAILY", date)

        if (tasks.isEmpty() && dailyGoals.isEmpty()) {
            return "Unable to optimize: You do not have any scheduled tasks or daily goals defined for $date yet. Please add tasks and goals first, then run optimization!"
        }

        val tasksDescription = if (tasks.isEmpty()) {
            "No scheduled calendar time-blocks listed yet."
        } else {
            tasks.joinToString("\n") { task ->
                "- [${task.startTime} - ${task.endTime}] ${task.title} (Importance: ${task.importance}, Completed: ${task.isCompleted})"
            }
        }

        val goalsDescription = if (dailyGoals.isEmpty()) {
            "No specific daily goals added."
        } else {
            dailyGoals.joinToString("\n") { goal ->
                "- ${goal.title}: ${goal.description} (Completed: ${goal.isCompleted})"
            }
        }

        val systemInstruction = """
            You are an elite productivity scientist and time-architect AI coach. 
            Your goal is to optimize time layouts, analyze scheduling constraints, buffer against fatigue, and recommend dynamic improvements.
            Always write highly motivating, structured, and crisp markdown responses containing actionable schedule timelines, time audit insights, and energy-management recommendations. Do not use generic filler text or empty pleasantries.
        """.trimIndent()

        val prompt = """
            Please analyze and optimize my day's schedule for date: $date.
            
            **Daily Goals defined**:
            $goalsDescription
            
            **Current Time-blocked Schedule**:
            $tasksDescription
            
            Please deliver a response structured as follows:
            1. **Time-Block Audit**: Highlight overlapping items, energy drain areas, and where goals lack dedicated time.
            2. **Optimized Schedule Recommendations**: Suggest a precise reorganized timeline (with specific timestamps) protecting high-focus block for HIGH importance tasks, buffering rest periods, and grouping low importance administrative details. (Do not change the tasks themselves, just optimize their timing and order).
            3. **Daily Enhancement Tips**: Offer 2 dynamic ways to elevate performance or simplify the goals.
            
            Respond in clean, beautifully structured Markdown with readable bold sections and list bullet items.
        """.trimIndent()

        return GeminiClient.generateContent(prompt, systemInstruction)
    }

    /**
     * Asks Gemini to provide Course Correction and Goal Enhancements for incomplete goals,
     * given user reflection logs or grades.
     */
    suspend fun requestCourseCorrection(
        date: String,
        incompleteGoals: List<Goal>,
        userNote: String,
        dailyGrade: String,
        dailyScore: Int
    ): String {
        if (incompleteGoals.isEmpty()) {
            return "Outstanding job! You have completed all daily goals for $date. No course correction is required. Keep building momentum!"
        }

        val goalsStr = incompleteGoals.joinToString("\n") { goal ->
            "- [Tier: ${goal.type}] Title: ${goal.title} | Desc: ${goal.description}"
        }

        val systemInstruction = """
            You are an expert behavioral psychologist and adaptive performance coach.
            You understand that human performance is dynamic and non-linear. Instead of shaming failure, you provide supportive, diagnostic, highly structured, and realistic course-corrections.
            You always recommend realistic micro-adjustments, goal deconstruction strategies, scope reductions, and psychological tips to rebuild positive streak momentum.
        """.trimIndent()

        val prompt = """
            I am struggling to complete some goals for period: $date.
            I graded my performance as: **$dailyGrade** (Score: **$dailyScore/10**).
            My self-reflection notes: "$userNote"
            
            **Incomplete Goals**:
            $goalsStr
            
            Please deliver an actionable course-correction and dynamic goal enhancement report structured as follows:
            1. **Performance Diagnosis**: Gently analyze any blockers, scheduling friction, or over-ambition mentioned in my reflection.
            2. **Targeted Course Corrections**: For each incomplete goal, suggest a micro-adjustment (e.g. how to split it up, how to adjust scope, or where in the schedule to place it).
            3. **Dynamic Goal Enhancement**: Present a reformulated, lower-friction version of my goals for tomorrow or next week to rebuild confidence and traction.
            4. **Streak Guard Tip**: Provide an actionable routine trick to protect my daily streak.
            
            Respond in highly structured, motivational, and crisp markdown that is visually appealing and reads elegantly.
        """.trimIndent()

        return GeminiClient.generateContent(prompt, systemInstruction)
    }
}
