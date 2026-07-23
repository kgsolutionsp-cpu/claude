package com.company.fuelguard.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FuelType { Diesel, Petrol }

@Entity(tableName = "vehicles", indices = [Index(value = ["license_plate"], unique = true)])
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val license_plate: String,
    val model: String,
    val fuel_type: FuelType,
    val avg_consumption: Double,
    val tank_capacity: Double,
    val station_name: String,
    val station_lat: Double?,
    val station_lng: Double?,
    val weekly_limit: Int = 2,
    val monthly_limit: Int = 8,
    val created_at: Long = System.currentTimeMillis(),
    val is_active: Boolean = true
)
