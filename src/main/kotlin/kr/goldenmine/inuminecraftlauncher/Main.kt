package kr.goldenmine.inuminecraftlauncher

import io.github.bonigarcia.wdm.WebDriverManager
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import kr.goldenmine.inuminecraftlauncher.ui.MainFrame
import kr.goldenmine.inuminecraftlauncher.ui.MainFrameController
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

        val mainFrame = MainFrame()
        val launcherDirectories = DefaultLauncherDirectories(File("inulauncher"))
        val instanceSettings = InstanceSettings("1.16.5", "1.16", "36.2.34", 8, "inu1165", listOf())
        val launcherSettings = LauncherSettings(
            launcherDirectories,
            instanceSettings,
            width = 854,
            height = 480,
//            overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java"
        )
        val mainFrameController = MainFrameController(launcherSettings, mainFrame)
        mainFrameController.init()
        //        MinecraftOptions options = new MinecraftOptions(new File("java/jdk-8u202/bin/java"), new ArrayList<>(), 36);
    }
}