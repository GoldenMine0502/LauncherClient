package kr.goldenmine.inuminecraftlauncher.assets.manifest

import com.google.gson.annotations.SerializedName

data class VersionInfo(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String
)