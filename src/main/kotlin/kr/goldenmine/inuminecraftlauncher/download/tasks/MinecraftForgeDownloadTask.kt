package kr.goldenmine.inuminecraftlauncher.download.tasks

import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MinecraftForgeDownloadTask(
    private val launcherSettings: LauncherSettings,
    private val id: String,
    private val version: String = "best"
) : ITask<Boolean> {
    private val log: Logger = LoggerFactory.getLogger(MinecraftAssetDownloadTask::class.java)

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
                launcherSettings.logToGUI("downloading forge $version...")
                log.info("downloading version $version")

                if (version != null) {
                    val fileName = "forge-${version}-installer.jar"
                    val url =
                        "https://maven.minecraftforge.net/net/minecraftforge/forge/${version}/$fileName"
                    val body = AssetService.FORGE_API.downloadFromUrl(url).execute().body()

                    if (body != null) {
                        val file = File(launcherSettings.launcherDirectories.forgeDirectory, fileName)
                        writeResponseBodyToDisk(file, body)
                        return true
                    }
                }
            }
        }
        return false
    }
}