package kr.goldenmine.inuminecraftlauncher.assets.version.libraries

import com.google.gson.annotations.SerializedName

data class Artifact(
    @SerializedName("path") val path: String,
    @SerializedName("sha1") val sha1: String,
    @SerializedName("size") val size: Long,
    @SerializedName("url") val url: String
)