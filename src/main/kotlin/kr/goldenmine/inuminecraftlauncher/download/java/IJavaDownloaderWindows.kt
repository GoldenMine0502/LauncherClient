package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.Compress
import kr.goldenmine.inuminecraftlauncher.util.getFileMD5
import kr.goldenmine.inuminecraftlauncher.util.writeResponseBodyToDisk
import net.technicpack.utilslib.OperatingSystem
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.UnsupportedOperationException
import kotlin.streams.toList

@Slf4j
class IJavaDownloaderWindows(
    private val launcherDirectories: LauncherDirectories,
    private val instanceSettings: InstanceSettings
) : IJavaDownloader {
    private val log: Logger = LoggerFactory.getLogger(IJavaDownloaderWindows::class.java)

    override val operatingSystem: OperatingSystem
        get() = OperatingSystem.WINDOWS

    override fun download() {
        /* download java */
        val osName = operatingSystem.getName()
        val folder = File(launcherDirectories.javaDirectory, osName)
        val dstFolderName = instanceSettings.javaVersionSpecific[osName]
            ?: throw UnsupportedOperationException("no java for ${instanceSettings.instanceName} of $osName")

        val dstFileName = "$dstFolderName.zip"

        val dstFile = File(folder, dstFileName)

        val md5 = ServerRequest.SERVICE.checkJava(osName, dstFileName).execute().body()?.md5

        // 파일이 없거나 md5가 다르면(다운로드 실패인 상황) 다운로드
        var count = 0
        while(!dstFile.exists() || md5 != getFileMD5(dstFile)) {
            val response = ServerRequest.SERVICE.downloadJava(osName, dstFileName).execute()
            if(response.isSuccessful) {
                val body = response.body()
                if(body != null) {
                    writeResponseBodyToDisk(dstFile, body)
                }
            }
            count++
            if(count >= 5) {
                log.error("failed to download java.")
                break
            }
        }

        if(count < 5) {
            log.info("downloaded java is valid.")
        }

        /* unzip java */
        val dstFolder = File(folder, dstFolderName)
        val compress = Compress()
        compress.unZip(dstFile.path, dstFolder.path)
    }

    override fun findAllExistingJava(): List<File> {
        val routes = listOf(
            "C:/Program Files/Java",
            "C:/Program Files (x86)/Java"
        )

        val javaRoute = "bin/java.exe"

        val javaList = routes.flatMap { route ->
            val folder = File(route)

            val list = if (folder.exists()) folder.listFiles()?.filter { File(it, javaRoute).exists() }
                ?: listOf() else listOf()
            list.map { File(it.absolutePath, javaRoute) }
        }.toMutableList()

        /* downloaded java */
        val osName = operatingSystem.getName()
        val folder = File(launcherDirectories.javaDirectory, osName)
        val fileName = instanceSettings.javaVersionSpecific[osName]
            ?: throw UnsupportedOperationException("no java for ${instanceSettings.instanceName} of $osName")

        val downloadedFolder = File(folder, fileName)
        if(downloadedFolder.exists())
            javaList.add(File(downloadedFolder, javaRoute))

        return javaList.parallelStream().filter {
            checkVersionSame(it, instanceSettings.javaVersion)
        }.toList()
    }
}