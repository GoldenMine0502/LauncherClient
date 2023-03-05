package kr.goldenmine.inuminecraftlauncher.launcher

import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.util.Compress
import kr.goldenmine.inuminecraftlauncher.util.OS_NAME
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MinecraftDataDownloader(
    private val launcherSettings: LauncherSettings
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftDataDownloader::class.java)

    fun downloadJava() {
        try {
            log.info("downloading java...")

            val javaFileName = launcherSettings.instanceSettings.javaVersionSpecific[OS_NAME]
                ?: throw RuntimeException("not supported OS")
            val javaFileNameWithExtension = "$javaFileName.zip"
            val javaRoute = File(launcherSettings.launcherDirectories.javaDirectory, "$OS_NAME/$javaFileName")
            val javaRouteWithExtension =
                File(launcherSettings.launcherDirectories.javaDirectory, "$OS_NAME/$javaFileNameWithExtension")

            // 존재하지 않을 때만 다운로드
            if (!javaRouteWithExtension.exists()) {
                val response = ServerRequest.SERVICE.downloadJava(OS_NAME, javaFileNameWithExtension).execute()
                if (!response.isSuccessful) throw RuntimeException("failed to download java. response is not successful.")

                val body = response.body() ?: throw RuntimeException("failed to download java. no body.")

                log.info("saving java to $javaRoute")
                writeResponseBodyToDisk(javaRouteWithExtension, body)

                log.info("unzipping java...")
                Compress().unZip(javaRouteWithExtension.absolutePath, javaRoute.absolutePath)
            } else {
                log.info("java already exists. $javaFileName")
            }
        } catch(ex: Exception) {
            ex.printStackTrace()
            log.info("using local java")
        }
    }

    fun downloadMods() {
        log.info("downloading mods...")

        val modsFolder = File(
            launcherSettings.launcherDirectories.instancesDirectory,
            "${launcherSettings.instanceSettings.instanceName}/mods"
        )
        modsFolder.mkdirs()

        launcherSettings.instanceSettings.mods.forEach { modName ->
            val modFile = File(modsFolder, modName)

            // 존재하지 않을 때만 다운로드
            if (!modFile.exists()) {
                val response = ServerRequest.SERVICE.downloadMod(modName).execute()
                if (!response.isSuccessful) throw RuntimeException("failed to download mod $modName. response is not successful.")

                val body = response.body() ?: throw RuntimeException("failed to download mod $modName. no body.")

                writeResponseBodyToDisk(modFile, body)
                log.info("downloaded mod $modName")
            } else {
                log.info("mod $modName already exists.")
            }
        }
    }

    fun download() {
        downloadJava()
        downloadMods()
    }
}