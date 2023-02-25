package kr.goldenmine.inuminecraftlauncher.assets.forge

import com.google.gson.annotations.SerializedName
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Library


class MinecraftForgeInstall(
    @SerializedName("spec") val spec: Int,
    @SerializedName("profile") val profile: String,
    @SerializedName("version") val version: String,
    @SerializedName("icon") val icon: String, // base64
    @SerializedName("json") val json: String,
    @SerializedName("path") val path: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("minecraft") val minecraft: String,
    @SerializedName("welcome") val welcome: String,
    @SerializedName("mirrorList") val mirrorListUrl: String,
    @SerializedName("data") val data: Map<String, Data>,
    @SerializedName("processors") val processors: List<Processor>,
    @SerializedName("libraries") val libraries: List<Library>,
//    @SerializedName("") val:,
    ) {
}