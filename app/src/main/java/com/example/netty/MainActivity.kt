package com.example.netty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.netty.testHilt.MyViewModel
import com.example.netty.testHilt.Truck
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    @Inject
//    lateinit var truck: Truck
//
//    @Inject
//    lateinit var okHttpClient: OkHttpClient
//
//    @Inject
//    lateinit var retrofit: Retrofit
//
//    @Inject
//    lateinit var myViewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        truck.deliver()

        initServer()
    }

    private fun initServer() {
        val server: Server = AndServer.webServer(this)
            .port(8080)
            .timeout(10, TimeUnit.SECONDS)
            .build()


        server.startup()
    }
}