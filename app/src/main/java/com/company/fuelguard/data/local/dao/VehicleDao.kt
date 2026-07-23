package com.company.fuelguard.data.local.dao

import androidx.room.*
import com.company.fuelguard.data.local.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Query("SELECT * FROM vehicles WHERE is_active = 1 ORDER BY license_plate ASC")
    fun getAllActiveVehicles(): Flow<List<Vehicle>>
}
