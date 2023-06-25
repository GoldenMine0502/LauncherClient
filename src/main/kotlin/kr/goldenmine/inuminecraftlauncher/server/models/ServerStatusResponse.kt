package kr.goldenmine.inuminecraftlauncher.server.models

import com.google.gson.annotations.SerializedName

data class ServerStatusResponse(
    val availableCounts: Int,
    val totalCounts: Int
){

}