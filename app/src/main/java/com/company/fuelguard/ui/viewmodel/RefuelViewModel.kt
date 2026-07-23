package com.company.fuelguard.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.company.fuelguard.data.local.AppDatabase
import com.company.fuelguard.data.local.model.*
import com.company.fuelguard.data.repository.*
import com.company.fuelguard.domain.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RefuelUiState(
    val vehicle: Vehicle? = null,
    val lastRecord: RefuelRecord? = null,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val odometer: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class RefuelViewModel(
    private val vehicleId: Int,
    private val vehicleRepository: VehicleRepository,
    private val refuelRepository: RefuelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RefuelUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val vehicle = vehicleRepository.getVehicleById(vehicleId) ?: throw Exception("Vehicle not found.")
                _uiState.update { it.copy(
                    vehicle = vehicle,
                    lastRecord = refuelRepository.getLatestRecordForVehicle(vehicleId),
                    weeklyCount = refuelRepository.getWeeklyRefillCount(vehicleId),
                    monthlyCount = refuelRepository.getMonthlyRefillCount(vehicleId),
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onOdometerChanged(value: String) {
        if (value.all { it.isDigit() }) _uiState.update { it.copy(odometer = value) }
    }
}

class RefuelViewModelFactory(private val application: Application, private val vehicleId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RefuelViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            return RefuelViewModel(vehicleId, VehicleRepositoryImpl(db.vehicleDao()), RefuelRepositoryImpl(db.refuelRecordDao())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
