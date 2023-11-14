package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.ui.Loggable
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class JavaRepository(
    private val launcherDirectories: LauncherDirectories,
    private val instanceSettings: InstanceSettings,
    private val guilogger: Loggable? = null,
) {
    private val log: Logger = LoggerFactory.getLogger(JavaRepository::class.java)

    private val downloaders = HashMap<OperatingSystem, IJavaDownloader>()

    var primary: File? = null
        private set

    fun updatePrimaryJava() {
        downloaders[OperatingSystem.OSX] = IJavaDownloaderMac(instanceSettings)
        downloaders[OperatingSystem.WINDOWS] = IJavaDownloaderWindows(launcherDirectories, instanceSettings, guilogger)
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
                guilogger?.info("자바가 존재하지 않습니다.")
                guilogger?.info("자바 ${instanceSettings.javaVersion}을 다운로드해 주십시오.")
                log.error("no java found.")
            }
        } else {
            log.error("unsupported operating system: ${OperatingSystem.getOperatingSystem()}")
        }
    }
}