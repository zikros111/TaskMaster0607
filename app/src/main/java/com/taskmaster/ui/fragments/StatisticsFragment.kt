package com.taskmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.taskmaster.databinding.FragmentStatisticsBinding
import com.taskmaster.ui.viewmodel.TaskViewModel
import com.taskmaster.ui.viewmodel.SphereViewModel
import com.taskmaster.utils.XpCalculator
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val sphereViewModel: SphereViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            updateStatistics(tasks)
        }
        sphereViewModel.allSpheres.observe(viewLifecycleOwner) { spheres ->
            taskViewModel.allTasks.value?.let { tasks ->
                updateSphereStats(spheres, tasks)
            }
        }
    }

    private fun updateStatistics(tasks: List<com.taskmaster.data.entity.Task>) {
        val completedTasks = tasks.count { it.isCompleted }
        val totalTasks = tasks.size
        val completionRate = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

        val calendar = Calendar.getInstance()
        val end = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val start = calendar.time
        val tasksLastWeek = tasks.filter { task ->
            task.dueDate?.let { it.after(start) && it.before(end) } == true
        }
        val avg = if (tasksLastWeek.isNotEmpty()) tasksLastWeek.size / 7f else 0f

        binding.apply {
            textCompletedTasks.text = completedTasks.toString()
            textTotalTasks.text = totalTasks.toString()
            textCompletionRate.text = "$completionRate%"
            textAverageTasks.text = String.format("%.1f", avg)

        }
    }

    private fun updateSphereStats(
        spheres: List<com.taskmaster.data.entity.Sphere>,
        tasks: List<com.taskmaster.data.entity.Task>
    ) {
        binding.containerSpheres.removeAllViews()
        spheres.forEach { sphere ->
            val xp = tasks.filter { it.isCompleted && it.sphereId == sphere.id }
                .sumOf { it.xpReward }
            val level = XpCalculator.calculateLevelFromXp(xp)
            val xpForLevel = xp - (level - 1) * 100
            val xpNeeded = XpCalculator.getXpForNextLevel(level)

            val item = layoutInflater.inflate(R.layout.item_sphere_stat, binding.containerSpheres, false)
            val name = item.findViewById<android.widget.TextView>(R.id.text_name)
            val progress = item.findViewById<android.widget.ProgressBar>(R.id.progress)
            val levelText = item.findViewById<android.widget.TextView>(R.id.text_level)
            name.text = sphere.name
            progress.max = xpNeeded
            progress.progress = xpForLevel
            levelText.text = "Уровень $level"
            binding.containerSpheres.addView(item)


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
