package com.taskmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskmaster.databinding.FragmentTaskDetailBinding
import com.taskmaster.data.entity.Task
import com.taskmaster.ui.adapter.SubtaskAdapter
import com.taskmaster.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var subtaskAdapter: SubtaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subtaskAdapter = SubtaskAdapter { task, checked ->
            if (checked) taskViewModel.completeTask(task)
        }
        binding.recyclerSubtasks.apply {
            adapter = subtaskAdapter
            layoutManager = LinearLayoutManager(context)
        }
        val task = requireArguments().getParcelable<Task>("task") ?: return
        binding.textTitle.text = task.title
        binding.textDescription.text = task.description
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val subs = taskViewModel.getSubtasks(task.id)
            subtaskAdapter.submitList(subs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
