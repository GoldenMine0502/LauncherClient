package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.download.DownloaderRequest
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

@Slf4j
class IJavaDownloaderMac : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderMac::class.java)

    override val destFile: File
        get() = File("mac/java.zip")
    override val requestFileName: String
        get() = "java_mac.zip"
    override val javaRoute: String
        get() = "Contents/Home/bin/java"

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem.OSX

    override fun download() {
        val response = DownloaderRequest.SERVICE.downloadFile(requestFileName).execute()

        if(response.isSuccessful) {
            val body = response.body()
            log.info("response is successful.")

            if(body != null) {
                writeResponseBodyToDisk(destFile, body)
                log.info("downloaded java for mac.")
            }
        }
    }

    override fun findAllExistingJava(): List<File> {
        val routes = listOf(
            "/Library/Java/JavaVirtualMachines/",
            "/System/Library/Java/JavaVirtualMachines/",
            "/System/Library/Frameworks/JavaVM.framework/",
            "/usr/libexec/java_home",
        )

        val javaList = routes.flatMap { route ->
            val folder = File(route)

            if(folder.exists())folder.listFiles()?.filter { File(it, javaRoute).exists() } ?: listOf() else listOf()
        }

        val defaultJava = File("usr/bin/java")
        return javaList + defaultJava
    }

    override fun getJavaVersionName(version: Int): String {
        if(version < 10) {
            return "1.$version"
        } else {
            return "$version"
        }
    }
}