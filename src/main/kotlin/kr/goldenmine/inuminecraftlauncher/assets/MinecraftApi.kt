package kr.goldenmine.inuminecraftlauncher.assets

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface MinecraftApi {
    @GET("mc/game/version_manifest.json")
    fun getVersionManifest(): Call<MinecraftVersionManifest>

    @GET("v1/packages/95af6e50cd04f06f65c76e4a62237504387e5480/1.16.5.json")
    fun getMinecraft1165(): Call<MinecraftVersion>

    @GET
    fun getVersionFromUrl(@Url url: String): Call<MinecraftVersion>

    @GET("/v1/packages/28680197f74e5e1d55054f6a63509c8298d428f9/1.16.json")
    fun getPackage116(): Call<MinecraftPackage>

    @GET
    fun getPackageFromUrl(@Url url: String): Call<MinecraftPackage>

    @GET
    fun downloadFromUrl(@Url url: String): Call<ResponseBody>
}