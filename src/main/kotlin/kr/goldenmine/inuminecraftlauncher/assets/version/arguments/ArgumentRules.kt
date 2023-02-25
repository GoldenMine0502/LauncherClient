package kr.goldenmine.inuminecraftlauncher.assets.version.arguments

import com.google.gson.annotations.SerializedName

data class ArgumentRules(
    @SerializedName("action") val action: String, // allow or disallow
    @SerializedName("features") val features: Map<String, Any>?,
    @SerializedName("os") val os: ArgumentRulesOs?
)