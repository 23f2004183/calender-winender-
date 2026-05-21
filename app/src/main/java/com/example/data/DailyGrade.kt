package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_grades")
data class DailyGrade(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val grade: String, // "A", "B", "C", "D", "F"
    val score: Int, // 1 to 10
    val note: String // User's self-reflection note
)
