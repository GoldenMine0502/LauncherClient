package kr.goldenmine.inuminecraftlauncher.assets.version.downloads

import com.google.gson.annotations.SerializedName

data class Server(
    @SerializedName("sha1") val sha1: String,
    @SerializedName("size") val size: Long,
    @SerializedName("url") val url: String
)