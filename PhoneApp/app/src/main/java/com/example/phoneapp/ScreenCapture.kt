package com.example.phoneapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.phoneapp.ui.theme.SSLClient

/** MediaCallback
 *
 * Called whenever there are changes to the state of the media projection
 *
 * @author Thomas Bennion
 */
class MediaCallback: MediaProjection.Callback() {
    override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
        super.onCapturedContentVisibilityChanged(isVisible)
    }

    override fun onCapturedContentResize(width: Int, height: Int) {
        super.onCapturedContentResize(width, height)
    }

    override fun onStop() {
        super.onStop()
    }
}

/** DisplayCallback
 *
 * Called whenever there are changes to the state of the virtual display
 *
 * @author Thomas Bennion
 */
class DisplayCallback: VirtualDisplay.Callback() {

    override fun onPaused() {
        super.onPaused();
    }

    override fun onResumed() {
        super.onResumed();
    }

    override fun onStopped() {
        super.onStopped();
    }
}

/** Screen Capture
 *
 * Handles foreground tasks for capturing the device screen.
 * Don't bind as it is not implemented.
 *
 * Please note that when started, ScreenCapture will provide notification services to
 * MediaProjection. Therefore, due to Android's security policy, your app must be running this
 * service before any MediaProjection activity receives a callback.
 *
 * @author Thomas Bennion
 */
class ScreenCapture() : Service()
{
    private val CHANNEL_ID = "Screen Capture Service"

    override fun onCreate() {
        //println("OnCreate")

        super.onCreate()
        val name = "Screen Capture Service"
        val descriptionText = "This service is responsible for screen capture."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = descriptionText
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //println("onStartCommand Service")

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Capture Running")
            .setContentText("Capturing the screen in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(1, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

}