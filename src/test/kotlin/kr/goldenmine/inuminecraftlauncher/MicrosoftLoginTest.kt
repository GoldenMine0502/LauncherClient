package kr.goldenmine.inuminecraftlauncher

import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.OS_NAME_MAC
import kr.goldenmine.inuminecraftlauncher.util.OS_NAME_WINDOWS
import org.junit.jupiter.api.Test
import java.io.File

class MicrosoftLoginTest {
    @Test
    fun test() {
        val launcherDirectories = DefaultLauncherDirectories(File("inulauncher"))

        val instanceSettings = InstanceSettings(
            "1.16.5",
            "1.16",
            "36.2.34",
            8,

            mapOf(
                Pair(OS_NAME_MAC, "jdk1.8.0_351.jdk"),
                Pair(OS_NAME_WINDOWS, "jdk8u351")
            ),
            1024,
            4096,
            "inu1165",
            "minecraft.goldenmine.kr",
            20000,
            listOf(),
            "test",
            "test"
        )

//        val launcherSettings = LauncherSettings(
//            launcherDirectories,
//            instanceSettings,
//            width = 854,
//            height = 480,
////            overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java"
//            loggerGUI = DefaultLoggerGUI()
//        )

//        launcherSettings.userAdministrator.microsoftAuthenticator.loginNewUser()
    }
}