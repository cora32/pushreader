package io.alexarix.pushreader

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.alexarix.pushreader.ui.theme.PushReaderTheme

class MainActivity : ComponentActivity() {
    val model by viewModels<MainViewModel>()

    override fun onDestroy() {
        super.onDestroy()

        lifecycle.removeObserver(model)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycle.addObserver(model)

        setContent {
            PushReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(bottom = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Stats(
                                isPermissionGranted = model.isPermissionGranted.value,
                                isServiceRunning = model.isServiceRunning.value,
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,

                                ) {
                                TextButton(
                                    onClick = {
                                        model.requestNotificationListenerAccess(this@MainActivity)
                                    }
                                ) {
                                    Text(
                                        "Notification access settings\n(Grant \"PushReader\" all notification access )",
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            fontWeight = FontWeight.W300,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
//                                Spacer(Modifier.height(8.dp))
//                                TextButton(
//                                    enabled = model.isPermissionGranted.value,
//                                    onClick = {
//                                    }
//                                ) {
//                                    Text(
//                                        "Start PushReader service", textAlign = TextAlign.Center,
//                                        style = TextStyle(
//                                            fontWeight = FontWeight.W300,
//                                            fontSize = 15.sp
//                                        )
//                                    )
//                                }
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        RuntimePermissionsDialog(
                            POST_NOTIFICATIONS,
                            onPermissionDenied = {},
                            onPermissionGranted = {},
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RuntimePermissionsDialog(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                LocalContext.current,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            val requestLocationPermissionLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->

                    if (isGranted) {
                        onPermissionGranted()
                    } else {
                        onPermissionDenied()
                    }
                }

            SideEffect {
                requestLocationPermissionLauncher.launch(permission)
            }
        }
    }
}

@Composable
fun Stats(
    modifier: Modifier = Modifier,
    isPermissionGranted: Boolean,
    isServiceRunning: Boolean,
) {
    Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
        Text(
            if (isPermissionGranted) "Permission granted" else "Permission denied",
            color = if (isPermissionGranted) Color.Green else Color.Red
        )
//        Text(
//            if (isServiceRunning) "Service is running" else "Service is not running",
//            color = if (isServiceRunning) Color.Green else Color.Red
//        )
    }
}