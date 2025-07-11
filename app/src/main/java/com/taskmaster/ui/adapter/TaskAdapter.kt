package com.taskmaster.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskmaster.R
import com.taskmaster.data.entity.Task
import com.taskmaster.databinding.ItemTaskBinding
import com.taskmaster.ui.adapter.TaskWithSubtasks
import com.taskmaster.utils.PriorityUtils
import java.util.Date

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCompleteClick: (Task) -> Unit,
    private val onPostponeClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<TaskWithSubtasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskWithSubtasks) {
            val task = item.task
            binding.apply {
                textTitle.text = task.title
                textDescription.text = task.description
                if (item.subtasks.isNotEmpty()) {
                    textSubtasks.visibility = View.VISIBLE
                    textSubtasks.text = item.subtasks.joinToString("\n") { "• ${it.title}" }
                } else {
                    textSubtasks.visibility = View.GONE
                }
                textPriority.text = "Приоритет: ${task.priority}"
                textXp.text = "+${task.xpReward} XP"

                // ИСПРАВЛЕННОЕ ПРИМЕНЕНИЕ ЦВЕТА ПРИОРИТЕТА
                // Используем setBackgroundColor вместо setColorFilter,
                // так как priorityIndicator это View, а не ImageView
                val priorityColor = PriorityUtils.getPriorityColor(task.priority)
                val colorInt = ContextCompat.getColor(root.context, priorityColor)
                priorityIndicator.setBackgroundColor(colorInt)

                // Set click listeners
                root.setOnLongClickListener { onTaskClick(task); true }
                buttonComplete.setOnClickListener { onCompleteClick(task) }
                buttonPostpone.setOnClickListener { onPostponeClick(task) }
                buttonDelete.setOnClickListener { onDeleteClick(task) }

                if (task.isCompleted) {
                    root.strokeColor = ContextCompat.getColor(root.context, R.color.success)
                } else if (task.dueDate != null && task.dueDate.before(Date())) {
                    root.strokeColor = ContextCompat.getColor(root.context, R.color.warning)
                } else {
                    root.strokeColor = ContextCompat.getColor(root.context, android.R.color.transparent)
                }
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<TaskWithSubtasks>() {
        override fun areItemsTheSame(oldItem: TaskWithSubtasks, newItem: TaskWithSubtasks): Boolean {
            return oldItem.task.id == newItem.task.id
        }

        override fun areContentsTheSame(oldItem: TaskWithSubtasks, newItem: TaskWithSubtasks): Boolean {
            return oldItem.task == newItem.task && oldItem.subtasks == newItem.subtasks
        }
    }
}