package com.company.fuelguard.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.company.fuelguard.ui.navigation.Screen
import com.company.fuelguard.ui.theme.WarningOrange
import com.company.fuelguard.ui.viewmodel.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefuelEntryScreen(navController: NavController, vehicleId: Int) {
    val context = LocalContext.current
    val viewModel: RefuelViewModel = viewModel(factory = RefuelViewModelFactory(context.applicationContext as Application, vehicleId))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("New Refuel Request") }, navigationIcon = { IconButton({ navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)) }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Error: ${uiState.error}") }
            uiState.vehicle != null -> RefuelEntryContent(Modifier.padding(padding), navController, uiState, viewModel::onOdometerChanged)
        }
    }
}

@Composable
fun RefuelEntryContent(modifier: Modifier, navController: NavController, uiState: RefuelUiState, onOdometerChanged: (String) -> Unit) {
    val vehicle = uiState.vehicle!!
    val lastOdometer = uiState.lastRecord?.odometer_reading ?: 0.0
    val currentOdometer = uiState.odometer.toDoubleOrNull() ?: 0.0
    val distanceSinceLast = (currentOdometer - lastOdometer).takeIf { it > 0 } ?: 0.0
    val estimatedFuelNeeded = (distanceSinceLast / vehicle.avg_consumption).takeIf { it > 0 && vehicle.avg_consumption > 0 } ?: 0.0
    val isOdometerValid = currentOdometer > lastOdometer

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(vehicle.license_plate, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(vehicle.model, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(uiState.odometer, onOdometerChanged, Modifier.fillMaxWidth(), label = { Text("Current Odometer (km)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = uiState.odometer.isNotEmpty() && !isOdometerValid, singleLine = true)
        if (uiState.odometer.isNotEmpty() && !isOdometerValid) Text("Odometer must be > ${lastOdometer.roundToInt()} km", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp).fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        InfoCard(uiState, distanceSinceLast, estimatedFuelNeeded)
        Spacer(Modifier.height(16.dp))
        Button({ navController.navigate(Screen.Camera.createRoute(vehicle.id, uiState.odometer)) }, Modifier.fillMaxWidth().height(50.dp), enabled = isOdometerValid && uiState.odometer.isNotEmpty()) { Text("Take Photo", fontSize = 18.sp) }
    }
}

@Composable
private fun InfoCard(uiState: RefuelUiState, distanceSinceLast: Double, estimatedFuelNeeded: Double) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow("Last Refill at", "${uiState.lastRecord?.odometer_reading?.roundToInt() ?: 0} km")
            InfoRow("Distance Since Last", "${distanceSinceLast.roundToInt()} km")
            InfoRow("Est. Fuel Needed", "%.1f liters".format(estimatedFuelNeeded), isHighlighted = true)
            Divider(Modifier.padding(vertical = 8.dp))
            RefillLimitRow("Weekly Refills", uiState.weeklyCount, uiState.vehicle!!.weekly_limit)
            RefillLimitRow("Monthly Refills", uiState.monthlyCount, uiState.vehicle.monthly_limit)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal, color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RefillLimitRow(label: String, count: Int, limit: Int) {
    val isApproaching = count.toDouble() / limit >= 0.8
    val isOver = count >= limit
    val color = if (isOver) MaterialTheme.colorScheme.error else if (isApproaching) WarningOrange else MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isOver) Icon(Icons.Default.Warning, "Limit Reached", tint = color, modifier = Modifier.size(16.dp))
            Text("$count / $limit used", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
