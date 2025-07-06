package com.taskmaster.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.taskmaster.databinding.DialogEditProfileBinding
import com.taskmaster.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileDialogFragment : DialogFragment() {

    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.currentUser.observe(this) { user ->
            user?.let {
                binding.editTextName.setText(it.name)
                binding.editTextCity.setText(it.city)
            }
        }

        binding.buttonSave.setOnClickListener { saveProfile() }
        binding.buttonCancel.setOnClickListener { dismiss() }
    }

    private fun saveProfile() {
        val name = binding.editTextName.text.toString().trim()
        val city = binding.editTextCity.text.toString().trim()
        profileViewModel.currentUser.value?.let { user ->
            profileViewModel.updateProfile(user.copy(name = name, city = city))
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditProfileDialogFragment()
    }
}
