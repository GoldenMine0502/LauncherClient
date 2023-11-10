package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.util.runProcessAndGetAllText
import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.regex.Pattern
import javax.naming.OperationNotSupportedException
import kotlin.streams.toList

@Slf4j
class IJavaDownloaderMac(
    private val instanceSettings: InstanceSettings
) : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderMac::class.java)

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
        // 확인했던 경로들. 다른 경로들도 있을 수 있음
        val routes = listOf(
            "/Library/Java/JavaVirtualMachines/",
            "/System/Library/Java/JavaVirtualMachines/",
            "/System/Library/Frameworks/JavaVM.framework/",
            "/usr/libexec/java_home",
        )

        val javaRoute = "Contents/Home/bin/java"

        val javaList = routes.flatMap { route ->
            val folder = File(route)

            val list = if(folder.exists())folder.listFiles()?.filter { File(it, javaRoute).exists() } ?: listOf() else listOf()
            list.map { File(it.absolutePath, javaRoute) }
        }.toMutableList()

        val defaultJava = File("/usr/bin/java")
        javaList.add(defaultJava)

        return javaList.parallelStream().filter {
            checkVersionSame(it)
        }.toList()
    }

    private val versionSplitPattern = Pattern.compile("[\\._]")

    fun checkVersionSame(javaPath: File): Boolean {
        // example
        // java version "17.0.6" 2023-01-17 LTS
        // Java(TM) SE Runtime Environment (build 17.0.6+9-LTS-190)
        // Java HotSpot(TM) 64-Bit Server VM (build 17.0.6+9-LTS-190, mixed mode, sharing)
        val text = runProcessAndGetAllText(listOf(javaPath.absolutePath, "-version"))[0]
        val javaString = text.substring(text.indexOf('\"') + 1, text.lastIndexOf('\"'))
        val split = javaString.split(versionSplitPattern).map { it.toInt() }
        log.info("$javaString $split")

        return if(instanceSettings.javaVersion < 10) {
            split[0] == 1 && split[1] == instanceSettings.javaVersion
        } else { // after java 11
            split[0] == instanceSettings.javaVersion
        }
    }
}