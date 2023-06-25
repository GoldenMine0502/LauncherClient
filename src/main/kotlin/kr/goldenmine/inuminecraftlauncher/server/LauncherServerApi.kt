package kr.goldenmine.inuminecraftlauncher.server

import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import kr.goldenmine.inuminecraftlauncher.server.models.ServerStatusResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.GET

interface LauncherServerApi {
    @POST("/account/random")
    fun requestRandomAccount(): Call<MinecraftAccount>

    @GET("/account/status")
    fun requestStatus(): Call<ServerStatusResponse>
}