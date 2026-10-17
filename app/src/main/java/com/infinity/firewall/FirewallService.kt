package com.infinity.firewall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class FirewallService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val running = AtomicBoolean(false)

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == ACTION_STOP) {
            stopFirewall()
            return START_NOT_STICKY
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (running.compareAndSet(false, true)) {
            Thread {
                establishFirewall()
            }.start()
        }

        return START_NOT_STICKY
    }

    private fun establishFirewall() {
        try {
            val builder = Builder()
                .setSession("Infinity Firewall")
                .addAddress("10.10.10.1", 32)
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .setMtu(1500)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setBlocking(true)
            }

            vpnInterface = builder.establish()

            val fd = vpnInterface?.fileDescriptor
                ?: throw IOException("VPN interface unavailable")

            val buffer = java.nio.ByteBuffer.allocate(32767)

            while (running.get()) {
                buffer.clear()
                val length = android.system.Os.read(
                    fd,
                    buffer
                )

                if (length < 0) break

                // Intentionally discard every captured packet.
                // No packets are forwarded to the internet.
            }
        } catch (_: Exception) {
            // A failed VPN must not be reported as active.
        } finally {
            running.set(false)
            vpnInterface?.close()
            vpnInterface = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopFirewall() {
        running.set(false)
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Infinity Firewall",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Infinity Firewall")
                .setContentText("Network blocking service running")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(openApp)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Infinity Firewall")
                .setContentText("Network blocking service running")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build()
        }
    }

    override fun onDestroy() {
        running.set(false)
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP =
            "com.infinity.firewall.STOP"

        private const val CHANNEL_ID = "infinity_firewall"
        private const val NOTIFICATION_ID = 1001
    }
}
