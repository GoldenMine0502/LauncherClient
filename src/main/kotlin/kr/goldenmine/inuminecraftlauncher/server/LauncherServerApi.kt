package kr.goldenmine.inuminecraftlauncher.server

import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface LauncherServerApi {
    @POST("/account/random")
    fun requestRandomAccount(): Call<MinecraftAccount>
}