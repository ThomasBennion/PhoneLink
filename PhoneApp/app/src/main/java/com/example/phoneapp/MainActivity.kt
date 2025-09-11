package com.example.phoneapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.Image.Plane
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.phoneapp.ui.theme.SSLClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Enumeration
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory


class MainActivity : ComponentActivity() {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        val metrics = windowManager.currentWindowMetrics
        // Gets all excluding insets
        // Gets all excluding insets
        val windowInsets: WindowInsets = metrics.windowInsets
        val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout()
        )

        val insetsWidth: Int = insets.right + insets.left
        val insetsHeight: Int = insets.top + insets.bottom

        // Legacy size that Display#getSize reports
        val bounds: Rect = metrics.bounds

        val displaySize = Size(
            bounds.width(),
            bounds.height()
        )

        val startScreen = "home_screen"

        if (checkSelfPermission("android.permission.FOREGROUND_SERVICE")
            != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission("android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION")
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                (arrayOf(
                    "android.permission.FOREGROUND_SERVICE",
                    "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION"
                )), 1);
        }
        val clientTrustManager: TrustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        val clientKeyManager: KeyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )
        val appKeyStore: KeyStore = setCertificate()
        clientTrustManager.init(appKeyStore)
        clientKeyManager.init(appKeyStore, null)
        val dataStore: DataStore<Preferences> = this.dataStore
        val client = SSLClient(clientTrustManager, clientKeyManager, displaySize, dataStore)
        client.start()
        super.onCreate(savedInstanceState)
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val imageThread = HandlerThread("Image Thread")
        val displayThread = HandlerThread("Virtual Display Thread")
        val mediaThread = HandlerThread("Media Display Thread")
        imageThread.start()
        displayThread.start()
        mediaThread.start()
        val mediaHandler: Handler = Handler.createAsync(mediaThread.looper)
        val imageHandler: Handler = Handler.createAsync(imageThread.looper)
        val displayHandler: Handler = Handler.createAsync(displayThread.looper)

        val resultLauncher = getResultLauncher(
            mediaProjectionManager,
            client,
            imageHandler,
            displayHandler,
            mediaHandler,
            displaySize
        )

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = startScreen) {
                composable("home_screen") {
                    HomePage(
                        client = client,
                        onBackPress = { navController.navigate("home_screen") },
                        onSettingsPress = { navController.navigate("settings_screen") },
                        resultLauncher = resultLauncher,
                        manager = mediaProjectionManager,
                    )
                }

                composable("settings_screen") {
                    SettingsPage(
                        client = client,
                        dataStore = dataStore,
                        onBackPress = { navController.navigate("home_screen")}
                    )
                }
            }
        }
    }

    private fun setCertificate(): KeyStore {
        val appKeyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore");
        appKeyStore.apply {
            load(null)
        }

        val testCertificate: Enumeration<String> = appKeyStore.aliases()
        if (testCertificate.hasMoreElements()) {
            return appKeyStore
        }

        try {
            val serverStream: InputStream = this.assets.open("server.pem")
            val certStream: InputStream = this.assets.open("clientcert.pem")
            val keyStream: InputStream = this.assets.open("pkcs8_key")
            val keyBytes = keyStream.readBytes()
            val serverCert: Certificate = CertificateFactory
                .getInstance("X.509", "AndroidOpenSSL")
                .generateCertificate(serverStream)
            val clientCert: Certificate = CertificateFactory
                .getInstance("X.509", "AndroidOpenSSL")
                .generateCertificate(certStream)
            val newKey: KeySpec = PKCS8EncodedKeySpec(keyBytes)
            
            val clientKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val privateClient: PrivateKey = clientKeyFactory.generatePrivate(newKey)
            val clientPair = KeyPair(clientCert.publicKey, privateClient)
            appKeyStore.setCertificateEntry("ServerCert", serverCert)
            appKeyStore.setKeyEntry("ClientCert", clientPair.private, null, arrayOf(clientCert))

            certStream.close()
            keyStream.close()
        } catch (e: Exception) {
            println(e)
        }
        return appKeyStore
    }

    private fun getBitmap(image:Image): Bitmap {
        val buffer = image.planes[0].buffer
        val frameBitmap: Bitmap = Bitmap.createBitmap(
            image.width,
            image.height,
            Bitmap.Config.ARGB_8888,
        )

        val rowStride = image.planes[0].rowStride
        val pixelStride = image.planes[0].pixelStride

        val pixels = IntArray(image.width * image.height)
        val byteArray = ByteArray(buffer.remaining())
        buffer.get(byteArray)
        var offset = 0
        var index = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixelIndex = offset + x * pixelStride
                /*
                val r = byteArray[pixelIndex].toInt() and 0xFF
                val g = byteArray[pixelIndex + 1].toInt() and 0xFF
                val b = byteArray[pixelIndex + 2].toInt() and 0xFF
                val a = byteArray[pixelIndex + 3].toInt() and 0xFF
                pixels[index++] = (a shl 24) or (r shl 16) or (g shl 8) or b
                */
                pixels[index++] = ((byteArray[pixelIndex + 3].toInt() and 0xFF) shl 24) or
                        ((byteArray[pixelIndex].toInt() and 0xFF) shl 16) or
                        ((byteArray[pixelIndex + 1].toInt() and 0xFF) shl 8) or
                        ((byteArray[pixelIndex + 2].toInt() and 0xFF))
            }
            offset += rowStride
        }

        frameBitmap.setPixels(
            pixels,
            0,
            image.width,
            0,
            0,
            image.width,
            image.height
        )

        return frameBitmap
    }

    private fun getResultLauncher(
        mediaProjectionManager: MediaProjectionManager,
        client: SSLClient,
        imageHandler: Handler,
        displayHandler: Handler,
        mediaHandler: Handler,
        displaySize: Size,
    ): ActivityResultLauncher<Intent>
    {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val mediaProjection: MediaProjection =
                    mediaProjectionManager.getMediaProjection(result.resultCode, data!!)
                mediaProjection.registerCallback(MediaCallback(), mediaHandler)

                val frameReader = ImageReader.newInstance(
                    displaySize.width,
                    displaySize.height,
                    PixelFormat.RGBA_8888,
                    5
                )

                val virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    displaySize.width,
                    displaySize.height,
                    (resources.displayMetrics.densityDpi),
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    frameReader.surface,
                    DisplayCallback(),
                    displayHandler
                )

                // Set up a listener for new frames
                frameReader.setOnImageAvailableListener({ reader ->
                    
                    val image: Image = reader.acquireNextImage()
                    val bufferSize = (image.width / 4) * (image.height / 4) * 4

                    val frameBitmap: Bitmap = getBitmap(image)

                    val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(
                        frameBitmap,
                        image.width / 4,
                        image.height / 4,
                        true
                    )
                    val bufferBitmap: ByteBuffer = ByteBuffer.allocate(bufferSize)
                    scaledBitmap.copyPixelsToBuffer(bufferBitmap)
                    val sentData: ByteArray = bufferBitmap.array()

                    frameBitmap.recycle()
                    scaledBitmap.recycle()
                    image.close()

                    client.WriteImage(sentData)

                }, imageHandler)
            }
        }
    }
}