package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"
    val targetDateOrPeriod: String, // e.g. "2026-05-21" for DAILY, "2026-W21" for WEEKLY, "2026-05" for MONTHLY, "2026-Q2" for QUARTERLY, "2026" for YEARLY
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val aiEnhanced: Boolean = false,
    val originalGoalId: Int? = null,
    val courseCorrectionAdvice: String? = null
)
