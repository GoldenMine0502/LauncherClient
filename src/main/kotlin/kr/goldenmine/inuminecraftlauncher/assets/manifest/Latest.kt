package kr.goldenmine.inuminecraftlauncher.assets.manifest

import com.google.gson.annotations.SerializedName

data class Latest(
    @SerializedName("release") val release: String,
    @SerializedName("snapshot") val snapshot: String
)