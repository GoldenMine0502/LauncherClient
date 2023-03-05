package kr.goldenmine.inuminecraftlauncher.download.tasks

import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import java.io.File

class MinecraftForgeDownloadTask(
    private val launcherDirectories: LauncherDirectories,
    private val id: String,
    private val version: String = "best"
) : ITask<Boolean> {
    override fun download(): Boolean {
        val repository = AssetService.FORGE_API.getVersionRepository().execute().body()
        if (repository != null) {
            val versions = repository[id]

            if (versions != null) {
                val version =
                    if (version == "best")
                        versions.lastOrNull()
                    else
                        versions.firstOrNull { it == version || it == "$id-$version" }

//                println(repository)
                println("downloading version $version")

                if (version != null) {
                    val fileName = "forge-${version}-installer.jar"
                    val url =
                        "https://maven.minecraftforge.net/net/minecraftforge/forge/${version}/$fileName"
                    val body = AssetService.FORGE_API.downloadFromUrl(url).execute().body()

                    if (body != null) {
                        val file = File(launcherDirectories.forgeDirectory, fileName)
                        writeResponseBodyToDisk(file, body)
                        return true
                    }
                }
            }
        }
        return false
    }

}