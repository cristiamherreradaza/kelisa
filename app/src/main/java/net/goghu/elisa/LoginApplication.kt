package net.goghu.elisa

import android.app.Application

class LoginApplication : Application() {
    companion object{
        lateinit var reqResApi: ReqResApi
    }

    override fun onCreate() {
        super.onCreate()

        reqResApi = ReqResApi.getInstance(this)
    }
}