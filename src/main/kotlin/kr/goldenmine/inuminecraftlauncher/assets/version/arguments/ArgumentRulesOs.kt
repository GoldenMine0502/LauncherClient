package kr.goldenmine.inuminecraftlauncher.assets.version.arguments

import com.google.gson.annotations.SerializedName

data class ArgumentRulesOs(
    @SerializedName("arch") val arch: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("version") val version: String?
)