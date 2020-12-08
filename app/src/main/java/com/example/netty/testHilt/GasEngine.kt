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
class GasEngine @Inject constructor(): Engine {
    override fun start() {
        L.e("燃气发动机启动")
    }

    override fun shutdown() {
        L.e("燃气发动机停机")
    }
}