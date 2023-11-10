package kr.goldenmine.inuminecraftlauncher

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.instances.getDefaultInstance
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import kr.goldenmine.inuminecraftlauncher.ui.DefaultLoggerGUI
import kr.goldenmine.inuminecraftlauncher.ui.MainFrame
import kr.goldenmine.inuminecraftlauncher.ui.MainFrameController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

// 마인크래프트 런쳐를 위한 api
// https://github.com/tomsik68/mclauncher-api
object Main {

    private val log: Logger = LoggerFactory.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isNotEmpty()) {
            DevelopmentConfiguration.IS_DEVELOPMENT_SERVER = args[0].toBoolean()
            if(args.size >= 2)
                DevelopmentConfiguration.IS_DEVELOPMENT_LOGGER = args[1].toBoolean()
        }

        val mainFolder = File("inulauncher")
        val version = getDefaultInstance().instanceName

        val instanceSettings = try {
            ServerRequest.SERVICE.getInstanceSetting(version).execute().body()
        } catch(ex: Exception) {
            null
        }

        log.info(GsonBuilder().setPrettyPrinting().create().toJson(instanceSettings))
        val mainFrame = MainFrame(instanceSettings?.version)

        val launcherDirectories = DefaultLauncherDirectories(mainFolder)

        if(instanceSettings != null) {
            val loggerGUI = DefaultLoggerGUI(mainFrame)
            val launcherSettings = LauncherSettings(
                launcherDirectories,
                instanceSettings,
                width = 854,
                height = 480,
                loggerGUI = loggerGUI
//            overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java"
            )

            launcherSettings.logToGUI("")
            launcherSettings.logToGUI("==================================")
            launcherSettings.logToGUI("디스코드 입장을 권장합니다: https://discord.gg/4MXcmE67UU")
            launcherSettings.logToGUI("==================================")
            launcherSettings.logToGUI("")
            launcherSettings.logToGUI("실행시 프로그램 설치 경로에 영어만 있어야 합니다.")

            val mainFrameController = MainFrameController(launcherSettings, mainFrame)
            mainFrameController.init()
        } else {
            mainFrame.logArea.append("failed to connect server.\nplease restart this program.")
        }
        //        MinecraftOptions options = new MinecraftOptions(new File("java/jdk-8u202/bin/java"), new ArrayList<>(), 36);
    }
}