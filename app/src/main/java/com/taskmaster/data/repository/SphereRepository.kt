package com.taskmaster.data.repository

import androidx.lifecycle.LiveData
import com.taskmaster.data.dao.SphereDao
import com.taskmaster.data.entity.Sphere
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SphereRepository @Inject constructor(
    private val sphereDao: SphereDao
) {
    fun getAllSpheres(): LiveData<List<Sphere>> = sphereDao.getAllSpheres()

    suspend fun insertSphere(sphere: Sphere): Long = sphereDao.insertSphere(sphere)

    suspend fun updateSphere(sphere: Sphere) = sphereDao.updateSphere(sphere)

    suspend fun deleteSphere(sphere: Sphere) = sphereDao.deleteSphere(sphere)

    suspend fun addXpToSphere(sphereId: Long, xp: Int) {
        // Простая реализация - можно улучшить позже
    }

    suspend fun insertDefaultSpheres() {
        val default = listOf(
            Sphere(name = "🏋️ Спорт", color = "#4CAF50", icon = "fitness", order = 0),
            Sphere(name = "📚 Обучение", color = "#2196F3", icon = "education", order = 1),
            Sphere(name = "💼 Работа", color = "#9C27B0", icon = "work", order = 2),
            Sphere(name = "🏠 Быт", color = "#FF9800", icon = "home", order = 3),
            Sphere(name = "❤️ Семья", color = "#E91E63", icon = "family", order = 4)
        )
        sphereDao.insertSpheres(default)
    }
}
