package kr.goldenmine.inuminecraftlauncher.download.tasks

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftPackage
import kr.goldenmine.inuminecraftlauncher.assets.version.AssetIndex
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import java.io.File

class MinecraftPackageDownloadTask(
    private val launcherDirectories: LauncherDirectories,
    private val version: AssetIndex
): ITask<MinecraftPackage> {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun download(): MinecraftPackage? {
        val minecraftPackage = AssetService.MINECRAFT_API.getPackageFromUrl(version.url).execute().body()

        if(minecraftPackage != null) {
            val file = File(launcherDirectories.assetsDirectory, "indexes/${version.id}.json")
            file.parentFile.mkdirs()

            file.bufferedWriter().use {
                gson.toJson(minecraftPackage, it)
            }
        }
        return minecraftPackage
    }
}