package kr.goldenmine.inuminecraftlauncher.assets.forge

import com.google.gson.annotations.SerializedName

class Data(
    @SerializedName("client") val client: String,
    @SerializedName("server") val server: String,
    ) {
}