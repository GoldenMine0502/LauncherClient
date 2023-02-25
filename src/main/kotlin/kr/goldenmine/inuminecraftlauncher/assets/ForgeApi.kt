package kr.goldenmine.inuminecraftlauncher.assets

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ForgeApi {
    @GET("net/minecraftforge/forge/maven-metadata.json")
    fun getVersionRepository(): Call<Map<String, List<String>>>

    @GET
    fun downloadFromUrl(@Url url: String): Call<ResponseBody>
}