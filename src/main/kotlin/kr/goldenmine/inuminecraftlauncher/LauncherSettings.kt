package kr.goldenmine.inuminecraftlauncher

import kr.goldenmine.inuminecraftlauncher.download.java.JavaRepository
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.user.UserAdministrator

/*
                val temporaryDirectory = DefaultLauncherDirectories(File("inulauncher"))
                val microsoftAuthenticator = MicrosoftAuthenticator(File(temporaryDirectory.launcherDirectory, "oauth"))
                val users = TechnicUserStore.load(File(temporaryDirectory.launcherDirectory, "users.json"))
 */
class LauncherSettings(
    val launcherDirectories: LauncherDirectories,
    val instanceSettings: InstanceSettings,
    val width: Int? = null,
    val height: Int? = null,
//    val overrideJavaPath: String? = null,
) {
    val userAdministrator = UserAdministrator(launcherDirectories)
    val javaRepository = JavaRepository(instanceSettings)
}