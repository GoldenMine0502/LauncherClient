package kr.goldenmine.inuminecraftlauncher

import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import net.technicpack.launcher.io.TechnicUserStore
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftAuthenticator
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser
import net.technicpack.minecraftcore.microsoft.auth.UserModel
import java.io.File

class UserAdministrator(launcherDirectories: LauncherDirectories) {
    val microsoftAuthenticator = MicrosoftAuthenticator(File(launcherDirectories.launcherDirectory, "oauth"))
    val users: TechnicUserStore = TechnicUserStore.load(File(launcherDirectories.launcherDirectory, "users.json"))
    private val userModel = UserModel(users, microsoftAuthenticator)

    fun login(): MicrosoftUser {
        return if(users.users.isEmpty()) {
            val microsoftUser = userModel.microsoftAuthenticator.loginNewUser()
            users.addUser(microsoftUser)
            users.save()
            microsoftUser
        } else { // refresh
            val user = users.getUser(users.users.first())
            userModel.microsoftAuthenticator.refreshSession(user as MicrosoftUser)
            user
        }
    }
}