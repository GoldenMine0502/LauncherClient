package kr.goldenmine.inuminecraftlauncher.assets.version.arguments

import com.google.gson.annotations.SerializedName

data class Arguments(
    @SerializedName("game") val game: List<Any>,
    @SerializedName("jvm") val jvm: List<Any>
)