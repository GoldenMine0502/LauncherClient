package kr.goldenmine.inuminecraftlauncher.server

import kr.goldenmine.inuminecraftlauncher.DevelopmentConfiguration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LauncherServerService {
    companion object {
        val LAUNCHER_SERVER: LauncherServerApi = Retrofit.Builder()
            .baseUrl(
                if(DevelopmentConfiguration.IS_DEVELOPMENT_SERVER)
                    "http://localhost:20301/"
                else
                    "http://minecraft.goldenmine.kr:20301/"
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LauncherServerApi::class.java)
    }
}