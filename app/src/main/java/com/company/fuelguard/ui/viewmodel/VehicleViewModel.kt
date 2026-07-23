package com.company.fuelguard.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.company.fuelguard.data.local.AppDatabase
import com.company.fuelguard.data.local.model.Vehicle
import com.company.fuelguard.data.repository.VehicleRepositoryImpl
import com.company.fuelguard.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.*

class VehicleViewModel(vehicleRepository: VehicleRepository) : ViewModel() {
    val vehicles: StateFlow<List<Vehicle>> = vehicleRepository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class VehicleViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            val repo = VehicleRepositoryImpl(AppDatabase.getDatabase(application).vehicleDao())
            return VehicleViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
