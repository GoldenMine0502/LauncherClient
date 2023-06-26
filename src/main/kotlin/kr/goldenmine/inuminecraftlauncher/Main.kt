package kr.goldenmine.inuminecraftlauncher

import com.google.gson.GsonBuilder
import io.github.bonigarcia.wdm.WebDriverManager
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
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
            DevelopmentConfiguration.IS_DEVELOPMENT = args[0].toBoolean()
        }

        WebDriverManager.chromedriver().setup()
//        val builder = SpringApplicationBuilder(CoreMain::class.java)
//        builder.headless(false)
//        val context = builder.run(*args)

        /*
chiselsandbits-1.0.43.jar
ftb-backups-2.1.2.2.jar
immersive-portals-0.17-mc1.16.5-forge.jar
inumodelloader-1.1.2-SNAPSHOT.jar.disabled
inumodelloader-1.2.3-SNAPSHOT.jar.disabled
inumodelloader-1.2.4-SNAPSHOT.jar.disabled
inumodelloader-1.2.8-SNAPSHOT.jar.disabled
inumodelloader-1.3.0-SNAPSHOT.jar.disabled
inumodelloader-1.3.2-SNAPSHOT.jar.disabled
inumodelloader-1.3.4-SNAPSHOT.jar
test.jar
thutcore-1.16.4-8.2.0.jar
thuttech-1.16.4-9.1.2.jar
worldedit-mod-7.2.5-dist.jar
         */
        val mainFolder = File("inulauncher")
        val versionFile = File(mainFolder, "version.txt")
        val version = versionFile.readText()

        val instanceSettings = ServerRequest.SERVICE.getInstanceSetting(version).execute().body()

        log.info(GsonBuilder().setPrettyPrinting().create().toJson(instanceSettings))
        val mainFrame = MainFrame(instanceSettings?.version)

        val launcherDirectories = DefaultLauncherDirectories(mainFolder)

        if(instanceSettings != null) {
            val launcherSettings = LauncherSettings(
                launcherDirectories,
                instanceSettings,
                width = 854,
                height = 480,
//            overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java"
            )
            val mainFrameController = MainFrameController(launcherSettings, mainFrame)
            mainFrameController.init()
        }
        //        MinecraftOptions options = new MinecraftOptions(new File("java/jdk-8u202/bin/java"), new ArrayList<>(), 36);
    }
}