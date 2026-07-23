package com.company.fuelguard.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.company.fuelguard.data.local.AppDatabase
import com.company.fuelguard.data.local.model.RefuelRecord
import com.company.fuelguard.data.repository.*
import com.company.fuelguard.domain.repository.*
import com.company.fuelguard.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class CameraUiState(
    val isProcessing: Boolean = false,
    val stampedImageUri: Uri? = null,
    val error: String? = null,
    val shareDetails: ShareDetails? = null
)

data class ShareDetails(
    val vehiclePlate: String,
    val odometer: String,
    val address: String,
    val dateTime: String,
    val refillNumber: String
)

class CameraViewModel(
    private val app: Application,
    private val vehicleRepo: VehicleRepository,
    private val refuelRepo: RefuelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    fun processImage(vehicleId: Int, odometer: Double, imageFile: File, driverName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                val vehicle = vehicleRepo.getVehicleById(vehicleId) ?: throw Exception("Vehicle not found")
                val location = GpsUtils.getCurrentLocation(app) ?: throw Exception("GPS location unavailable")
                val address = GpsUtils.getAddressFromLocation(app, location)
                val weeklyCount = refuelRepo.getWeeklyRefillCount(vehicleId) + 1
                val monthlyCount = refuelRepo.getMonthlyRefillCount(vehicleId) + 1
                val timestamp = System.currentTimeMillis()
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
                val transactionId = "FUEL-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date(timestamp))}"

                val stampInfo = StampInfo(lines = listOf(
                    date, "Plate: ${vehicle.license_plate}", "Odometer: ${odometer.toInt()} km",
                    "Fuel: ${vehicle.fuel_type.name}", "GPS: ${"%.5f".format(location.latitude)}, ${"%.5f".format(location.longitude)}",
                    "Location: $address", "Driver: $driverName", "Refill: #$weeklyCount of ${vehicle.weekly_limit} (Week)", transactionId
                ))
                val stampedUri = ImageStamper.stampImage(app, imageFile, stampInfo) ?: throw Exception("Failed to stamp image")

                refuelRepo.addRecord(RefuelRecord(
                    vehicle_id = vehicleId, driver_name = driverName, odometer_reading = odometer, fuel_liters = null,
                    fuel_type = vehicle.fuel_type, gps_lat = location.latitude, gps_lng = location.longitude,
                    location_address = address, timestamp = timestamp, photo_path = imageFile.absolutePath,
                    stamped_photo_path = stampedUri.toString(), transaction_id = transactionId, refill_number_week = weeklyCount,
                    refill_number_month = monthlyCount, consumption_at_refill = null
                ))
                _uiState.update { it.copy(isProcessing = false, stampedImageUri = stampedUri, shareDetails = ShareDetails(
                    vehicle.license_plate, "${odometer.toInt()} km", address, date, "#$weeklyCount"
                ))}
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isProcessing = false, error = e.message) }
            }
        }
    }
}

class CameraViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            return CameraViewModel(application, VehicleRepositoryImpl(db.vehicleDao()), RefuelRepositoryImpl(db.refuelRecordDao())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
