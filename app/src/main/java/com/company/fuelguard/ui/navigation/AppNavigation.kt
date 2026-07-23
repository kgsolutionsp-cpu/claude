package com.company.fuelguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.company.fuelguard.ui.screens.*
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object RefuelEntry : Screen("refuel_entry/{vehicleId}") {
        fun createRoute(vehicleId: Int) = "refuel_entry/$vehicleId"
    }
    object Camera : Screen("camera/{vehicleId}/{odometer}") {
        fun createRoute(vehicleId: Int, odometer: String) = "camera/$vehicleId/$odometer"
    }
    object Preview : Screen("preview")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(
            route = Screen.RefuelEntry.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.IntType })
        ) {
            RefuelEntryScreen(navController, it.arguments!!.getInt("vehicleId"))
        }
        composable(
            route = Screen.Camera.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.IntType },
                navArgument("odometer") { type = NavType.StringType }
            )
        ) {
            CameraScreen(navController, it.arguments!!.getInt("vehicleId"), it.arguments!!.getString("odometer")!!)
        }
        composable(
            route = Screen.Preview.route + "?uri={uri}&plate={plate}&odo={odo}&addr={addr}&time={time}&refill={refill}",
            arguments = listOf(
                navArgument("uri") { nullable = true },
                navArgument("plate") { nullable = true },
                navArgument("odo") { nullable = true },
                navArgument("addr") { nullable = true },
                navArgument("time") { nullable = true },
                navArgument("refill") { nullable = true }
            )
        ) {
            val args = it.arguments!!
            val decode: (String?) -> String = { s -> s?.let { URLDecoder.decode(it, "UTF-8") } ?: "" }
            PreviewScreen(navController,
                imageUriString = decode(args.getString("uri")),
                plate = decode(args.getString("plate")),
                odometer = decode(args.getString("odo")),
                address = decode(args.getString("addr")),
                dateTime = decode(args.getString("time")),
                refillNum = decode(args.getString("refill"))
            )
        }
    }
}
