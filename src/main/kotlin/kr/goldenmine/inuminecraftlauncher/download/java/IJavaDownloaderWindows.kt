package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.OS_NAME_WINDOWS
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

@Slf4j
class IJavaDownloaderWindows(
    private val launcherDirectories: LauncherDirectories,
    private val instanceSettings: InstanceSettings
) : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderWindows::class.java)

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem.WINDOWS

    override fun getFile(): File {
        val windowsFileName =
            instanceSettings.javaVersionSpecific[OS_NAME_WINDOWS] ?: throw RuntimeException("no java for windows.")
        val file = File(launcherDirectories.javaDirectory, "windows/$windowsFileName.zip")

        return file
    }

    override fun download() {
        val file = getFile()

        val response = ServerRequest.SERVICE.downloadJava(OS_NAME_WINDOWS, file.name).execute()

        if (response.isSuccessful) {
            val body = response.body()
            log.info("response is successful.")

            if (body != null) {
                writeResponseBodyToDisk(file, body)
                log.info("downloaded java for windows.")
            }
        }
    }

    override fun findAllExistingJava(): List<File> {
        val routes = listOf(
            "C:/Program Files/Java",
            "C:/Program Files (x86)/Java"
        )

        val javaRoute = "bin/java.exe"

        val javaList = routes.flatMap { route ->
            val folder = File(route)

            val list = if (folder.exists()) folder.listFiles()?.filter { File(it, javaRoute).exists() } ?: listOf() else listOf()
            list.map { File(it.absolutePath, javaRoute) }
        }

        return javaList
    }
}