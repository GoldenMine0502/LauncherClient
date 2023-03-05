package kr.goldenmine.inuminecraftlauncher

import com.google.gson.GsonBuilder
import io.github.bonigarcia.wdm.WebDriverManager
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import kr.goldenmine.inuminecraftlauncher.ui.MainFrame
import kr.goldenmine.inuminecraftlauncher.ui.MainFrameController
import kr.goldenmine.inuminecraftlauncher.util.OS_NAME_MAC
import kr.goldenmine.inuminecraftlauncher.util.OS_NAME_WINDOWS
import java.io.File

// 마인크래프트 런쳐를 위한 api
// https://github.com/tomsik68/mclauncher-api
object Main {
    @JvmStatic
    fun main(args: Array<String>) {

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
        val versionFile = File("version.txt")
        val version = versionFile.readText()

        val instanceSettings = ServerRequest.SERVICE.getInstanceSetting(version).execute().body()

        if(instanceSettings != null) {
            println(GsonBuilder().setPrettyPrinting().create().toJson(instanceSettings))
            val mainFrame = MainFrame()
            val launcherDirectories = DefaultLauncherDirectories(File("inulauncher"))
//        val instanceSettings = InstanceSettings(
//            "1.16.5",
//            "1.16",
//            "36.2.34",
//            8,
//            mapOf(
//                Pair(OS_NAME_MAC, "jdk1.8.0_351.jdk"),
//                Pair(OS_NAME_WINDOWS, "jdk8u351")
//            ),
//            "inu1165",
//            "minecraft.goldenmine.kr",
//            20000,
//            listOf(
//                "chiselsandbits-1.0.43.jar",
//                "immersive-portals-0.17-mc1.16.5-forge.jar",
//                "inumodelloader-1.3.4-SNAPSHOT.jar",
//                "test.jar",
//                "thutcore-1.16.4-8.2.0.jar",
//                "thuttech-1.16.4-9.1.2.jar",
//                "worldedit-mod-7.2.5-dist.jar"
//            )
//        )
            val launcherSettings = LauncherSettings(
                launcherDirectories,
                instanceSettings,
                width = 854,
                height = 480,
//            overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java"
            )
            val mainFrameController = MainFrameController(launcherSettings, mainFrame)
            mainFrameController.init()
        } else {
            println("no instance settings. exit the program. $version")
        }
        //        MinecraftOptions options = new MinecraftOptions(new File("java/jdk-8u202/bin/java"), new ArrayList<>(), 36);
    }
}