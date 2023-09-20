package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.Compress
import kr.goldenmine.inuminecraftlauncher.util.getFileMD5
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JavaRepository(
    launcherDirectories: LauncherDirectories,
    instanceSettings: InstanceSettings
) {
    private val log: Logger = LoggerFactory.getLogger(JavaRepository::class.java)

    private val downloaders = HashMap<OperatingSystem, IJavaDownloader>()

    var primary: File? = null
        private set

    init {
        downloaders[OperatingSystem.OSX] = IJavaDownloaderMac()
        downloaders[OperatingSystem.WINDOWS] = IJavaDownloaderWindows(launcherDirectories, instanceSettings)
//        downloaders[OperatingSystem.WINDOWS] = I

        val downloader = downloaders[OperatingSystem.getOperatingSystem()]

        try {
            downloadJava()
            unzipJava()
            setPrimaryDefault()
        } catch (ex: Exception) {
            log.warn(ex.message, ex)

            if (downloader != null) {
                val javaList = downloader.findAllExistingJava()
                javaList.forEach {
                    log.info(it.absolutePath)
                }
                val bestVersions = downloader.getJavaVersionName(instanceSettings.javaVersion)

                val java = javaList.firstOrNull { file->
                    val c = bestVersions.count { version -> file.name.contains(version) }

                    c > 0
                }

                if (java != null) {
                    primary = java
                    log.info("set java version to ${primary?.path}")
                } else {
                    log.error("no java found.")
                }
            } else {
                log.error("unsupported operating system: ${OperatingSystem.getOperatingSystem()}")
            }
        }
    }

    fun checkMD5Java(): Boolean {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            val file = downloader.getFile()
            val md5 = ServerRequest.SERVICE.checkJava(downloader.operatingSystem.getName(), downloader.getFile().name)
                .execute().body()

            if (file.exists()) {
                val fileMd5 = getFileMD5(file)
                if (md5?.md5 == fileMd5) {
                    return true
                }
                // TODO download java automatically
//                if (md5 == null) {
//                    throw RuntimeException("the file doesn't exist on the server")
//                }

//                if (md5 != fileMd5) {
//                    deleteRecursive(downloader.destFile.parentFile)
//                }
            } else {
                log.info("no java. it should be downloaded.")
            }
        } else {
            throw RuntimeException("no downloader for current os")
        }
        return false
    }

    fun downloadJava() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            var count = 0
            while (!checkMD5Java()) {
                log.info("downloading java...")
                downloader.download()
                log.info("downloaded java.")
                count++
                if(count >= 5) {
                    throw RuntimeException("severaly failed to download java. ")
                }
            }
        }
    }

    fun unzipJava() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            val file = downloader.getFile()
            val directory = file.parentFile

            // unzip if the downloaded file doesn't unzipped
            val checkExists = directory.listFiles()?.filter { it.isDirectory }?.size ?: 0

            if (checkExists == 0) {
                println("unzipping")
                val compress = Compress()
                compress.unZip(file.path, directory.path)
            }
        }
    }

    fun setPrimaryDefault() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            val file = downloader.getFile()
            if(!file.exists()) {
                throw RuntimeException("java doesnt exist")
            }
            primary = downloader.getFile()
            log.info("set java version to (default) ${primary?.path}")
        }
    }
}