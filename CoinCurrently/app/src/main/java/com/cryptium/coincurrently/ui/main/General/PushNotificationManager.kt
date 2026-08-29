package com.cryptium.coincurrently.ui.main.General

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.Managers.FirebaseManager
import com.cryptium.coincurrently.ui.main.Repositories.PriceAlertsRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import networking.ApiHelper
import networking.MainRepository
import networking.RetrofitBuilder
import androidx.core.net.toUri

class PushNotificationManager: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        sendNotification(remoteMessage)

        remoteMessage.data["alertId"]?.let {
            GlobalScope.launch {
                PriceAlertsRepository.deleteLocalAlert(it)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)

        intent.action = Intent.ACTION_VIEW

        remoteMessage.data["data"]?.let {
            intent.data = it.toUri()
        }

        remoteMessage.data["cryptoID"]?.let {
            intent.putExtra("cryptoID", it)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "0"

        val notificationBuilder: Notification.Builder = Notification.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setContentTitle(remoteMessage.notification?.title ?: "")
            .setContentText(remoteMessage.notification?.body ?: "")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_MAX)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "CoinCurrently", NotificationManager.IMPORTANCE_DEFAULT)

        try {
            notificationManager.createNotificationChannel(channel)
        } catch (_: Exception) {
            return
        }

        notificationManager.notify(1, notificationBuilder.build())
    }
}