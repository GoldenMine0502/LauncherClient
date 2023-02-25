package kr.goldenmine.inuminecraftlauncher.assets

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.ArgumentsDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration


class AssetService {
    companion object {
        val MINECRAFT_API: MinecraftApi = Retrofit.Builder()
            .baseUrl("https://launchermeta.mojang.com/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().registerTypeAdapter(
                        Arguments::class.java,
                        ArgumentsDeserializer()
                    ).create()
                )
            )
            .client(
                OkHttpClient
                    .Builder()
                    .readTimeout(Duration.ofSeconds(120))
                    .connectTimeout(Duration.ofSeconds(60))
                    .build()
            )
            .build()
            .create(MinecraftApi::class.java)

        val FORGE_API: ForgeApi = Retrofit.Builder()
            .baseUrl("https://files.minecraftforge.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForgeApi::class.java)
    }
}
