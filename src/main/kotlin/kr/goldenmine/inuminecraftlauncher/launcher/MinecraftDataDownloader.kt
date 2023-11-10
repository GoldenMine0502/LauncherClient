package kr.goldenmine.inuminecraftlauncher.launcher

import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.download.MD5Response
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.util.*
import okhttp3.ResponseBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call
import java.io.File

class MinecraftDataDownloader(
    private val launcherSettings: LauncherSettings
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftDataDownloader::class.java)

    fun downloadJava() {
        try {
//            launcherSettings.logToGUI("downloading java...")
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
        } catch (ex: Exception) {
            log.error(ex.message, ex)
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
            // 제대로 존재하지 않으면 다운로드
            downloadAutomatically(
                modsFolder,
                modName,
                "mod",
                { ServerRequest.SERVICE.downloadMod(modName) },
                { ServerRequest.SERVICE.checkMod(modName) },
            )
        }

        // delete old version
        modsFolder.listFiles()?.forEach { file ->
            val find = launcherSettings.instanceSettings.mods.any { file.name.contains(it) }

            if (!find) {
                log.info("deleting old version ${file.name}")
                file.delete()
            }
        }
    }

    fun downloadShader() {

        try {
            downloadAutomatically(
                File(launcherSettings.launcherDirectories.getInstanceDirectory(launcherSettings.instanceSettings.instanceName), "shaderpacks"),
                "${launcherSettings.instanceSettings.shader}.zip",
                "shader",
                { ServerRequest.SERVICE.downloadShader(launcherSettings.instanceSettings.shader) },
                { ServerRequest.SERVICE.checkShader(launcherSettings.instanceSettings.shader) }
            )
        } catch(ex: RuntimeException) {
            log.error(ex.message, ex)
        }
    }

    fun downloadOptions() {
        /*
        options.txt
        optionsof.txt
        optionsshaders.txt
         */

        try {
            val directory =
                launcherSettings.launcherDirectories.getInstanceDirectory(launcherSettings.instanceSettings.instanceName)

            val optionsStr = launcherSettings.instanceSettings.instanceName
            val optionsOfStr = "${optionsStr}of"
            val optionsShaderStr = "${optionsStr}shaders"

            downloadAutomatically(
                directory,
                "options.txt",
                "options",
                { ServerRequest.SERVICE.downloadOption(optionsStr) },
                { ServerRequest.SERVICE.checkOption(optionsStr) },
                false
            )

            downloadAutomatically(
                directory,
                "optionsof.txt",
                "options",
                { ServerRequest.SERVICE.downloadOption(optionsOfStr) },
                { ServerRequest.SERVICE.checkOption(optionsOfStr) },
                false
            )

            downloadAutomatically(
                directory,
                "optionsshaders.txt",
                "optionsshaders",
                { ServerRequest.SERVICE.downloadOption(optionsShaderStr) },
                { ServerRequest.SERVICE.checkOption(optionsShaderStr) },
                false
            )
        } catch(ex: RuntimeException) {
            log.error(ex.message, ex)
        }
    }

    fun download() {
        downloadJava()
        downloadMods()
        downloadShader()
        downloadOptions()
    }

    fun downloadAutomatically(
        directory: File,
        fileName: String,
        type: String,
        request: (param: String) -> Call<ResponseBody>,
        check: (param: String) -> Call<MD5Response>,
        checkMD5: Boolean = true,
        customFileName: String = fileName,
    ) {
        directory.mkdirs()
        val file = File(directory, customFileName)
//        if(!file.exists()) file.createNewFile()

        val md5Server = check.invoke(fileName).execute().body()?.md5

        if (!file.exists() || (checkMD5 && getFileMD5(file) != md5Server)) {
            launcherSettings.logToGUI("downloading $type $fileName...")
            log.info("server: $md5Server client: ${if(file.exists()) getFileMD5(file) else null}")

            val response = request.invoke(fileName).execute()
            if (!response.isSuccessful) throw RuntimeException("failed to download $type $fileName. response is not successful.")

            val body = response.body() ?: throw RuntimeException("failed to download $type $fileName. no body.")

            writeResponseBodyToDisk(file, body)
            log.info("downloaded $type $fileName")
        } else {
            log.info("$type $fileName already exists.")
        }
    }
}
