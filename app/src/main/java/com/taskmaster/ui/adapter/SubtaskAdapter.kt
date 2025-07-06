package com.taskmaster.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskmaster.data.entity.Task
import com.taskmaster.databinding.ItemSubtaskBinding

class SubtaskAdapter(
    private val onChecked: (Task, Boolean) -> Unit
) : ListAdapter<Task, SubtaskAdapter.SubtaskViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val binding = ItemSubtaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubtaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubtaskViewHolder(private val binding: ItemSubtaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.textSubtaskTitle.text = task.title
            binding.checkSubtask.isChecked = task.isCompleted
            binding.checkSubtask.setOnCheckedChangeListener(null)
            binding.checkSubtask.setOnCheckedChangeListener { _, isChecked ->
                onChecked(task, isChecked)
            }
        }
    }

    private class Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
