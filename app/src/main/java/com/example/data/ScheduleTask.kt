package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_tasks")
data class ScheduleTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val startTime: String, // "HH:MM" e.g. "09:00"
    val endTime: String, // "HH:MM" e.g. "10:30"
    val importance: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val originalStartTime: String? = null, // Store pre-optimization time to show changes
    val originalEndTime: String? = null
)
