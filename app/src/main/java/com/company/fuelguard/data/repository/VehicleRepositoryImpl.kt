package com.company.fuelguard.data.repository

import com.company.fuelguard.data.local.dao.VehicleDao
import com.company.fuelguard.data.local.model.Vehicle
import com.company.fuelguard.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class VehicleRepositoryImpl(private val vehicleDao: VehicleDao) : VehicleRepository {
    override fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllActiveVehicles()
    override suspend fun getVehicleById(id: Int): Vehicle? = vehicleDao.getVehicleById(id)
}
