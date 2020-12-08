package com.example.netty.test

import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RestController

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/12/2
 *UpdateUser:更新者
 *更新时间：2020/12/2
 */
@RestController
class UserController {

    @GetMapping("/")
    public fun login(): String {
        return "Successful."
    }
}