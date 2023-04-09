package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.util.Compress
import kr.goldenmine.inuminecraftlauncher.util.deleteRecursive
import kr.goldenmine.inuminecraftlauncher.util.getFileMD5
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JavaRepository(
    instanceSettings: InstanceSettings
) {
    private val log: Logger = LoggerFactory.getLogger(JavaRepository::class.java)

    private val downloaders = HashMap<OperatingSystem, IJavaDownloader>()

    var primary: File? = null
        private set

    init {
        downloaders[OperatingSystem.OSX] = IJavaDownloaderMac()
        downloaders[OperatingSystem.WINDOWS] = IJavaDownloaderWindows(instanceSettings)
//        downloaders[OperatingSystem.WINDOWS] = I

        val downloader = downloaders[OperatingSystem.getOperatingSystem()]

        try {
            checkMD5Java()
            downloadJava()
            unzipJava()
        } catch(ex: Exception) {
            log.warn(ex.message, ex)

            if(downloader != null) {
                val javaList = downloader.findAllExistingJava()
                val best = downloader.getJavaVersionName(instanceSettings.javaVersion)

                val java = javaList.firstOrNull { it.name.contains(best) }

                if(java != null) {
                    primary = File(java, downloader.javaRoute)
                    log.info("set java version to ${primary?.path}")
                } else {
                    log.error("no java found.")
                }
            } else {
                log.error("unsupported operating system: ${OperatingSystem.getOperatingSystem().toString()}")
            }
        }
    }

    fun checkMD5Java() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
//            val md5 = ServerRequest.SERVICE.checkFile(downloader.requestFileName).execute().body()

            if(downloader.destFile.exists()) {
                val fileMd5 = getFileMD5(downloader.destFile)

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
    }

    fun downloadJava() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            log.info("destFile exists: ${downloader.destFile.exists()}")

            if (!downloader.destFile.exists()) {
                log.info("downloading java...")
                downloader.download()
                log.info("downloaded java.")
            }
        }
    }

    fun unzipJava() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            val file = downloader.destFile
            val directory = downloader.destFile.parentFile

            // unzip if the downloaded file doesn't unzipped
            val checkExists = directory.listFiles()?.filter { it.isDirectory }?.size ?: 0

            if(checkExists == 0) {
                println("unzipping")
                val compress = Compress()
                compress.unZip(file.path, directory.path)
            }
        }
    }

    fun setPrimaryDefault() {
        val downloader = downloaders[OperatingSystem.getOperatingSystem()]
        if (downloader != null) {
            val extractedFolder = downloader.destFile.parentFile.listFiles()?.filter { it.isDirectory }?.first()!!
            primary = File(extractedFolder, downloader.javaRoute)
        }
    }
}