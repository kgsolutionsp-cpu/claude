package com.company.fuelguard.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RefuelStatus { Pending, Approved, Rejected }

@Entity(
    tableName = "refuel_records",
    foreignKeys = [ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicle_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["vehicle_id"]), Index(value = ["transaction_id"], unique = true)]
)
data class RefuelRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicle_id: Int,
    val driver_name: String,
    val odometer_reading: Double,
    val fuel_liters: Double?,
    val fuel_type: FuelType,
    val gps_lat: Double,
    val gps_lng: Double,
    val location_address: String,
    val timestamp: Long = System.currentTimeMillis(),
    val photo_path: String,
    val stamped_photo_path: String,
    val status: RefuelStatus = RefuelStatus.Pending,
    val approval_notes: String? = null,
    val transaction_id: String,
    val refill_number_week: Int,
    val refill_number_month: Int,
    val consumption_at_refill: Double?,
    val verified_at_station: Boolean = false,
    val synced: Boolean = false
)
