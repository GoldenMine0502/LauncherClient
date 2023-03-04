package kr.goldenmine.inuminecraftlauncher

import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import org.junit.jupiter.api.Test
import java.io.File

class MicrosoftLoginTest {
    @Test
    fun test() {
        val launcherDirectories = DefaultLauncherDirectories(File("inulauncher"))
        val instanceSettings = InstanceSettings("1.16.5", "1.16", "36.2.34", 8, true)
        val launcherSettings = LauncherSettings(
            launcherDirectories,
            instanceSettings,
            width = 854,
            height = 480,
//            overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java"
        )

        launcherSettings.userAdministrator.microsoftAuthenticator.loginNewUser()
    }
}