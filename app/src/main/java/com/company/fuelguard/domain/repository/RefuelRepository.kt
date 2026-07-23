package com.company.fuelguard.domain.repository

import com.company.fuelguard.data.local.model.RefuelRecord

interface RefuelRepository {
    suspend fun addRecord(record: RefuelRecord): Long
    suspend fun getLatestRecordForVehicle(vehicleId: Int): RefuelRecord?
    suspend fun getWeeklyRefillCount(vehicleId: Int): Int
    suspend fun getMonthlyRefillCount(vehicleId: Int): Int
}
