package kr.goldenmine.inuminecraftlauncher.assets.version.libraries

import com.google.gson.annotations.SerializedName

data class Library(
    @SerializedName("downloads") val downloads: LibraryDownloads,
    @SerializedName("name") val name: String,
    @SerializedName("rules") val rules: List<LibraryRules>?,
    @SerializedName("natives") val natives: Map<String, String>?,
    @SerializedName("extract") val extract: Extract,
)