package kr.goldenmine.inuminecraftlauncher.ui

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kr.goldenmine.inuminecraftlauncher.InstanceSettings
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.download.ServerRequest
import kr.goldenmine.inuminecraftlauncher.instances.getInstanceName
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftCommandBuilder
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftDataDownloader
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftDownloader
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftLauncher
import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import kr.goldenmine.inuminecraftlauncher.server.LauncherServerService
import kr.goldenmine.inuminecraftlauncher.server.models.ServerStatusResponse
import kr.goldenmine.inuminecraftlauncher.util.MoveToTheBottom
import net.technicpack.minecraftcore.microsoft.auth.MicrosoftUser
import net.technicpack.utilslib.DesktopUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
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

//        addLog("디버그: ${DevelopmentConfiguration.IS_DEVELOPMENT}" )
        printGuestStatus()
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
            addLog("프로필: ${microsoftUser.username}, ${microsoftUser.id}")
        } else {
            mainFrame.profileInfo.text = "계정: "
            addLog("프로필: null")
        }
    }

    private fun registerAllEvents() {
        mainFrame.loginMicrosoft.addActionListener { tryMicrosoftLogin() }
        mainFrame.loginGuest.addActionListener { tryGuestLogin() }
        mainFrame.logoutMicrosoft.addActionListener { logout() }
        mainFrame.instanceSelection.addActionListener {
            val selectedVersion = mainFrame.instanceSelection.selectedItem as String
            val instanceName = getInstanceName(selectedVersion)
            if(instanceName != null) {
                changeVersion(instanceName)
            } else {
                addLog("none is selected.")
            }
        }
    }

    fun addLog(text: String?) {
        launcherSettings.logToGUI(text)
    }

    fun changeVersion(version: String) {
        disableLoginButton()

        ServerRequest.SERVICE.getInstanceSetting(version).enqueue(object : Callback<InstanceSettings> {
            override fun onResponse(call: Call<InstanceSettings>, response: Response<InstanceSettings>) {
                if(response.isSuccessful) {
                    val instanceSettings = response.body()
                    if(instanceSettings != null) {
                        launcherSettings.instanceSettings = instanceSettings
                        addLog("set instance settings to: ${launcherSettings.instanceSettings.instanceName}")
                        enableLoginButton()
                    } else {
                        addLog("none is received.")
                    }
                } else {
                    addLog("failed to get data.")
                }
            }

            override fun onFailure(call: Call<InstanceSettings>, t: Throwable) {
                addLog("failed to connect to server: $version")
            }

        })
    }

    fun logout() {
        launcherSettings.userAdministrator.users.users.clear()
        launcherSettings.userAdministrator.users.save()
        updateProfile()
    }

    fun loginRepeat(username: String?, count: Int): MicrosoftUser {
        for(i in 1..count) {
            try {
                Thread.sleep(100L)
                return launcherSettings.userAdministrator.login(username) { DesktopUtils.browseUrl(it) }
            } catch(ex: Exception) {
                log.error(ex.message, ex)
            }
        }

        throw RuntimeException("microsoft login failed")
    }

    fun printGuestStatus() {
//        val status = LauncherServerService.LAUNCHER_SERVER.requestStatus().execute().body()
//        log.info(status.toString())
//        if(status != null)
//            addLog("available guests: ${status.availableCounts} / ${status.totalCounts}")
        LauncherServerService.LAUNCHER_SERVER.requestStatus().enqueue(object : retrofit2.Callback<ServerStatusResponse> {
            override fun onResponse(call: Call<ServerStatusResponse>, response: Response<ServerStatusResponse>) {
                val status = response.body()
                log.info(status.toString())
                if(status != null) {
                    addLog("이용 가능한 게스트: ${status.availableCounts} / ${status.totalCounts}")
                }
            }

            override fun onFailure(call: Call<ServerStatusResponse>, t: Throwable) {
                addLog("failed to connect to server.")
                addLog("서버에 연결할 수 없습니다.")
                addLog("연결에 실패했습니다. 관리자에게 문의해주세요.")
            }
        })
    }

    private fun tryGuestLogin() {
        disableLoginButton()
//        addLog("pressed guest login")
        printGuestStatus()
        LauncherServerService.LAUNCHER_SERVER.requestRandomAccount().enqueue(object : retrofit2.Callback<MinecraftAccount> {
            override fun onResponse(call: Call<MinecraftAccount>, response: Response<MinecraftAccount>) {
                val minecraftAccount = response.body()
                if(minecraftAccount != null) {
                    addLog("username: ${minecraftAccount.userName}")
                    launchMinecraft(minecraftAccount)
                } else {
                    addLog("failed to get token.")
                    addLog("이미 모든 게스트가 사용 중이거나, ")
                    addLog("최근에 게스트를 사용하였습니다.")
                    enableLoginButton()
                }
            }

            override fun onFailure(call: Call<MinecraftAccount>, t: Throwable) {
                addLog("failed to get token. failed to connect server")
                addLog("서버에 연결할 수 없습니다.")
                addLog("연결에 실패했습니다. 관리자에게 문의해주세요.")
                enableLoginButton()
            }
        })
    }

    private fun tryMicrosoftLogin() {
        disableLoginButton()
//        addLog("pressed microsoft login")
        Thread {
            try {
                val microsoftUser = loginRepeat(launcherSettings.userAdministrator.users.users.firstOrNull(), 5)
                updateProfile()

                val minecraftAccount = MinecraftAccount(
                    microsoftUser.username,
                    microsoftUser.id.toString().replace("-", "").lowercase(),
                    microsoftUser.accessToken,
                    "msa"
                )
                launchMinecraft(minecraftAccount)
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

    fun launchMinecraft(minecraftAccount: MinecraftAccount) {
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
        enableLoginButton()
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