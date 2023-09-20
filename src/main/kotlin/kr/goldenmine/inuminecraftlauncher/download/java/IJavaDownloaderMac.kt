package kr.goldenmine.inuminecraftlauncher.download.java

import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.naming.OperationNotSupportedException

@Slf4j
class IJavaDownloaderMac : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderMac::class.java)

    override val javaRoute: String
        get() = "Contents/Home/bin/java"

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem.OSX

    override fun getFile(): File {
        throw OperationNotSupportedException("macos should use local java.")
    }


    override fun download() {
        throw OperationNotSupportedException("macos should use local java.")
//        val response = ServerRequest.SERVICE.downloadJava("Mac", "").execute()
//
//        if(response.isSuccessful) {
//            val body = response.body()
//            log.info("response is successful.")
//
//            if(body != null) {
//                writeResponseBodyToDisk(destFile, body)
//                log.info("downloaded java for mac.")
//            }
//        }
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

            val list = if(folder.exists())folder.listFiles()?.filter { File(it, javaRoute).exists() } ?: listOf() else listOf()
            list.map { File(it.absolutePath, javaRoute) }
        }

        val defaultJava = File("/usr/bin/java")
        return if(defaultJava.exists()) javaList + defaultJava else javaList
    }

    override fun getJavaVersionName(version: Int): List<String> {
        if(version < 10) {
            return listOf("1.$version", "-$version")
        } else {
            return listOf("$version")
        }
    }
}