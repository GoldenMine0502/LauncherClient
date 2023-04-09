package kr.goldenmine.inuminecraftlauncher.launcher

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeInstall
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftPackage
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersion
import kr.goldenmine.inuminecraftlauncher.assets.forge.ArtifactAdditional
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.ArgumentsDeserializer
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Artifact
import kr.goldenmine.inuminecraftlauncher.util.*
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarFile


class MinecraftLauncher(
    private val launcherSettings: LauncherSettings,
    private val minecraftCommandBuilder: MinecraftCommandBuilder
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftLauncher::class.java)

    private val gson = GsonBuilder().registerTypeAdapter(Arguments::class.java, ArgumentsDeserializer()).create()

    fun copyNativeFiles(minecraftVersion: MinecraftVersion) {
        val availableVersions = minecraftVersion.libraries.filter { library ->
            if (library.rules != null) {
                library.rules.count {
                    val isActionAllow = it.action == "allow"
                    val osAlternative = if (OS_NAME == "macos") "osx" else OS_NAME
                    val isOsSame = it.os == null || (it.os.name == OS_NAME || it.os.name == osAlternative)

                    !(isActionAllow xor isOsSame)// 둘다 true인경우
                } == library.rules.size
            } else true
        }.map {
            val splited = it.name.split(":")
            Pair(splited[1], splited[2])
        }
        log.info(availableVersions.toString())

        val natives = listFilesRecursively(launcherSettings.launcherDirectories.temporaryDirectory)
            .filter { it.name.contains(OS_NAME) }
//            .filter { }


        for (native in natives) {
            // 확장자 제거
            val dstFolderName = native.name.substring(0, native.name.lastIndexOf('.'))
            val dstFolder = File(launcherSettings.launcherDirectories.temporaryDirectory, dstFolderName)
            dstFolder.mkdirs()
            Compress().unZip(native.absolutePath, dstFolder.absolutePath)
        }

        val nativeFiles = launcherSettings.launcherDirectories.temporaryDirectory.listFiles()
            ?.asSequence()
            ?.filter { file ->
//                log.info(file.name)
                availableVersions.any { version ->
                    file.name.contains(version.first) && file.name.contains(version.second)
                }
            }
            ?.filter { it.name.contains(OS_NAME) }
            ?.flatMap { folder -> folder.listFiles()?.filter { file -> file.isFile } ?: listOf() }
            ?.toList()
            ?: emptyList()

        log.info("natives: " + nativeFiles.map { it.absolutePath + "\n" })

        nativeFiles.forEach {
            val dstFile = File(
                launcherSettings.launcherDirectories.instancesDirectory,
                "${launcherSettings.instanceSettings.instanceName}/natives/${it.name}"
            )
            dstFile.parentFile.mkdirs()
            if (!dstFile.exists()) dstFile.createNewFile()

            it.copyTo(dstFile, true)
        }
    }

    fun copyVirtual() {
        val minecraftPackageFile =
            File(
                launcherSettings.launcherDirectories.assetsDirectory,
                "indexes/${launcherSettings.instanceSettings.assetVersion}.json"
            )
        val minecraftPackage = gson.fromJson(minecraftPackageFile.readText(), MinecraftPackage::class.java)

        minecraftPackage.objects.forEach { (t, u) ->
            val sourceFile = File(
                launcherSettings.launcherDirectories.assetsDirectory,
                "objects/${u.hash.substring(0, 2)}/${u.hash}"
            )
            val dstFile = File(launcherSettings.launcherDirectories.assetsDirectory, "virtual/legacy/$t")
            dstFile.parentFile.mkdirs()
            dstFile.createNewFile()

            sourceFile.copyTo(dstFile, true)
        }
    }

    fun preProcess() {
        val minecraftVersionFile =
            File(
                launcherSettings.launcherDirectories.librariesDirectory,
                "versions/${launcherSettings.instanceSettings.minecraftVersion}.json"
            )
        val minecraftVersion = gson.fromJson(minecraftVersionFile.readText(), MinecraftVersion::class.java)

        // copy natives files
        copyNativeFiles(minecraftVersion)
//        launcherSettings.launcherDirectories.temporaryDirectory.mkdirs()

        // copy all files to virtual
        copyVirtual()

        // install forge
        MinecraftForgeInstaller(launcherSettings).install()
    }

    fun launchMinecraft(): Int {
        preProcess()

        val javaRepository = launcherSettings.javaRepository

        log.info(javaRepository.primary?.path)

        val javaPath = javaRepository.primary?.path

        if (javaPath != null) {
            val command = "$javaPath ${minecraftCommandBuilder.buildCommand()}"
            log.info(command)

            val commandNewLine = command.replace(" -", " \n-").replace(";", ";\n").replace(":", ":\n")
            log.info(commandNewLine)

            val code = runProcessAndWait(command.split(" "))

            log.info("process finished with exit code $code")
            return code
        } else {
            return -1
        }
    }
}