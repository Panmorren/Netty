package com.example.netty.testHilt

import com.safframework.log.L
import javax.inject.Inject

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/11/27
 *UpdateUser:更新者
 *更新时间：2020/11/27
 */
class Truck @Inject constructor(val driver: Driver) {
    @BindGasEngine
    @Inject
    lateinit var gasEngine: Engine

    @BindElectricEngine
    @Inject
    lateinit var electricEngine: Engine

    fun deliver() {
        gasEngine.start()
        electricEngine.start()
        L.e("卡车正在运送货物。通过驱动 $driver")
        gasEngine.shutdown()
        electricEngine.shutdown()
    }
}