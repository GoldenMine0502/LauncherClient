package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.streams.toList

@Slf4j
class IJavaDownloaderMac(
    private val instanceSettings: InstanceSettings
) : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderMac::class.java)

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem.OSX

    override fun download() {
        throw UnsupportedOperationException("macos should use local java.")
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
            checkVersionSame(it, instanceSettings.javaVersion)
        }.toList()
    }
}