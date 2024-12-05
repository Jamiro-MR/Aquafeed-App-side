package mx.ucol.aquafeed

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MyApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_fcm"
    }

    override fun onCreate() {
        super.onCreate()

        Firebase.messaging.token.addOnCompleteListener {
            if (!it.isSuccessful){
                Log.e("TOKEN FCM", "The token was not generated.")
                return@addOnCompleteListener
            }

            val token = it.result
            Log.d("TOKEN FCM", "The token is $token")
        }
        createNotificationChannel()
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Notification FCM",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.description = "Notification received from FCM token"
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}