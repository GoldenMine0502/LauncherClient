package kr.goldenmine.inuminecraftlauncher.launcher

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftPackage
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersion
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.ArgumentsDeserializer
import kr.goldenmine.inuminecraftlauncher.util.runProcessAndWait
import kr.goldenmine.inuminecraftlauncher.util.*
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MinecraftLauncher(
    private val launcherSettings: LauncherSettings,
    private val minecraftCommandBuilder: MinecraftCommandBuilder
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftDownloader::class.java)

    fun preProcess() {
        val gson = GsonBuilder().registerTypeAdapter(Arguments::class.java, ArgumentsDeserializer()).create()

        val minecraftVersionFile =
            File(
                launcherSettings.launcherDirectories.librariesDirectory,
                "versions/${launcherSettings.instanceSettings.minecraftVersion}.json"
            )
        val minecraftVersion = gson.fromJson(minecraftVersionFile.readText(), MinecraftVersion::class.java)

        val availableVersions = minecraftVersion.libraries.filter { library ->
            if (library.rules != null) {
                library.rules.count {
                    val isActionAllow = it.action == "allow"
                    val osAlternative = if (OS_NAME == "macos") "osx" else OS_NAME
                    val isOsSame = it.os == null || (it.os.name == OS_NAME || it.os.name == osAlternative)

                    !(isActionAllow xor isOsSame)
                } == library.rules.size
            } else true
        }.map {
            val splited = it.name.split(":")
            Pair(splited[1], splited[2])
        }
        log.info(availableVersions.toString())
        // copy natives files

//        deleteRecursive(launcherSettings.launcherDirectories.temporaryDirectory)
//        launcherSettings.launcherDirectories.temporaryDirectory.mkdirs()

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
                log.info(file.name)
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
                "${launcherSettings.instanceSettings.minecraftVersion}/natives/${it.name}"
            )
            dstFile.parentFile.mkdirs()
            if (!dstFile.exists()) dstFile.createNewFile()

            it.copyTo(dstFile, true)
        }

        // copy all files to virtual
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

        // forge file to libraries folder
        val forgeSrcFile =
            File(launcherSettings.launcherDirectories.forgeDirectory, launcherSettings.instanceSettings.forgeFileName)

        val forgeVersionName =
            "${launcherSettings.instanceSettings.minecraftVersion}-${launcherSettings.instanceSettings.forgeVersion}"
        val forgeDstFile = File(
            launcherSettings.launcherDirectories.librariesDirectory,
            "net/minecraftforge/forge/$forgeVersionName/forge-$forgeVersionName-client.jar"
        )

        val forgeUniversalSrcFile = listFilesRecursively(
            File(
                launcherSettings.launcherDirectories.forgeDirectory,
                "${launcherSettings.instanceSettings.forgeInstallerFileName.substringBeforeLast('.')}/maven"
            )
        ).first { it.name.contains("universal") }
        val forgeUniversalDstFile = File(
            launcherSettings.launcherDirectories.librariesDirectory,
            "net/minecraftforge/forge/$forgeVersionName/forge-$forgeVersionName-universal.jar"
        )

        forgeSrcFile.copyTo(forgeDstFile, true)
        forgeUniversalSrcFile.copyTo(forgeUniversalDstFile, true)
    }

    fun launchMinecraft(): Int {
//        val javaRepository = JavaRepository(launcherSettings.instanceSettings)
//        javaRepository.setPrimaryDefault()
        val javaRepository = launcherSettings.javaRepository

        log.info(javaRepository.primary?.path)

        val javaPath = javaRepository.primary?.path

//        val javaPath = if(launcherSettings.overrideJavaPath == null)
//            javaRepository.primary?.absolutePath
//        else
//            "${launcherSettings.overrideJavaPath}"

        if (javaPath != null) {
            val command = "$javaPath ${minecraftCommandBuilder.buildCommand()}"
            log.info(command)

            val commandNewLine = command.replace(" -", " \n-").replace(";", ";\n")
            log.info(commandNewLine)

            if (OperatingSystem.getOperatingSystem() == OperatingSystem.OSX)
                System.setProperty("jdk.lang.Process.launchMechanism", "FORK")

            val code = runProcessAndWait(command.split(" "))

            log.info("process finished with exit code $code")
            return code
        } else {
            return -1
        }
    }
//    private val File.absolutePathAndApplyQuotes: String
//        get() = "\"${this.absolutePath}\""
}