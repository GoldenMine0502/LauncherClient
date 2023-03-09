package kr.goldenmine.inuminecraftlauncher.download

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ServerService {
    @GET("file/download/java/{os}/{fileName}")
    fun downloadJava(
        @Path("os") os: String,
        @Path("fileName") fileName: String
    ): Call<ResponseBody>

    @GET("file/download/versions")
    fun getInstanceSettings(): Call<List<String>>

    @GET("file/download/versions/{version}")
    fun getInstanceSetting(@Path("version") version: String): Call<InstanceSettings>

    @GET("file/download/mods/{modName}")
    fun downloadMod(@Path("modName") modName: String): Call<ResponseBody>

    @GET("file/download/options/{option}")
    fun downloadOption(@Path("option") option: String): Call<ResponseBody>

    @GET("file/download/shaders/{shader}")
    fun downloadShader(@Path("shader") shader: String): Call<ResponseBody>

    @GET("file/check/mods/{modName}")
    fun checkMod(@Path("modName") modName: String): Call<MD5Response>

    @GET("file/check/options/{option}")
    fun checkOption(@Path("option") option: String): Call<MD5Response>

    @GET("file/check/shaders/{shader}")
    fun checkShader(@Path("shader") shader: String): Call<MD5Response>

//    @GET("file/check/{fileName}")
//    fun checkFile(@Path("fileName") fileName: String): Call<String>
}