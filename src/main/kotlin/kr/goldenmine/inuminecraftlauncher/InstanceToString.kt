package kr.goldenmine.inuminecraftlauncher

import com.google.gson.GsonBuilder
import java.io.File

fun main() {
    val instanceSettings = InstanceSettings(
        "1.16.5",
        "1.16",
        "36.2.34",
        8,
        mapOf(
            Pair("Mac", "jdk1.8.0_351.jdk"),
            Pair("Windows", "jdk8u351")
        ),
        "inu1165",
        listOf(
            "chiselsandbits-1.0.43.jar",
            "immersive-portals-0.17-mc1.16.5-forge.jar",
            "inumodelloader-1.3.4-SNAPSHOT.jar",
            "test.jar",
            "thutcore-1.16.4-8.2.0.jar",
            "thuttech-1.16.4-9.1.2.jar",
            "worldedit-mod-7.2.5-dist.jar"
        )
    )

    val gson = GsonBuilder().setPrettyPrinting().create()
    val file = File("references/${instanceSettings.instanceName}.json")
    file.bufferedWriter().use {
        gson.toJson(instanceSettings, it)
    }
}