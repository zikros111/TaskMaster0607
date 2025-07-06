package com.taskmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskmaster.R
import com.taskmaster.databinding.FragmentDateTasksBinding
import com.taskmaster.ui.adapter.TaskAdapter
import com.taskmaster.ui.adapter.TaskWithSubtasks
import com.taskmaster.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DateTasksFragment : Fragment() {

    private var _binding: FragmentDateTasksBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDateTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = Bundle().apply { putParcelable("task", task) }
                findNavController().navigate(R.id.taskDetailFragment, bundle)
            },
            onCompleteClick = { task -> taskViewModel.completeTask(task) },
            onPostponeClick = { task -> taskViewModel.postponeTask(task) },
            onDeleteClick = { task -> taskViewModel.deleteTask(task) }
        )
        binding.recyclerView.apply {
            adapter = this@DateTasksFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
        val date = requireArguments().getSerializable("date") as? java.util.Date ?: return
        taskViewModel.getTasksByDate(date).observe(viewLifecycleOwner) { tasks ->
            viewLifecycleOwner.lifecycleScope.launch {
                val list = tasks.map { parent ->
                    val subs = taskViewModel.getSubtasks(parent.id)
                    TaskWithSubtasks(parent, subs)
                }
                adapter.submitList(list)
            }
        }
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
