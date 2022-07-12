package com.example.jello.app

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class CreateNotifications(var context: Context) {
    companion object{
        var notificationID = 0
    }
    init {
        notificationID++
    }

    fun createNotification(channelId:String, contentTitle: String, contentText: String, smallIcon: Int): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(smallIcon)
            .setAutoCancel(true)
            .build()
    }

    fun notifyNotification(notification: Notification){
        val manger: NotificationManager
        if (Build.VERSION.SDK_INT >= 26){
            manger = context.getSystemService(NotificationManager::class.java)!!
            manger.notify(notificationID,notification)
        }else{
            manger = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manger.notify(notificationID,notification)
        }
    }


}