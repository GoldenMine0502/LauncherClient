package kr.goldenmine.inuminecraftlauncher.download

import kr.goldenmine.inuminecraftlauncher.DevelopmentConfiguration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


object ServerRequest {
    val SERVICE: ServerService = Retrofit.Builder()
//        .baseUrl("http://minecraft.goldenmine.kr:20200/")
        .baseUrl(
            if(DevelopmentConfiguration.IS_DEVELOPMENT_SERVER)
                "http://localhost:20301/"
            else
                "http://minecraft.goldenmine.kr:20301/"
        )
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(ServerService::class.java)
}
