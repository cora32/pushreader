package io.alexarix.pushreader.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import io.alexarix.pushreader.IconArrowLeft
import io.alexarix.pushreader.repo.SPM
import io.alexarix.pushreader.ui.theme.PushReaderTheme
import io.alexarix.pushreader.viewmodels.AppItemData
import io.alexarix.pushreader.viewmodels.SettingsViewModel
import java.net.URL


@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    val model by viewModels<SettingsViewModel>()

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
                        TopBar(onBack = { this@SettingsActivity.finish() })
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val state = rememberScrollState()

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(bottom = 32.dp, top = 16.dp, start = 8.dp, end = 8.dp)
                    ) {
                        val appString =
                            if (model.selectedApps.intValue == 0) " - Intercepting all" else ""
                        when (model.isLoading.value) {
                            true -> Loader(width = 100.dp, height = 10.dp)
                            false ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(state),
                                ) {
                                    Text(
                                        "Receiver Url:", style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W500
                                        )
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    UrlField(model = model)
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Compare uniqueness by: ${model.distinctBy.value}",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W500
                                        )
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    UniquenessToggles(
                                        tickerToggle = { model.toggleUniqueByTicker(it) },
                                        titleToggle = { model.toggleUniqueByTitle(it) },
                                        bigTitleToggle = { model.toggleUniqueByBigTitle(it) },
                                        textToggle = { model.toggleUniqueByText(it) },
                                        bigTextToggle = { model.toggleUniqueByBigText(it) },
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Intercept notification from: (selected: ${model.selectedApps.intValue}$appString)",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W500
                                        )
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    AppList(
                                        appList = model.appList.value,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(350.dp)
                                    ) { packageName, enabled ->
                                        model.toggleApp(packageName, enabled)
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
fun UrlField(modifier: Modifier = Modifier, model: SettingsViewModel) {
    var state by remember {
        mutableStateOf(
            SPM.url
        )
    }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun processText(newText: String) {
        state = newText
        val isValid = isValidUrl(newText)
        isError = !isValid

        if (isValid) {
            model.setUrl(newText)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = state,
            onValueChange = { newText ->
                processText(newText)
            },
            label = { Text("Your receiver url here") },
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusRequester.freeFocus()
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(focusRequester)
        )
        Spacer(modifier = Modifier.width(8.dp))
        CompositionLocalProvider(
            LocalRippleConfiguration provides
                    RippleConfiguration(
                        rippleAlpha = RippleAlpha(
                            pressedAlpha = 0.2f,
                            focusedAlpha = 0.4f,
                            draggedAlpha = 0.4f,
                            hoveredAlpha = 0.4f
                        ),
                        color = Color.White
                    )
        ) {
            TextButton(
                onClick = {
                    focusRequester.requestFocus()
                    val newText = pasteFromClipboard(context)
                    if (newText != null) {
                        processText(newText)
                    }
                },
                modifier = Modifier
                    .width(70.dp)
                    .height(IntrinsicSize.Max)
                    .background(Color(0xFF00335F))
            ) {
                Text(
                    "PASTE",
                    style = TextStyle(
                        color = Color.LightGray
                    )
                )
            }
        }
    }
}

private fun pasteFromClipboard(context: Context): String? {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

    if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
        val clipData: ClipData? = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val item: ClipData.Item = clipData.getItemAt(0)
            return item.text?.toString()
        }
    }
    return null
}

