package kr.goldenmine.inuminecraftlauncher.assets.version.libraries

import com.google.gson.annotations.SerializedName

data class LibraryDownloads(
    @SerializedName("artifact") val artifact: Artifact,
    @SerializedName("classifiers") val classifiers: Map<String, Artifact>?,
)