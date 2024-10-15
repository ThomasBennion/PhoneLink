package com.example.phoneapp

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phoneapp.ui.theme.PhoneAppTheme
import com.example.phoneapp.ui.theme.SSLClient
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    client: SSLClient,
    onBackPress: (() -> Unit),
    onSettingsPress: (() -> Unit),
    resultLauncher: ActivityResultLauncher<Intent>,
    manager: MediaProjectionManager
)
{


    val default = ""
    var name = remember {
        mutableStateOf(default)
    }

    var input = remember {
        mutableStateOf(default)
    }

    val captureOn = remember {
        mutableStateOf(false)
    }

    val serviceContext = LocalContext.current.applicationContext
    val disabledButton = ButtonDefaults.buttonColors(
        contentColor = MaterialTheme.colorScheme.onSecondary,
        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    val enabledButton = ButtonDefaults.buttonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    val buttonOn = remember {
        mutableStateOf(disabledButton)
    }
    val captureButton = object {
        fun toggle() {
            if (!captureOn.value) {
                serviceContext.startForegroundService(
                    Intent(serviceContext, ScreenCapture()::class.java)
                )
                resultLauncher.launch(manager.createScreenCaptureIntent())
                buttonOn.value = enabledButton
            } else {
                serviceContext.stopService(
                    Intent(serviceContext, ScreenCapture()::class.java)
                )
                buttonOn.value = disabledButton
            }
            captureOn.value = captureOn.value != true
        }
    }

    val cScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()

        executor.execute(kotlinx.coroutines.Runnable {
            run {
                val buffer = ByteArray(1024)
                var byte: Int
                while (true) {
                    try {
                        byte = client.read(buffer)
                        if (byte > 0) {
                            val finalBytes = byte
                            handler.post {
                                run {
                                    var newMessage: String = (String(buffer, 0, finalBytes))
                                    println(newMessage)
                                    name.value = newMessage
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
    var pState = "State2"
    PhoneAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(80.dp),
                    colors = TopAppBarDefaults.topAppBarColors  (
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            verticalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = "Paranoia ${pState}",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { onSettingsPress() },
                            Modifier
                                .size(width = 160.dp, height = 80.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "Settings",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${name.value}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .fillMaxHeight(0.25F),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { captureButton.toggle() },
                    colors = buttonOn.value,
                    modifier = Modifier
                        .size(width = 160.dp, height = 60.dp)
                        .align(Alignment.CenterHorizontally),
                    )
                {
                    Text(
                        text = "Start Capture Service",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}