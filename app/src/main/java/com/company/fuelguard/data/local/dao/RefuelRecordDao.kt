package com.company.fuelguard.data.local.dao

import androidx.room.*
import com.company.fuelguard.data.local.model.RefuelRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelRecordDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecord(record: RefuelRecord): Long

    @Query("SELECT * FROM refuel_records WHERE vehicle_id = :vehicleId ORDER BY odometer_reading DESC LIMIT 1")
    suspend fun getLatestRecordForVehicle(vehicleId: Int): RefuelRecord?

    @Query("SELECT COUNT(*) FROM refuel_records WHERE vehicle_id = :vehicleId AND timestamp >= :startTime")
    suspend fun getRecordCountForVehicleSince(vehicleId: Int, startTime: Long): Int
}
