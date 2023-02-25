package kr.goldenmine.inuminecraftlauncher.download

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersion
import kr.goldenmine.inuminecraftlauncher.assets.manifest.VersionInfo
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import java.io.File

class MinecraftJsonDownloadTask(
    private val launcherDirectories: LauncherDirectories,
    private val versionInfo: VersionInfo
): ITask<MinecraftVersion> {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun download(): MinecraftVersion? {
        val minecraftVersion = AssetService.MINECRAFT_API.getVersionFromUrl(versionInfo.url).execute().body()

        if(minecraftVersion != null) {
            val file = File(launcherDirectories.librariesDirectory, "versions/${versionInfo.id}.json")
            file.parentFile.mkdirs()
            if (!file.exists()) file.createNewFile()

            file.bufferedWriter().use {
                gson.toJson(minecraftVersion, it)
            }
        }

        return minecraftVersion
    }
}