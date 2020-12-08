package com.example.netty.utils

import android.app.Application
import androidx.multidex.MultiDex
import com.safframework.log.L
import com.safframework.log.LogLevel
import dagger.hilt.android.HiltAndroidApp

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/10/12
 *UpdateUser:更新者
 *更新时间：2020/10/12
 */

@HiltAndroidApp
open class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        L.init("netty")
        L.logLevel = LogLevel.DEBUG

        MultiDex.install(this)
    }
}