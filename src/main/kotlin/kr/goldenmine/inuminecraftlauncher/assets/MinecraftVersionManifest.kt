package kr.goldenmine.inuminecraftlauncher.assets

import com.google.gson.annotations.SerializedName
import kr.goldenmine.inuminecraftlauncher.assets.manifest.Latest
import kr.goldenmine.inuminecraftlauncher.assets.manifest.VersionInfo

data class MinecraftVersionManifest(
    @SerializedName("versions") val versions: List<VersionInfo>,
    @SerializedName("latest") val latest: Latest

) {
    fun printAll() {
        println("Latest Release: ${latest.release}")
        println("Latest Snapshot: ${latest.snapshot}")
        for (versionInfo in versions) {
            println("ID: ${versionInfo.id}")
            println("URL: ${versionInfo.url}")
        }
    }
}