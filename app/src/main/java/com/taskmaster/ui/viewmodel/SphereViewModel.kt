package com.taskmaster.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.data.entity.Sphere
import com.taskmaster.data.repository.SphereRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SphereViewModel @Inject constructor(
    private val sphereRepository: SphereRepository
) : ViewModel() {

    val allSpheres: LiveData<List<Sphere>> = sphereRepository.getAllSpheres()

    fun insertDefaultSpheres() {
        viewModelScope.launch {
            sphereRepository.insertDefaultSpheres()
        }
    }
}