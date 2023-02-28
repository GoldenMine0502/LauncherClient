package kr.goldenmine.inuminecraftlauncher

import com.google.gson.Gson
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeVersion
import kr.goldenmine.inuminecraftlauncher.download.*
import kr.goldenmine.inuminecraftlauncher.launcher.DefaultLauncherDirectories
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftCommandBuilder
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftDownloader
import kr.goldenmine.inuminecraftlauncher.launcher.MinecraftLauncher
import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import kr.goldenmine.inuminecraftlauncher.util.unzipJar
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MinecraftAssetDownloadTest {

    private val temporaryDirectory = DefaultLauncherDirectories(File("inulauncher"))
    private val instanceSettings = InstanceSettings("1.16.5", "1.16", "36.2.34", 8, true)
    private val minecraftAccount = MinecraftAccount("test", "test", "test", "test")
    private val launcherSettings = LauncherSettings(temporaryDirectory,
        instanceSettings,
        width = 854,
        height = 480,
//        overrideJavaPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/bin/java",
    )

    @Test
    fun testAll() {
        val downloader = MinecraftDownloader(launcherSettings)
        downloader.download()

        val builder = MinecraftCommandBuilder(launcherSettings, minecraftAccount)

        val launcher = MinecraftLauncher(launcherSettings, builder)
//        launcher.download()
//        launcher.preProcess()
        launcher.launchMinecraft()
    }

    @Test
    fun launchMinecraft() {
        val builder = MinecraftCommandBuilder(launcherSettings, minecraftAccount)

        val launcher = MinecraftLauncher(launcherSettings, builder)
        launcher.launchMinecraft()
    }

    @Test
    fun printTest() {
        println(instanceSettings.forgeInstallerFileName.substringBeforeLast('.'))
    }

    @Test
    fun forgeDownloadTest() {
        val forgeDownloadTask = MinecraftForgeDownloadTask(temporaryDirectory, instanceSettings.minecraftVersion, instanceSettings.forgeVersion)
        val result = forgeDownloadTask.download()

        println("downloaded: $result")
    }

    @Test
    fun forgeLoadTest() {
        val gson = Gson()

        val forgeInstallerFile = File(temporaryDirectory.forgeDirectory, instanceSettings.forgeInstallerFileName)
        if(!forgeInstallerFile.exists()) println("no forge file")

        val fileNameNoExtension = instanceSettings.forgeInstallerFileName.substringBeforeLast('.')

        val dstFolder = File(temporaryDirectory.forgeDirectory, fileNameNoExtension)

        unzipJar(forgeInstallerFile, dstFolder)
        println("unzipped")

        val versionFile = File(dstFolder, "version.json")
        val minecraftForgeVersion = gson.fromJson(versionFile.readText(), MinecraftForgeVersion::class.java)

//        println(minecraftForgeVersion)

        println(minecraftForgeVersion)

        println("=== game arguments ===")
        minecraftForgeVersion.arguments.game.forEach { println(it.toString()) }

        println("=== jvm arguments ===")
        minecraftForgeVersion.arguments.jvm.forEach { println(it.toString()) }

        println("=== libraries ===")
        minecraftForgeVersion.libraries.forEach { println("${it.downloads.artifact.path}, ${it.downloads.artifact.url}") }
    }

    @Test
    fun forgeExtractTest() {
        val forgeInstallerFile = File(temporaryDirectory.forgeDirectory, instanceSettings.forgeInstallerFileName)
        val javaPath = launcherSettings.javaRepository.primary?.absolutePath

        if (forgeInstallerFile.exists() && javaPath != null) {
            val processBuilder = ProcessBuilder(
                javaPath,
                "-jar",
                forgeInstallerFile.absolutePath,
                "--extract",
                forgeInstallerFile.parentFile.absolutePath
            ).redirectErrorStream(true)
            println(processBuilder.command().joinToString(" "))
            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream, "MS949"))

            Thread {
                var line: String?
                while (run {
                        line = reader.readLine()
                        line
                    } != null) {
                    println(line)
                }
            }.start()
            process.waitFor()

            val forgeFile = File(temporaryDirectory.forgeDirectory, instanceSettings.forgeFileName)
            println("forgeFile: ${forgeFile.absolutePath} ${forgeFile.exists()}")
        } else {
            println("exists ${forgeInstallerFile.exists()}")
            println("javaPath $javaPath")
        }
    }

    @Test
    fun forgeProfileTest() {
        val downloader = MinecraftDownloader(launcherSettings)
        downloader.downloadForge()
    }
}