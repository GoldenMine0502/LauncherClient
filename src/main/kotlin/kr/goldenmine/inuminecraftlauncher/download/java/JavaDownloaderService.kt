package kr.goldenmine.inuminecraftlauncher.download.java

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface JavaDownloaderService {
    @GET("file/download/{fileName}")
    fun downloadJava(@Path("fileName") fileName: String): Call<ResponseBody>

    @GET("file/check/{fileName}")
    fun checkFile(@Path("fileName") fileName: String): Call<String>
}