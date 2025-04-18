package io.alexarix.pushreader.activity

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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import io.alexarix.pushreader.ui.theme.PushReaderTheme
import io.alexarix.pushreader.underlinedInfoText
import io.alexarix.pushreader.viewmodels.AppDisplayItem
import io.alexarix.pushreader.viewmodels.MainViewModel
import io.alexarix.pushreader.viewmodels.e
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
                        TopBar(
                            onSettings = {
                                this@MainActivity.startActivity(
                                    Intent(
                                        this@MainActivity,
                                        SettingsActivity::class.java
                                    )
                                )
                            },
                            onLogs = {
                                this@MainActivity.startActivity(
                                    Intent(
                                        this@MainActivity,
                                        LogsActivity::class.java
                                    )
                                )
                            }
                        )
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
                            BottomButton(model = model, activity = this@MainActivity)
                            Spacer(Modifier.height(16.dp))
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                StatsData(model = model, activity = this@MainActivity)
                                Spacer(Modifier.height(16.dp))
                                LastData(model = model)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        RuntimePermissionsDialog(
                            onPermissionDenied = {
                                "--> Perm denied".e
                            },
                            onPermissionGranted = {
                                model.startService(this@MainActivity)
                                "--> Perm granted".e
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LastData(modifier: Modifier = Modifier, model: MainViewModel) {
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    Column(
        modifier = Modifier.height(650.dp)
    ) {
        Text(
            "Latest 100 entries in DB:", style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W500
            )
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                model.last100Items.value.isEmpty() -> Text("No data")
                else ->
                    LazyColumn(state = listState) {
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
}

@Composable
private fun ColumnScope.StatsData(
    modifier: Modifier = Modifier,
    model: MainViewModel,
    activity: MainActivity
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
                    style = TextStyle(color = Color(0xFF19AC0E))
                )

                else -> Text(
                    "Denied",
                    style = TextStyle(color = Color.Red),
                    modifier = Modifier.clickable {
                        model.requestNotificationListenerAccess(activity)
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
            Text("Url: ")
            when (model.url.value.trim()) {
                "" -> Text(
                    "Not set",
                    style = TextStyle(color = Color.Red)
                )

                else -> Text(
                    model.url.value.trim(),
                    style = TextStyle(color = Color(0xFF19AC0E))
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        StatRow(
            text = "Processed: ",
            value = model.processed.intValue,
            colored = false
        )
        Spacer(Modifier.height(8.dp))
        StatRow(
            text = "Ignored: ",
            value = model.ignored.intValue,
            colored = false
        )
        Spacer(Modifier.height(8.dp))
        StatRow(
            text = "Filtered: ",
            value = model.filtered.intValue,
            colored = false
        )
        Spacer(Modifier.height(8.dp))
        StatRow(
            text = "Entries in DB: ",
            value = model.entriesInDB.intValue,
            colored = false
        )
    }
    Spacer(Modifier.height(8.dp))
    StatRow(
        text = "Sent: ",
        value = model.sent.intValue,
        colored = false
    )
    Spacer(Modifier.height(8.dp))
    StatRow(
        text = "Not sent: ",
        value = model.notSent.intValue,
        colored = true
    )
    Spacer(Modifier.height(8.dp))
    StatRow(
        text = "Errors: ",
        value = model.errors.intValue,
        colored = true
    )
}

@Composable
private fun ColumnScope.BottomButton(
    modifier: Modifier = Modifier,
    model: MainViewModel,
    activity: MainActivity
) {
    Column(modifier = Modifier) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
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
                    model.requestNotificationListenerAccess(activity)
                },
                modifier = Modifier
            ) {
                Text(
                    "Notification access settings\n(Grant \"PushReader\" all notification access )",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontWeight = FontWeight.W300,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun StatRow(modifier: Modifier = Modifier, text: String, value: Int, colored: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text,
            style = TextStyle(fontWeight = FontWeight.W500)
        )
        when (colored) {
            true -> Text(
                "$value",
                style = TextStyle(
                    fontWeight = FontWeight.W500,
                    color = if (value == 0) Color(0xFF19AC0E) else Color.Red,
                )
            )

            else -> Text(
                "$value",
                style = TextStyle(
                    fontWeight = FontWeight.W500,
                )
            )
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
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        permissions.add(Manifest.permission.QUERY_ALL_PACKAGES)
//    }

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
            color = if (isPermissionGranted) Color(0xFF19AC0E) else Color.Red
        )
//        Text(
//            if (isServiceRunning) "Service is running" else "Service is not running",
//            color = if (isServiceRunning) Color(0xFF19AC0E) else Color.Red
//        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    onSettings: () -> Unit,
    onLogs: () -> Unit
) {
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
            Row {
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

                TextButton(onClick = {
                    onLogs()
                }) {
                    Text(
                        text = "Logs",
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    )
                }
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

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun LazyItemScope.DbItem(modifier: Modifier = Modifier, item: AppDisplayItem) {
    val tickerText = underlinedInfoText(name = "Ticker", value = item.entity.tickerText)
    val summaryText = underlinedInfoText(name = "Summary", value = item.entity.summaryText)
    val subText = underlinedInfoText(name = "Summary", value = item.entity.subText)
    val info = underlinedInfoText(name = "Info", value = item.entity.infoText)
    val titleText = underlinedInfoText(name = "Title", value = item.entity.title)
    val bigTitleText = underlinedInfoText(name = "Big title", value = item.entity.bigTitle)
    val textText = underlinedInfoText(name = "Text", value = item.entity.text)
    val bigTextText = underlinedInfoText(name = "Big text", value = item.entity.bigText)
    val buttonsText = underlinedInfoText(name = "Buttons", value = "${item.entity.actions}")
    val categoryText = underlinedInfoText(name = "Category", value = item.entity.category)
    val iconText = underlinedInfoText(name = "Small Icon", value = "size: ")
    val largeIconText = underlinedInfoText(name = "Large Icon", value = "size: ")


    val smallIcon =
        item.entity.smallIconStr?.let { Base64.decode(it) }
    val largeIcon =
        item.entity.largeIconStr?.let { Base64.decode(it) }

    Column {
        Text(
            text = "${sdf.format(item.entity.timestamp)}",
            textAlign = TextAlign.Start,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W700
            ),
            modifier = Modifier.align(Alignment.End)
        )
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = item.icon, contentDescription = item.name,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = item.name,
                    textAlign = TextAlign.Start,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${item.entity.packageName}",
                    textAlign = TextAlign.Start,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    )
                )
            }
        }
        Column(modifier = Modifier.padding(8.dp)) {

            Text(
                text = summaryText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = subText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = info,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = tickerText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = titleText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = bigTitleText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = textText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = bigTextText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = buttonsText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Text(
                text = categoryText,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = iconText,
                    textAlign = TextAlign.Start,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                )
                if (smallIcon != null)
                    AsyncImage(
                        model = smallIcon,
                        contentDescription = null,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceDim),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(30.dp)
                    ) else Text("none")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = largeIconText,
                    textAlign = TextAlign.Start,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                )
                if (largeIcon != null)
                    AsyncImage(
                        model = largeIcon,
                        contentDescription = null,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceDim),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(30.dp)
                    ) else Text("none")
            }

        }
        HorizontalDivider(
            thickness = 0.4.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US)