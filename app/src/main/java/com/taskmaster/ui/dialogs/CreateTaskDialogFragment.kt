package com.taskmaster.ui.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.taskmaster.R
import com.taskmaster.data.entity.Sphere
import com.taskmaster.data.entity.Task
import com.taskmaster.databinding.DialogCreateTaskBinding
import com.taskmaster.ui.viewmodel.SphereViewModel
import com.taskmaster.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateTaskDialogFragment : DialogFragment() {

    private var _binding: DialogCreateTaskBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by activityViewModels()
    private val sphereViewModel: SphereViewModel by activityViewModels()

    private var selectedDate = Date()
    private var selectedTime: Date? = null
    private var spheres = listOf<Sphere>()
    private var selectedSphere: Sphere? = null
    private var editingTask: Task? = null
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            editingTask = bundle.getParcelable(ARG_TASK)
            isEditMode = editingTask != null
            editingTask?.let { task ->
                selectedDate = task.dueDate ?: Date()
            }
        }
    }

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupSpinners()
        setupDateTimePickers()
        setupObservers()
        setupClickListeners()
    }

    private fun setupDialog() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupSpinners() {
        // Приоритеты
        val priorities = listOf(
            "1 - Очень низкий",
            "2 - Низкий",
            "3 - Ниже среднего",
            "4 - Средний-",
            "5 - Средний",
            "6 - Средний+",
            "7 - Выше среднего",
            "8 - Высокий",
            "9 - Очень высокий",
            "10 - Критический"
        )

        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            priorities
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = priorityAdapter
        binding.spinnerPriority.setSelection(4) // По умолчанию "5 - Средний"

        // Типы задач
        val taskTypes = listOf(
            "Простая задача",
            "Сложная задача (с подзадачами)",
            "Привычка",
            "Напоминание"
        )

        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskTypes
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTaskType.adapter = typeAdapter

        // Показать/скрыть поле подзадач при смене типа
        binding.spinnerTaskType.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.layoutSubtasks.visibility = if (position == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupDateTimePickers() {
        binding.buttonSelectDate.text = dateFormat.format(selectedDate)

        binding.buttonSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSelectTime.setOnClickListener {
            showTimePicker()
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.buttonSelectTime.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupObservers() {
        sphereViewModel.allSpheres.observe(this) { sphereList ->
            spheres = sphereList
            setupSphereSpinner()
            populateEditingTask()
        }
    }

    private fun setupSphereSpinner() {
        val sphereNames = spheres.map { it.name }
        val sphereAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sphereNames
        )
        sphereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSphere.adapter = sphereAdapter

        if (spheres.isNotEmpty()) {
            selectedSphere = spheres[0]
        }

        binding.spinnerSphere.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSphere = spheres.getOrNull(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupClickListeners() {
        binding.buttonCreate.setOnClickListener {
            saveTask()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun populateEditingTask() {
        if (!isEditMode) return
        editingTask?.let { task ->
            binding.editTextTitle.setText(task.title)
            binding.editTextDescription.setText(task.description)
            binding.spinnerPriority.setSelection(task.priority - 1)
            binding.buttonSelectDate.text = dateFormat.format(task.dueDate ?: Date())
            selectedSphere = spheres.find { it.id == task.sphereId }
            val index = spheres.indexOfFirst { it.id == task.sphereId }
            if (index >= 0) binding.spinnerSphere.setSelection(index)
            binding.buttonCreate.text = "Сохранить"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                binding.buttonSelectDate.text = dateFormat.format(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedTime = calendar.time
                binding.buttonSelectTime.text = "Напомнить в ${timeFormat.format(selectedTime!!)}"
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItemPosition + 1
        val sphereId = selectedSphere?.id ?: 1L
        val isComplexTask = binding.spinnerTaskType.selectedItemPosition == 1

        if (title.isEmpty()) {
            Snackbar.make(binding.root, "Введите название задачи", Snackbar.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (isEditMode) {
                    editingTask?.let { task ->
                        val updated = task.copy(
                            title = title,
                            description = description,
                            priority = priority,
                            sphereId = sphereId,
                            dueDate = selectedDate
                        )
                        taskViewModel.updateTask(updated)
                    }
                } else {
                    if (isComplexTask) {
                        val subtasksText = binding.editTextSubtasks.text.toString().trim()
                        createComplexTask(title, description, priority, sphereId, subtasksText)
                    } else {
                        taskViewModel.createTask(
                            title = title,
                            description = description,
                            priority = priority,
                            sphereId = sphereId,
                            dueDate = selectedDate
                        )
                    }
                }
                Snackbar.make(binding.root, if (isEditMode) "Задача обновлена!" else "Задача создана!", Snackbar.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Ошибка сохранения задачи", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun createComplexTask(
        title: String,
        description: String,
        priority: Int,
        sphereId: Long,
        subtasksText: String
    ) {
        // Создаем главную задачу
        val mainTaskId = taskViewModel.createTaskAndGetId(
            title = title,
            description = description,
            priority = priority,
            sphereId = sphereId,
            dueDate = selectedDate
        )

// Создаем подзадачи
        if (subtasksText.isNotEmpty()) {
            val subtasks = subtasksText.split("\n").filter { it.trim().isNotEmpty() }
            subtasks.forEach { subtaskTitle ->
                taskViewModel.createSubtask(
                    title = subtaskTitle.trim(),
                    parentTaskId = mainTaskId,
                    priority = priority,
                    sphereId = sphereId,
                    dueDate = selectedDate
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TASK = "arg_task"

        fun newInstance(task: Task? = null) = CreateTaskDialogFragment().apply {
            task?.let {
                arguments = Bundle().apply { putParcelable(ARG_TASK, it) }
            }
        }
    }
}