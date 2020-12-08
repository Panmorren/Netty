package com.example.netty.testHilt

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 *描述:
 *创建者：pjh
 *创建日期：2020/11/27
 *UpdateUser:更新者
 *更新时间：2020/11/27
 */
class MyViewModel @ViewModelInject constructor(val repository: Repository): ViewModel(){
}