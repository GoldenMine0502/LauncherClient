package kr.goldenmine.inuminecraftlauncher

import kr.goldenmine.inuminecraftlauncher.download.tasks.MinecraftLibraryDownloadTask
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import net.technicpack.launcher.io.TechnicUserStore
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftAuthenticator
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser
import net.technicpack.minecraftcore.microsoft.auth.UserModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class UserAdministrator(launcherDirectories: LauncherDirectories) {
    private val log: Logger = LoggerFactory.getLogger(UserAdministrator::class.java)

    val microsoftAuthenticator = MicrosoftAuthenticator(File(launcherDirectories.launcherDirectory, "oauth"))
    val users: TechnicUserStore = TechnicUserStore.load(File(launcherDirectories.launcherDirectory, "users.json"))
    private val userModel = UserModel(users, microsoftAuthenticator)

    fun login(): MicrosoftUser {
        return if(users.users.isEmpty()) {
            val microsoftUser = userModel.microsoftAuthenticator.loginNewUser()
            users.addUser(microsoftUser)
            microsoftUser
        } else { // refresh
            try {
                val user = users.getUser(users.users.first())
                userModel.microsoftAuthenticator.refreshSession(user as MicrosoftUser)
//                users.save()

                user
            } catch(ex: Exception) {
                log.info("refresh token is invalid.")
                throw ex
            }
        }
    }
}