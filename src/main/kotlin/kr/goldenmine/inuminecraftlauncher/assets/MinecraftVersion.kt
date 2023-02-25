package kr.goldenmine.inuminecraftlauncher.assets

import com.google.gson.annotations.SerializedName
import kr.goldenmine.inuminecraftlauncher.assets.version.*
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.downloads.Downloads
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Library
import kr.goldenmine.inuminecraftlauncher.assets.version.logging.Logging

data class MinecraftVersion(
    @SerializedName("arguments") val arguments: Arguments,
    @SerializedName("assetIndex") val assetIndex: AssetIndex,
    @SerializedName("assets") val assets: String,
    @SerializedName("complianceLevel") val complianceLevel: Int,
    @SerializedName("id") val id: String,
    @SerializedName("javaVersion") val javaVersion: JavaVersion,
    @SerializedName("libraries") val libraries: List<Library>,
    @SerializedName("downloads") val downloads: Downloads,

    @SerializedName("logging") val logging: Logging,
    @SerializedName("mainClass") val mainClass: String,
    @SerializedName("minimumLauncherVersion") val minimumLauncherVersion: Int,
    @SerializedName("releaseTime") val releaseTime: String,
    @SerializedName("time") val time: String,
    @SerializedName("type") val type: String,
) {
    fun printAll() {
        println("=== game ===")
        for (argumentGame in arguments.game) {
            println(argumentGame.toString())
        }

        println("=== jvm ===")
        for (argumentJvm in arguments.jvm) {
            println(argumentJvm.toString())
        }

        println("=== asset index ===")
        println(assetIndex)

        println("=== assets ===")
        println(assets)

        println("=== complianceLevel ==")
        println(complianceLevel)

        println("=== downloads ==")
        println("Client SHA1: ${downloads.client.sha1}")
        println("Client Size: ${downloads.client.size}")
        println("Client URL: ${downloads.client.url}")
        println("Server SHA1: ${downloads.server.sha1}")
        println("Server Size: ${downloads.server.size}")
        println("Server URL: ${downloads.server.url}")

        println("=== id ===")
        println(id)

        println("=== javaVersion ===")
        println(javaVersion)

        println("=== library ===")
        for (library in libraries) {
            println("    === ${library.name} ===")
            println("    sha1: ${library.downloads.artifact.sha1}")
            println("    url: ${library.downloads.artifact.url}")
            println("    size: ${library.downloads.artifact.size}")
            println("    path: ${library.downloads.artifact.path}")
            println("    rules: ${library.rules}")
            println("    natives: ${library.natives}")
            println("    extract: ${library.extract}")
            println("    classifiers: ${library.downloads.classifiers}")
        }

        println("=== logging ===")
        println(logging.client)
    }
}