package com.example.phoneapp

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.phoneapp.ui.theme.PhoneAppTheme
import com.example.phoneapp.ui.theme.SSLClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(client: SSLClient, dataStore: DataStore<Preferences>, onBackPress: () -> Unit) {
    val PORT_KEY = intPreferencesKey("Port")
    val ADDRESS_KEY = stringPreferencesKey("Address")

    val defaultPort = 4655
    val defaultAddress = "192.168.1.100"

    val portPreference = dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[PORT_KEY] ?: defaultPort
        }
    val addressPreference = dataStore.data
        .map { preferences ->
            preferences[ADDRESS_KEY] ?: defaultAddress
        }

    val port = remember {
        mutableStateOf(defaultPort)
    }
    val address = remember {
        mutableStateOf(defaultAddress)
    }

    fun applySettings() {
        runBlocking {
            dataStore.edit { settings ->
                settings[ADDRESS_KEY] = address.value
                settings[PORT_KEY] = port.value
            }
        }
        if (client.checkConnection()) {
            client.disconnect()
                .then {
                    if (it) {
                        val host = InetSocketAddress(address.value, port.value)
                        client.connect(host)
                    }
                }
        } else {
            val host = InetSocketAddress(address.value, port.value)
            client.connect(host)
        }
        onBackPress()
    }

    val cScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())
        val executor = Executors.newSingleThreadExecutor()
        executor.execute(kotlinx.coroutines.Runnable {
            run {
                /*port.value = runBlocking {
                    portPreference.first()
                }
                address.value = runBlocking {
                    addressPreference.first()
                }*/
                handler.post {
                    run {
                        port.value = runBlocking {
                            portPreference.first()
                        }
                        address.value = runBlocking {
                            addressPreference.first()
                        }
                    }
                }
            }
        })
    }

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
                                text = "Settings",
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
                            onClick = { applySettings() },
                            Modifier
                                .size(width = 320.dp, height = 140.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "Connect",
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
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = address.value,
                    onValueChange = { it: String -> address.value = it },
                    label = { "Address" },
                    prefix = { "Address" },
                    supportingText = { "Address" },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Decimal
                    )
                )
                OutlinedTextField(
                    value = port.value.toString(),
                    onValueChange = { it: String -> port.value = it.toInt() },
                    label = { "port" },
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType= KeyboardType.Number
                    )
                )
            }
        }
    }
}