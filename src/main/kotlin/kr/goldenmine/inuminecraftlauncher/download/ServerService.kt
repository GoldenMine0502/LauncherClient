package kr.goldenmine.inuminecraftlauncher.download

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ServerService {
    @GET("file/download/java/{os}/")
    fun downloadJava(
        @Path("os") os: String
    ): Call<ResponseBody>

    @GET("file/download/versions")
    fun getInstanceSettings(): Call<List<String>>

    @GET("file/download/version")
    fun getInstanceSetting(@Query("version") version: String): Call<InstanceSettings>

    @GET("file/download/mods/{modName}")
    fun getMod(@Path("modName") modName: String)
//    @GET("file/check/{fileName}")
//    fun checkFile(@Path("fileName") fileName: String): Call<String>
}