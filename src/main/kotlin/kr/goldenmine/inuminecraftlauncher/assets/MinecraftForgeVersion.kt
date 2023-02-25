package kr.goldenmine.inuminecraftlauncher.assets

import com.google.gson.annotations.SerializedName
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Library
import kr.goldenmine.inuminecraftlauncher.assets.version.logging.Logging

data class MinecraftForgeVersion(
    @SerializedName("id") val id: String,
    @SerializedName("time") val time: String,
    @SerializedName("releaseTime") val releaseTime: String,
    @SerializedName("type") val type: String,
    @SerializedName("mainClass") val mainClass: String,
    @SerializedName("inheritsFrom") val inheritsFrom: String,
    @SerializedName("logging") val logging: Logging,
    @SerializedName("arguments") val arguments: Arguments,
    @SerializedName("libraries") val libraries: List<Library>,
    @SerializedName("_comment_") val comment: List<String>
)