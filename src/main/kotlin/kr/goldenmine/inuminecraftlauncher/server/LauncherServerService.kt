package kr.goldenmine.inuminecraftlauncher.server

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.DevelopmentConfiguration
import kr.goldenmine.inuminecraftlauncher.assets.ForgeApi
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftApi
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.ArgumentsDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration

class LauncherServerService {
    companion object {
        val LAUNCHER_SERVER: LauncherServerApi = Retrofit.Builder()
            .baseUrl(
                if(DevelopmentConfiguration.IS_DEVELOPMENT)
                    "http://localhost:20201/"
                else
                    "http://minecraft.goldenmine.kr:20301/"
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LauncherServerApi::class.java)
    }
}