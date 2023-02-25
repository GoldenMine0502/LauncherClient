package kr.goldenmine.inuminecraftlauncher.assets.version.arguments

import com.google.gson.annotations.SerializedName

data class Argument(
    @SerializedName("rules") val rules: List<ArgumentRules>,
    @SerializedName("value") val value: Any?
)