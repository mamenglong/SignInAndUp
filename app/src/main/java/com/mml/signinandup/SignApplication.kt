package com.mml.signinandup

import android.app.Application
import com.coder.zzq.smartshow.core.SmartShow

/**
 * 项目名称：SignInAndSingUp
 * Created by Long on 2019/3/8.
 * 修改时间：2019/3/8 15:25
 */
class SignApplication:Application() {
    override fun onCreate() {
        SmartShow.init(this)
        super.onCreate()
    }
}