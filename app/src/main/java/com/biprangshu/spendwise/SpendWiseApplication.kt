package com.biprangshu.spendwise

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.biprangshu.spendwise.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpendWiseApplication : Application() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
