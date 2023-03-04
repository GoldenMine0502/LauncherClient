package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

@Slf4j
class IJavaDownloaderWindows : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderWindows::class.java)

    override val destFile: File
        get() = File("windows/java.zip")
    override val requestFileName: String
        get() = "java_windows.zip"
    override val javaRoute: String
        get() = "bin/java.exe"

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem.OSX

    override fun download() {
        val response = ServerRequest.SERVICE.downloadJava("Windows").execute()

        if(response.isSuccessful) {
            val body = response.body()
            log.info("response is successful.")

            if(body != null) {
                writeResponseBodyToDisk(destFile, body)
                log.info("downloaded java for windows.")
            }
        }
    }

    override fun findAllExistingJava(): List<File> {
        val routes = listOf(
            "C:/Program Files/Java",
            "C:/Program Files (x86)/Java"
        )

        val javaList = routes.flatMap { route ->
            val folder = File(route)

            if(folder.exists())folder.listFiles()?.filter { File(it, javaRoute).exists() } ?: listOf() else listOf()
        }

        return javaList
    }

    override fun getJavaVersionName(version: Int): String {
        if(version < 10) {
            return "1.$version"
        } else {
            return "$version"
        }
    }
}