package com.taskmaster.ui.adapter

import com.taskmaster.data.entity.Task

data class TaskWithSubtasks(
    val task: Task,
    val subtasks: List<Task> = emptyList()
)
