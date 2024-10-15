package com.example.phoneapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Insets
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.Image
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.phoneapp.ui.theme.SSLClient
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
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

    override fun onCreate(savedInstanceState: Bundle?) {
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
        val port = 4655
        val address = "192.168.1.100"
        val host = InetSocketAddress(address, port)
        val clientTrustManager: TrustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        val clientKeyManager: KeyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )
        val appKeyStore: KeyStore = setCertificate()
        clientTrustManager.init(appKeyStore)
        clientKeyManager.init(appKeyStore, null)
        val client = SSLClient(host, clientTrustManager, clientKeyManager)
        client.start()
        super.onCreate(savedInstanceState)
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val imageThread = HandlerThread("Image Thread")
        val displayThread = HandlerThread("Virtual Display Thread")
        imageThread.start()
        displayThread.start()
        val imageHandler: Handler = Handler.createAsync(imageThread.looper)
        val displayHandler: Handler = Handler.createAsync(displayThread.looper)
        val resultLauncher = getResultLauncher(mediaProjectionManager, client, imageHandler, displayHandler)

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

    private fun getResultLauncher(
        mediaProjectionManager: MediaProjectionManager,
        client: SSLClient,
        imageHandler: Handler,
        displayHandler: Handler
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
                mediaProjection.registerCallback(MediaCallback(), null)

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

                // Legacy size that Display#getSize reports
                val bounds: Rect = metrics.bounds
                val displaySize = Size(
                    bounds.width() - insetsWidth,
                    bounds.height() - insetsHeight
                )

                val frameReader = ImageReader.newInstance(
                    displaySize.width / 2,
                    displaySize.height / 2,
                    PixelFormat.RGBA_8888,
                    5
                )
                val virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    displaySize.width / 2,
                    displaySize.height / 2,
                    8,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    frameReader.surface,
                    DisplayCallback(),
                    displayHandler
                )

                // Set up a listener for new frames
                frameReader.setOnImageAvailableListener({ reader ->
                    val image: Image = reader.acquireNextImage()
                    val buffer = image.planes[0].buffer
                    val frameData: ByteArray = ByteArray(buffer.remaining())
                    buffer.get(frameData)
                    image.close()
                    client.WriteImage(frameData, displaySize)
                }, imageHandler)
            }
        }
    }
}