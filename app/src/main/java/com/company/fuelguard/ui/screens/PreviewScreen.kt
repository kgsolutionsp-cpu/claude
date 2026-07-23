package com.company.fuelguard.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.company.fuelguard.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    navController: NavController,
    imageUriString: String,
    plate: String,
    odometer: String,
    address: String,
    dateTime: String,
    refillNum: String
) {
    val context = LocalContext.current
    val imageUri = Uri.parse(imageUriString)
    val shareDetails = """⛽ REFUELING REQUEST
Vehicle: $plate
Odometer: $odometer
Location: $address
Time: $dateTime
Refill #: $refillNum
Please approve.""".trimIndent()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Confirm & Share") }, navigationIcon = { IconButton({ navController.popBackStack(Screen.Home.route, false) }) { Icon(Icons.Default.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary, navigationIconContentColor = MaterialTheme.colorScheme.onPrimary)) },
        floatingActionButton = { ExtendedFloatingActionButton({ shareImage(context, imageUri, shareDetails) }, icon = { Icon(Icons.Default.Share, "Share") }, text = { Text("Request Approval") }) },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            AsyncImage(imageUri, "Stamped Refuel Photo", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
    }
}

private fun shareImage(context: Context, uri: Uri, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/jpeg"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Request Via"))
}
