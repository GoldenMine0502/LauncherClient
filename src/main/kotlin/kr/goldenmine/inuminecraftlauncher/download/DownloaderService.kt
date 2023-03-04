package kr.goldenmine.inuminecraftlauncher.download

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DownloaderService {
    @GET("file/download/{fileName}")
    fun downloadFile(@Path("fileName") fileName: String): Call<ResponseBody>

    @GET("file/check/{fileName}")
    fun checkFile(@Path("fileName") fileName: String): Call<String>
}