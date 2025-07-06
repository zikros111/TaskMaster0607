package com.taskmaster.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskmaster.databinding.DialogTasksByDateBinding
import com.taskmaster.ui.adapter.TaskAdapter
import com.taskmaster.ui.adapter.TaskWithSubtasks
import com.taskmaster.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class DateTasksDialogFragment : DialogFragment() {
    private var _binding: DialogTasksByDateBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by activityViewModels()
    private lateinit var adapter: TaskAdapter
    private var date: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        date = arguments?.getSerializable(ARG_DATE) as? Date ?: Date()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogTasksByDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskAdapter(
            onTaskClick = { task -> },
            onCompleteClick = { task -> taskViewModel.completeTask(task) },
            onPostponeClick = { task -> taskViewModel.postponeTask(task) },
            onDeleteClick = { task -> taskViewModel.deleteTask(task) }
        )
        binding.recyclerView.apply {
            adapter = this@DateTasksDialogFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
        taskViewModel.getTasksByDate(date).observe(viewLifecycleOwner) { tasks ->
            viewLifecycleOwner.lifecycleScope.launch {
                val list = tasks.map { parent ->
                    val subs = taskViewModel.getSubtasks(parent.id)
                    TaskWithSubtasks(parent, subs)
                }
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_DATE = "arg_date"
        fun newInstance(date: Date) = DateTasksDialogFragment().apply {
            arguments = Bundle().apply { putSerializable(ARG_DATE, date) }
        }
    }
}