@Composable
fun UniquenessToggles(
    modifier: Modifier = Modifier,
    tickerToggle: (Boolean) -> Unit,
    titleToggle: (Boolean) -> Unit,
    bigTitleToggle: (Boolean) -> Unit,
    textToggle: (Boolean) -> Unit,
    bigTextToggle: (Boolean) -> Unit,
) {
    val switchColors =
        SwitchDefaults.colors(
            checkedThumbColor = Color(0xFFFFFFFF),
            checkedTrackColor = Color(0xFF3EB506),
            checkedBorderColor = Color.Transparent,

            uncheckedThumbColor = Color(0xFFFFFFFF),
            uncheckedTrackColor = Color(0xFFFF2C56),
            uncheckedBorderColor = Color.Transparent,
        )

    Column {
        UniquenessToggle(
            name = "Ticker",
            colors = switchColors,
            onToggle = tickerToggle,
            isToggled = SPM.isUniqueByTicker
        )
        UniquenessToggle(
            name = "Title",
            colors = switchColors,
            onToggle = titleToggle,
            isToggled = SPM.isUniqueByTitle
        )
        UniquenessToggle(
            name = "BigTitle",
            colors = switchColors,
            onToggle = bigTitleToggle,
            isToggled = SPM.isUniqueByBigTitle
        )
        UniquenessToggle(
            name = "Text",
            colors = switchColors,
            onToggle = textToggle,
            isToggled = SPM.isUniqueByText
        )
        UniquenessToggle(
            name = "BigText",
            colors = switchColors,
            onToggle = bigTextToggle,
            isToggled = SPM.isUniqueByBigText
        )
    }
}

@Composable
fun AppList(
    appList: List<AppItemData>,
    modifier: Modifier = Modifier,
    onToggle: (String, Boolean) -> Unit
) {
    val switchColors =
        SwitchDefaults.colors(
            checkedThumbColor = Color(0xFFFFFFFF),
            checkedTrackColor = Color(0xFF3EB506),
            checkedBorderColor = Color.Transparent,

            uncheckedThumbColor = Color(0xFFFFFFFF),
            uncheckedTrackColor = Color(0xFFFF2C56),
            uncheckedBorderColor = Color.Transparent,
        )
    LazyColumn(modifier = modifier) {
        items(appList) { item ->
            AppRow(
                item = item,
                colors = switchColors
            ) { packageName, enabled ->
                onToggle(packageName, enabled)
            }
        }
    }
}

@Composable
fun UniquenessToggle(
    modifier: Modifier = Modifier,
    name: String,
    isToggled: Boolean,
    colors: SwitchColors,
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
            text = name, style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.W400
            )
        )
        Spacer(Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            colors = colors,
            onCheckedChange = {
                onChecked(it)
            },
        )
    }
}


@Composable
fun AppRow(
    modifier: Modifier = Modifier,
    item: AppItemData,
    colors: SwitchColors,
    onToggle: (String, Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(item.isToggled) }
    fun onChecked(value: Boolean) {
        isChecked = value
        onToggle(item.packageName, value)
    }

    CompositionLocalProvider(
        LocalRippleConfiguration provides
                RippleConfiguration(
                    rippleAlpha = RippleAlpha(
                        pressedAlpha = 0.2f,
                        focusedAlpha = 0.4f,
                        draggedAlpha = 0.4f,
                        hoveredAlpha = 0.4f
                    ),
                    color = Color.White
                )
    ) {
        Row(
            modifier = Modifier
                .toggleable(
                    value = isChecked,
                    onValueChange = { onChecked(it) },
                    role = Role.Switch,
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.drawable, contentDescription = item.name,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.name, style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W400
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.packageName.toString(), style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W300
                        )
                    )
                }
            }
            Spacer(Modifier.width(8.dp))

            Switch(
                checked = isChecked,
                colors = colors,
                onCheckedChange = {
                    onChecked(it)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Loader(
    width: Dp,
    height: Dp,
    color: Color = Color.Red,
    trackColor: Color = Color(0xFF19AC0E)
) {
    LinearWavyProgressIndicator(
        modifier = Modifier
//            .background(Color(0x974984F8))
            .width(width)
            .height(height),
        color = color,
        trackColor = trackColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(modifier: Modifier = Modifier, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
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

private fun isValidUrl(urlString: String): Boolean {
    return try {
        URL(urlString)
        true
    } catch (e: Exception) {
        false
    }
}
