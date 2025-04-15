package io.alexarix.pushreader.activity

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.alexarix.pushreader.repo.room.PRLogEntity
import io.alexarix.pushreader.ui.theme.PushReaderTheme
import io.alexarix.pushreader.viewmodels.MainViewModel
import io.alexarix.pushreader.viewmodels.e
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
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
                Scaffold(
                    topBar = {
                        TopBar(onSettings = {
                            this@MainActivity.startActivity(
                                Intent(
                                    this@MainActivity,
                                    SettingsActivity::class.java
                                )
                            )
                        })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(bottom = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Permission status: ")
                                        when (model.isPermissionGranted.value) {
                                            true -> Text(
                                                "Granted",
                                                style = TextStyle(color = Color.Green)
                                            )

                                            else -> Text(
                                                "Denied",
                                                style = TextStyle(color = Color.Red),
                                                modifier = Modifier.clickable {
                                                    model.requestNotificationListenerAccess(this@MainActivity)
                                                }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Receiver Url: ")
                                        when (model.url.value.trim()) {
                                            "" -> Text(
                                                "Not set",
                                                style = TextStyle(color = Color.Red)
                                            )

                                            else -> Text(
                                                model.url.value.trim(),
                                                style = TextStyle(color = Color.Green)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Processed: ",
                                            style = TextStyle(fontWeight = FontWeight.W500)
                                        )
                                        Text(
                                            "${model.processed.intValue}",
                                            style = TextStyle(fontWeight = FontWeight.W500)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Sent: ",
                                            style = TextStyle(fontWeight = FontWeight.W500)
                                        )
                                        Text(
                                            "${model.sent.intValue}",
                                            style = TextStyle(fontWeight = FontWeight.W500)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Entries in DB: ",
                                            style = TextStyle(fontWeight = FontWeight.W500)
                                        )
                                        Text(
                                            "${model.entriesInDB.intValue}",
                                            style = TextStyle(fontWeight = FontWeight.W500)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Not sent: ",
                                        style = TextStyle(fontWeight = FontWeight.W500)
                                    )
                                    Text(
                                        "${model.notSent.intValue}",
                                        style = TextStyle(
                                            fontWeight = FontWeight.W500,
                                            color = if (model.notSent.intValue == 0) Color.Green else Color.Red,
                                        )
                                    )
                                }
                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Errors: ",
                                        style = TextStyle(fontWeight = FontWeight.W500)
                                    )
                                    Text(
                                        "${model.errors.intValue}",
                                        style = TextStyle(
                                            fontWeight = FontWeight.W500,
                                            color = if (model.errors.intValue == 0) Color.Green else Color.Red,
                                        )
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                ) {
                                    Text(
                                        "Latest 100 entries in DB:", style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W500
                                        )
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = Color(0xFFE4E4E4)
                                    )
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        when {
                                            model.last100Items.value.isEmpty() -> Text("No data")
                                            else ->
                                                LazyColumn {
                                                    items(model.last100Items.value) {
                                                        DbItem(
                                                            item = it,
                                                            modifier = Modifier.animateItem()
                                                        )
                                                    }
                                                }
                                        }
                                    }
                                }
                                // Bottom button
                                Column() {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = Color(0xFFE4E4E4)
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF7D0000))
                                            .align(Alignment.CenterHorizontally)
                                            .padding(4.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                model.requestNotificationListenerAccess(this@MainActivity)
                                            },
                                            modifier = Modifier
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
                                    }
                                }
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        RuntimePermissionsDialog(
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
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
) {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(POST_NOTIFICATIONS)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        permissions.add(Manifest.permission.QUERY_ALL_PACKAGES)
    }

    val permissionsToRequest = mutableListOf<String>()
    for (permission in permissions) {
        "--> Checking perm: $permission".e
        if (ContextCompat.checkSelfPermission(
                LocalContext.current,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            "--> Perm: $permission is DENIED".e
            permissionsToRequest.add(permission)
        } else {
            "--> Perm: $permission is GRANTED".e
        }
    }

    "--> Perm size: ${permissionsToRequest.size} ".e
    if (permissionsToRequest.isEmpty()) return

    val requestLocationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { map ->

            if (map.all { it.value == true }) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

    SideEffect {
        requestLocationPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}

@Composable
private fun Stats(
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(modifier: Modifier = Modifier, onSettings: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "PushReader",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700,
                ),
                maxLines = 1,
            )
        },
        actions = {
            TextButton(onClick = {
                onSettings()
            }) {
                Text(
                    text = "Settings",
                    textAlign = TextAlign.Start,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            navigationIconContentColor = Color.Black,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black,
        ),
        modifier = Modifier.clip(
            shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
        )
    )
}

@Composable
fun LazyItemScope.DbItem(modifier: Modifier = Modifier, item: PRLogEntity) {
    Column {
        Text(
            text = "Date: ${sdf.format(item.timestamp)}",
            textAlign = TextAlign.Start,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.White
            )
        )
        Text(
            text = "PackageName: ${item.packageName}",
            textAlign = TextAlign.Start,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.White
            )
        )
        Column(modifier = Modifier.padding(8.dp)) {

            Text(
                text = "Ticker: ${item.tickerText}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )

            Text(
                text = "Text: ${item.text}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
            Text(
                text = "BigText: ${item.bigText}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
            Text(
                text = "Title: ${item.title}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
            Text(
                text = "BigTitle: ${item.bigTitle}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
            Text(
                text = "Buttons: ${item.actions?.joinToString(" ")}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
            Text(
                text = "Category: ${item.category}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )

        }
    }
}

val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US)