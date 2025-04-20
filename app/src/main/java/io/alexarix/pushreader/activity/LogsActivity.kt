package io.alexarix.pushreader.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import io.alexarix.pushreader.IconArrowLeft
import io.alexarix.pushreader.repo.managers.LogType
import io.alexarix.pushreader.repo.room.entity.PRServiceLogEntity
import io.alexarix.pushreader.ui.theme.PushReaderTheme
import io.alexarix.pushreader.underlinedInfoText
import io.alexarix.pushreader.viewmodels.LogsViewModel


@AndroidEntryPoint
class LogsActivity : ComponentActivity() {
    val model by viewModels<LogsViewModel>()

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
                val listState = rememberSaveable(saver = LazyListState.Saver) {
                    LazyListState()
                }

                Scaffold(
                    topBar = {
                        TopBar(onBack = { this@LogsActivity.finish() })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(bottom = 8.dp, top = 16.dp, start = 8.dp, end = 8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        LogToggle(
                            text = "Enable log",
                            isToggled = model.isLogEnabled.value,
                            onToggle = { model.toggleLog(it) }
                        )
                        Spacer(Modifier.height(8.dp))
                        if (model.isLogEnabled.value)
                            ToggleBlock(model = model)
                        Spacer(Modifier.height(16.dp))
                        when (model.isLoading.value) {
                            true ->
                                Loader(width = 150.dp, height = 6.dp)

                            false ->
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(750.dp)
                                ) {
                                    items(model.logs.value) { item ->
                                        LogEntry(entry = item)
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
private fun ToggleBlock(modifier: Modifier = Modifier, model: LogsViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LogToggle(
            text = "Show errors",
            isToggled = model.isShowErrors.value,
            onToggle = model::toggleShowErrors
        )
        LogToggle(
            text = "Show info",
            isToggled = model.isShowInfo.value,
            onToggle = model::toggleShowInfo
        )
        LogToggle(
            text = "Show OK",
            isToggled = model.isShowOk.value,
            onToggle = model::toggleShowOk
        )
        LogToggle(
            text = "Show unknown",
            isToggled = model.isShowUnknown.value,
            onToggle = model::toggleShowUnknown
        )
    }
}

@Composable
private fun LogToggle(
    modifier: Modifier = Modifier,
    text: String,
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(isToggled) }
    fun onChecked(value: Boolean) {
        isChecked = value
        onToggle(value)
    }

    Row(
        modifier = Modifier
            .toggleable(
                value = isChecked,
                onValueChange = { onChecked(it) },
                role = Role.Switch,
            )
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text, style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.W400
            )
        )
        Spacer(Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFFFFF),
                checkedTrackColor = Color(0xFF3EB506),
                checkedBorderColor = Color.Transparent,

                uncheckedThumbColor = Color(0xFFFFFFFF),
                uncheckedTrackColor = Color(0xFFFF2C56),
                uncheckedBorderColor = Color.Transparent,
            ),
            onCheckedChange = {
                onChecked(it)
            },
        )
    }
}

@Composable
private fun LogEntry(modifier: Modifier = Modifier, entry: PRServiceLogEntity) {
    val color = when (entry.logType) {
        LogType.OK -> Color(0xAB11971B)
        LogType.Unknown -> Color(0xA9ACA413)
        LogType.Info -> Color(0x9C0D7B97)
        LogType.Fail -> Color(0x99AA1010)
        null -> Color.Unspecified
    }

    val tickerText = underlinedInfoText(name = "Ticker", value = entry.tickerText)
    val summaryText = underlinedInfoText(name = "Summary", value = entry.summaryText)
    val subText = underlinedInfoText(name = "Subtext", value = entry.subText)
    val info = underlinedInfoText(name = "Info", value = entry.infoText)
    val titleText = underlinedInfoText(name = "Title", value = entry.title)
    val bigTitleText = underlinedInfoText(name = "Big title", value = entry.bigTitle)
    val textText = underlinedInfoText(name = "Text", value = entry.text)
    val bigTextText = underlinedInfoText(name = "Big text", value = entry.bigText)

    val showEntryData = entry.tickerText != null
            || entry.summaryText != null
            || entry.subText != null
            || entry.infoText != null
            || entry.title != null
            || entry.bigTitle != null
            || entry.text != null
            || entry.bigText != null
    Column(
        modifier = Modifier
            .background(color)
            .padding(4.dp)
    ) {
        Text(
            text = "${sdf.format(entry.timestamp)}",
            textAlign = TextAlign.Start,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.W700
            ),
            modifier = Modifier.align(Alignment.End)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${entry.reason}",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W800
                )
            )
            Spacer(Modifier.height(16.dp))
            if (showEntryData)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = tickerText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = summaryText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = subText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = info,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = titleText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = bigTitleText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = textText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = bigTextText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                }

            Spacer(Modifier.height(6.dp))
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(modifier: Modifier = Modifier, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Logs",
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700,
                ),
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                onBack()
            }) {
                Icon(IconArrowLeft, "backIcon")
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