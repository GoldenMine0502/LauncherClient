package kr.goldenmine.inuminecraftlauncher.assets.assets

import com.google.gson.annotations.SerializedName

data class MinecraftAsset(
    @SerializedName("hash") val hash: String,
    @SerializedName("size") val size: Int
)