package com.taskmaster.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Int = 5,
    val sphereId: Long = 1,
    val complexity: Int = 1,
    val orderIndex: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val dueDate: Date? = null,
    val completedAt: Date? = null,
    val xpReward: Int = 0,
    val parentTaskId: Long? = null
) : Parcelable
