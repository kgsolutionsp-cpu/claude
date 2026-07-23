package com.company.fuelguard.data.repository

import com.company.fuelguard.data.local.dao.RefuelRecordDao
import com.company.fuelguard.data.local.model.RefuelRecord
import com.company.fuelguard.domain.repository.RefuelRepository
import java.util.Calendar

class RefuelRepositoryImpl(private val refuelRecordDao: RefuelRecordDao) : RefuelRepository {
    override suspend fun addRecord(record: RefuelRecord): Long = refuelRecordDao.insertRecord(record)
    override suspend fun getLatestRecordForVehicle(vehicleId: Int): RefuelRecord? = refuelRecordDao.getLatestRecordForVehicle(vehicleId)

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    override suspend fun getWeeklyRefillCount(vehicleId: Int): Int = refuelRecordDao.getRecordCountForVehicleSince(vehicleId, getStartOfWeek())
    override suspend fun getMonthlyRefillCount(vehicleId: Int): Int = refuelRecordDao.getRecordCountForVehicleSince(vehicleId, getStartOfMonth())
}
