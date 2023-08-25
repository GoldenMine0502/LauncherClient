package kr.goldenmine.inuminecraftlauncher.download.tasks

import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.assets.assets.MinecraftAsset
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.getFileSHA1
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MinecraftAssetDownloadTask(
    private val launcherSettings: LauncherSettings,
    private val fileName: String,
    private val asset: MinecraftAsset
) : ITask<Boolean> {

    private val log: Logger = LoggerFactory.getLogger(MinecraftAssetDownloadTask::class.java)

    // if a download is success or already exist, return true
    override fun download(): Boolean {
        val file = File(launcherSettings.launcherDirectories.assetsDirectory, "objects/${asset.hash.substring(0, 2)}/${asset.hash}")
        file.parentFile.mkdirs()

        if (checkHash(file)) {
            log.info("already the asset exists: $fileName")
            return true
        }

        val url = "https://resources.download.minecraft.net/${asset.hash.substring(0, 2)}/${asset.hash}"

        launcherSettings.logToGUI("downloading asset ${asset.hash}...")

        val response = AssetService.MINECRAFT_API.downloadFromUrl(url).execute()
        if (response.isSuccessful) {
            val body = response.body()

            if (body != null) {
                writeResponseBodyToDisk(file, body)
                return checkHash(file)
            }
        }

        return false
    }

    fun checkHash(file: File): Boolean {
        if (file.exists()) {
            val sha1 = getFileSHA1(file)

            return sha1 == asset.hash && file.length().toInt() == asset.size
        }

        return false
    }
}