package com.example.jello.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class JelloApp:Application() {

    companion object{
        const val channel_ID_one = "Cart channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(channel_ID_one,"Cart Channel",NotificationManager.IMPORTANCE_HIGH)
    }

    private fun createNotificationChannel(channelID:String,channelName:String,importance:Int){
        if (Build.VERSION.SDK_INT >= 26){
            val channel = NotificationChannel(channelID,channelName,importance)
            channel.setShowBadge(true)
            channel.enableVibration(true)
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}