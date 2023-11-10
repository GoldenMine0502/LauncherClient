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
    private val launcherDirectories: LauncherDirectories,
    private val instanceSettings: InstanceSettings
) {
    private val log: Logger = LoggerFactory.getLogger(JavaRepository::class.java)

    private val downloaders = HashMap<OperatingSystem, IJavaDownloader>()

    var primary: File? = null
        private set

    init {
        updatePrimaryJava()
    }

    fun updatePrimaryJava() {
        downloaders[OperatingSystem.OSX] = IJavaDownloaderMac(instanceSettings)
        downloaders[OperatingSystem.WINDOWS] = IJavaDownloaderWindows(launcherDirectories, instanceSettings)
//        downloaders[OperatingSystem.WINDOWS] = I

        val downloader = downloaders[OperatingSystem.getOperatingSystem()]

        if (downloader != null) {
            try {
                downloader.download()
            } catch (ex: Exception) {
                log.error("an error occured while downloading java.", ex)
            }

            val javaList = downloader.findAllExistingJava()

            // logging for debug
            javaList.forEachIndexed { i, file ->
                log.info("java $i: ${file.absolutePath} ${file.exists()}")
            }

            if (javaList.isNotEmpty()) {
                primary = javaList.first()
                log.info("set java version to ${primary?.path}")
            } else {
                log.error("no java found.")
            }
        } else {
            log.error("unsupported operating system: ${OperatingSystem.getOperatingSystem()}")
        }
    }
}