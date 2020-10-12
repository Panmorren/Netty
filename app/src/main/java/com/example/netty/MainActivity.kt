package com.example.netty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.netty.kotlin.NettyDiscoveryClient
import com.example.netty.kotlin.NettyDiscoveryServer
import com.safframework.log.L

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NettyDiscoveryClient().start()
        NettyDiscoveryServer()
    }
}