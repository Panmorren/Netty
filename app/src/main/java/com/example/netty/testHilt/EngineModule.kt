package com.example.netty.testHilt

import androidx.core.app.ActivityCompat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/11/27
 *UpdateUser:更新者
 *更新时间：2020/11/27
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class EngineModule {
    @BindGasEngine
    @Binds
    abstract fun binEngine(gasEngine: GasEngine): Engine

    @BindElectricEngine
    @Binds
    abstract fun bindElectricEngine(electricEngine: ElectricEngine): Engine
}