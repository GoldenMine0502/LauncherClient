package kr.goldenmine.inuminecraftlauncher.assets.version

import com.google.gson.annotations.SerializedName

data class JavaVersion(
    @SerializedName("component") val component: String,
    @SerializedName("majorVersion") val majorVersion: Int,
)