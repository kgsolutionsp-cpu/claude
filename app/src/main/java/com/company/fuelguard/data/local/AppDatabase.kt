package com.company.fuelguard.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.company.fuelguard.data.local.dao.RefuelRecordDao
import com.company.fuelguard.data.local.dao.VehicleDao
import com.company.fuelguard.data.local.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter fun fromFuelType(value: FuelType): String = value.name
    @TypeConverter fun toFuelType(value: String): FuelType = FuelType.valueOf(value)
    @TypeConverter fun fromRefuelStatus(value: RefuelStatus): String = value.name
    @TypeConverter fun toRefuelStatus(value: String): RefuelStatus = RefuelStatus.valueOf(value)
}

@Database(entities = [Vehicle::class, RefuelRecord::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun refuelRecordDao(): RefuelRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fuelguard_database")
                .addCallback(DatabaseCallback(context))
                .build().also { INSTANCE = it }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.vehicleDao())
                }
            }
        }

        suspend fun populateDatabase(vehicleDao: VehicleDao) {
            val sampleVehicles = listOf(
                Vehicle(license_plate = "ABC-1234", model = "Toyota Hilux", fuel_type = FuelType.Diesel, avg_consumption = 12.5, tank_capacity = 80.0, station_name = "Pumangol Luanda"),
                Vehicle(license_plate = "DEF-5678", model = "Ford Ranger", fuel_type = FuelType.Diesel, avg_consumption = 11.2, tank_capacity = 70.0, station_name = "Pumangol Kilamba"),
                Vehicle(license_plate = "GHI-9012", model = "Toyota Corolla", fuel_type = FuelType.Petrol, avg_consumption = 15.8, tank_capacity = 50.0, station_name = "Pumangol Talatona")
            )
            sampleVehicles.forEach { vehicleDao.insertOrUpdateVehicle(it) }
        }
    }
}
