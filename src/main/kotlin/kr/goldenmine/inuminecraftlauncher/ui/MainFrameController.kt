package kr.goldenmine.inuminecraftlauncher.ui

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftCommandBuilder
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftDownloader
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftLauncher
import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import kr.goldenmine.inuminecraftlauncher.util.MoveToTheBottom
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
    }

    private fun loadClientInfo() {
        val readerClientJson =
            BufferedReader(InputStreamReader(Objects.requireNonNull(javaClass.classLoader.getResourceAsStream("client.json"))))
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

    private fun tryMicrosoftLogin() {
        addLog("pressed microsoft login")
        Thread {
            try {
                val microsoftUser = launcherSettings.userAdministrator.login()
                updateProfile()

                val minecraftAccount = MinecraftAccount(
                    microsoftUser.username,
                    microsoftUser.id.toString().replace("-", "").lowercase(),
                    microsoftUser.accessToken,
                    "msa"
                )

                val downloader = MinecraftDownloader(launcherSettings)

                val builder = MinecraftCommandBuilder(launcherSettings, minecraftAccount)

                val launcher = MinecraftLauncher(
                    launcherSettings,
                    builder
                )

                addLog("checking or downloading minecraft assets...")
                downloader.download()
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
            }
        }.start()
    }


}