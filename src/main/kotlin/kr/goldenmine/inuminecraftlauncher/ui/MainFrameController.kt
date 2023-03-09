package kr.goldenmine.inuminecraftlauncher.ui

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftCommandBuilder
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftDataDownloader
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftDownloader
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftLauncher
import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import kr.goldenmine.inuminecraftlauncher.util.MoveToTheBottom
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import java.util.concurrent.ExecutionException

class MainFrameController(
    private val launcherSettings: LauncherSettings,
    private val mainFrame: MainFrame
) {
    private val log: Logger = LoggerFactory.getLogger(MainFrameController::class.java)

    private val gson = Gson()
    private lateinit var clientInfo: ClientInfo

    fun init() {
        MoveToTheBottom.install(mainFrame.logArea)
        mainFrame.isVisible = true
        loadClientInfo()
        updateProfile()
        registerAllEvents()

        addLog("Developer: GoldenMine (https://github.com/GoldenMine0502)")
        addLog("Special Thanks To: RanolP (https://github.com/RanolP)")
    }

    private fun loadClientInfo() {
        val file = File(launcherSettings.launcherDirectories.launcherDirectory, "client.json")
        val readerClientJson =
            BufferedReader(FileReader(file))
        val type = object : TypeToken<ClientInfo?>() {}.type
        clientInfo = gson.fromJson(readerClientJson, type)
    }

    fun updateProfile() {
        val userName = launcherSettings.userAdministrator.users.users.firstOrNull()
        if(userName != null) {
            val microsoftUser = launcherSettings.userAdministrator.users.getUser(userName)
            mainFrame.profileInfo.text = "계정: ${microsoftUser.username}, ${microsoftUser.id}"
            addLog("updated profile: ${microsoftUser.username}, ${microsoftUser.id}")
        } else {
            mainFrame.profileInfo.text = "계정: "
            addLog("updated profile: null")
        }
    }

    private fun registerAllEvents() {
        mainFrame.loginMicrosoft.addActionListener { tryMicrosoftLogin() }
//        mainFrame.microsoftPassword.addActionListener { tryLogin() }

        mainFrame.logoutMicrosoft.addActionListener { logout() }
    }

    fun addLog(text: String?) {
        mainFrame.logArea.append("$text\n")
    }

    fun logout() {
        launcherSettings.userAdministrator.users.users.clear()
        launcherSettings.userAdministrator.users.save()
        updateProfile()
    }

    fun loginRepeat(count: Int): MicrosoftUser {
        for(i in 1..5) {
            try {
                Thread.sleep(100L)
                return launcherSettings.userAdministrator.login()
            } catch(ex: Exception) {
                log.error(ex.message, ex)
            }
        }

        throw RuntimeException("microsoft login failed")
    }

    private fun tryMicrosoftLogin() {
        disableLoginButton()
        addLog("pressed microsoft login")
        Thread {
            try {
                val microsoftUser = loginRepeat(5)
                updateProfile()

                val minecraftAccount = MinecraftAccount(
                    microsoftUser.username,
                    microsoftUser.id.toString().replace("-", "").lowercase(),
                    microsoftUser.accessToken,
                    "msa"
                )

                val downloader = MinecraftDownloader(launcherSettings)
                val dataDownloader = MinecraftDataDownloader(launcherSettings)

                val builder = MinecraftCommandBuilder(launcherSettings, minecraftAccount)

                val launcher = MinecraftLauncher(
                    launcherSettings,
                    builder
                )

                addLog("checking or downloading minecraft assets...")
                downloader.download()
                dataDownloader.download()
//                addLog("copying libraries...")
//                launcher.preProcess()
                addLog("launching minecraft...")
                val code = launcher.launchMinecraft()
                addLog("process finished with exit code $code")
            } catch (ex: InterruptedException) {
                log.error(ex.message, ex)
                addLog(ex.message)
            } catch (ex: ExecutionException) {
                log.error(ex.message, ex)
                addLog(ex.message)
            } catch (ex: IOException) {
                log.error(ex.message, ex)
                addLog(ex.message)
            } finally {
                enableLoginButton()
            }
        }.start()
    }

    fun disableLoginButton() {
        mainFrame.loginMicrosoft.isEnabled = false
        mainFrame.loginGuest.isEnabled = false
    }

    fun enableLoginButton() {
        mainFrame.loginMicrosoft.isEnabled = true
        mainFrame.loginGuest.isEnabled = true
    }
}