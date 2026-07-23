package com.company.fuelguard.ui.screens

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.company.fuelguard.ui.navigation.Screen
import com.company.fuelguard.ui.viewmodel.*
import com.company.fuelguard.utils.RequestCameraAndLocationPermissions
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.Executor

@Composable
fun CameraScreen(navController: NavController, vehicleId: Int, odometer: String) {
    var hasPermissions by remember { mutableStateOf(false) }
    RequestCameraAndLocationPermissions { granted ->
        hasPermissions = granted
        if (!granted) {
            navController.popBackStack()
        }
    }

    if (hasPermissions) {
        val context = LocalContext.current
        val viewModel: CameraViewModel = viewModel(factory = CameraViewModelFactory(context.applicationContext as Application))
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(uiState.stampedImageUri) {
            uiState.stampedImageUri?.let { uri ->
                val details = uiState.shareDetails!!
                val route = Screen.Preview.route +
                        "?uri=${URLEncoder.encode(uri.toString(), "UTF-8")}" +
                        "&plate=${details.vehiclePlate}" +
                        "&odo=${details.odometer}" +
                        "&addr=${URLEncoder.encode(details.address, "UTF-8")}" +
                        "&time=${URLEncoder.encode(details.dateTime, "UTF-8")}" +
                        "&refill=${details.refillNumber}"

                navController.navigate(route) { popUpTo(Screen.Home.route) }
            }
        }
        CameraContent(uiState) { file ->
            // In a real app, get driver name from a login session/settings
            viewModel.processImage(vehicleId, odometer.toDouble(), file, "Driver Name")
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Requesting permissions...") }
    }
}

@Composable
private fun CameraContent(uiState: CameraUiState, onImageCaptured: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    Box(Modifier.fillMaxSize()) {
        AndroidView({ ctx ->
            PreviewView(ctx).apply {
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    bindCameraUseCases(cameraProviderFuture.get(), this, lensFacing, lifecycleOwner, executor) { imageCapture = it }
                }, executor)
            }
        }, Modifier.fillMaxSize()) { view ->
            cameraProviderFuture.addListener({
                bindCameraUseCases(cameraProviderFuture.get(), view, lensFacing, lifecycleOwner, ContextCompat.getMainExecutor(context)) { imageCapture = it }
            }, ContextCompat.getMainExecutor(context))
        }

        Row(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 32.dp, start = 16.dp, end = 16.dp), Arrangement.SpaceAround, Alignment.CenterVertically) {
            IconButton({}) {} // Flash placeholder
            IconButton(
                onClick = { if (!uiState.isProcessing) imageCapture?.let { takePicture(it, context, onImageCaptured) { err -> Log.e("Camera", "Capture error: $err") } } },
                modifier = Modifier.size(72.dp).border(2.dp, Color.White, CircleShape)
            ) { Icon(Icons.Default.PhotoCamera, "Take Picture", tint = Color.White, modifier = Modifier.size(40.dp)) }
            IconButton({ lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK }) {
                Icon(Icons.Default.Cameraswitch, "Switch Camera", tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        if (uiState.isProcessing) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        uiState.error?.let {
            Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text("Error: $it") }
        }
    }
}

private fun bindCameraUseCases(provider: ProcessCameraProvider, view: PreviewView, lens: Int, owner: LifecycleOwner, executor: Executor, onReady: (ImageCapture) -> Unit) {
    val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
    val selector = CameraSelector.Builder().requireLensFacing(lens).build()
    val capture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
    onReady(capture)
    try {
        provider.unbindAll()
        provider.bindToLifecycle(owner, selector, preview, capture)
    } catch (e: Exception) { Log.e("Camera", "Binding failed", e) }
}

private fun takePicture(capture: ImageCapture, context: Context, onImage: (File) -> Unit, onError: (ImageCaptureException) -> Unit) {
    val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
    capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(res: ImageCapture.OutputFileResults) { onImage(file) }
            override fun onError(ex: ImageCaptureException) { onError(ex) }
        }
    )
}
