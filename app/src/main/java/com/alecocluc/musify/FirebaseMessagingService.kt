package com.alecocluc.musify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        // Mostrar la notificación usando NotificationManager
        showNotification(title, body)

        // Mostrar el log de la notificación recibida
        Log.d(TAG, "Mensaje recibido - Título: $title, Cuerpo: $body")
    }

    override fun onNewToken(token: String) {
        // Maneja la actualización del token aquí
        Log.d(TAG, "Nuevo token: $token")
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        // Crear un canal de notificaciones (siempre >= API 28)
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Descripción del canal"
        }

        // Registrar el canal con el sistema
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Crear un intent para abrir MainActivity cuando se toque la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Crear la notificación
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Obtener el NotificationManager del sistema y mostrar la notificación
        notificationManager.notify(0, builder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
