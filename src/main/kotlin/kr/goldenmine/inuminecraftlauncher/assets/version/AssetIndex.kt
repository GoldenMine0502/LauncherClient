package kr.goldenmine.inuminecraftlauncher.assets.version

import com.google.gson.annotations.SerializedName

data class AssetIndex(
    @SerializedName("id") val id: String,
    @SerializedName("sha1") val sha1: String,
    @SerializedName("size") val size: Int,
    @SerializedName("totalSize") val totalSize: Int,
    @SerializedName("url") val url: String
)