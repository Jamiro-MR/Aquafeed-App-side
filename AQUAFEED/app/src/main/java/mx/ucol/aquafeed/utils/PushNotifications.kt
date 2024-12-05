package mx.ucol.aquafeed.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mx.ucol.aquafeed.MyApplication
import mx.ucol.aquafeed.MainActivity

class PushNotifications : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM Token [PUSH NOTIFICATION]", "Token: $token")
    }


    fun sendTokenToServer(token: String, userId: Int) {
        Log.d("FCM Token", "Retrieved token: $token and Userid: $userId")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val orderId = remoteMessage.data["order_id"]
            Log.d("ON MESSAGE RECEIVED", "Order ID: " + orderId.toString())
            sendNotification(it.title, it.body, orderId?.toInt())
        }
    }

    private fun sendNotification(title: String?, messageBody: String?, orderId: Number?) {

        val notificationManager = getSystemService(NotificationManager::class.java)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("order_id", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, MyApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notificationBuilder)
    }
}