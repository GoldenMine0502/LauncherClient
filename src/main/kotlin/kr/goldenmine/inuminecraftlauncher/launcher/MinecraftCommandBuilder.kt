package kr.goldenmine.inuminecraftlauncher.launcher

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeVersion
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersion
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.ArgumentsDeserializer
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Library
import kr.goldenmine.inuminecraftlauncher.launcher.models.MinecraftAccount
import kr.goldenmine.inuminecraftlauncher.util.*
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.regex.Pattern

class MinecraftCommandBuilder(
    private val launcherSettings: LauncherSettings,
    private val minecraftAccount: MinecraftAccount,
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftCommandBuilder::class.java)

    fun getReplacements(
        minecraftVersion: MinecraftVersion,
        minecraftForgeVersion: MinecraftForgeVersion?
    ): Map<String, String> {
        val replacements = HashMap<String, String>()
        replacements["\${auth_player_name}"] = minecraftAccount.userName
        replacements["\${version_name}"] = minecraftVersion.id
        replacements["\${game_directory}"] =
            File(
                launcherSettings.launcherDirectories.instancesDirectory,
                launcherSettings.instanceSettings.instanceName
            ).absolutePath
        replacements["\${assets_root}"] = launcherSettings.launcherDirectories.assetsDirectory.absolutePath
        replacements["\${assets_index_name}"] = minecraftVersion.assetIndex.id
        replacements["\${auth_uuid}"] = minecraftAccount.uuid.replace("-", "").lowercase()
        replacements["\${auth_access_token}"] = minecraftAccount.accessToken
        replacements["\${user_type}"] = minecraftAccount.userType
        replacements["\${version_type}"] = minecraftVersion.type
        replacements["\${resolution_width}"] = launcherSettings.width.toString()
        replacements["\${resolution_height}"] = launcherSettings.height.toString()
        replacements["\${natives_directory}"] =
            File(
                launcherSettings.launcherDirectories.instancesDirectory,
                "${launcherSettings.instanceSettings.instanceName}/natives"
            ).absolutePath
        replacements["\${log_configuration}"] =
            File(
                launcherSettings.launcherDirectories.assetsDirectory,
                "objects/${
                    minecraftVersion.logging.client.file.sha1.substring(
                        0,
                        2
                    )
                }/${minecraftVersion.logging.client.file.id}"
            ).absolutePath
        replacements["\${launcher_name}"] = "INUMinecraftLauncher"
        replacements["\${launcher_version}"] = "1.0"
        replacements["\${classpath}"] = getClasspath(minecraftVersion, minecraftForgeVersion)

        // check all files exist
        for (pair in replacements) {
            if (pair.key == "\${classpath}") {
                pair.value.split(Pattern.compile("[;:]")).filter {
                    !File(it).exists()
                }.forEach { log.warn("not exist or not file route: $it") }
            } else {
                if (!File(pair.value).exists()) {
                    log.warn("not exist: ${pair.key} ${pair.value}")
                }
            }
        }

        return replacements
    }

    fun getFeatures(): Map<String, Boolean> {
        val features = HashMap<String, Boolean>()
        features["is_demo_user"] = false
        features["has_custom_resolution"] = launcherSettings.width != null && launcherSettings.height != null

        return features
    }

    fun buildCommand(): String {
        val gson = GsonBuilder().registerTypeAdapter(Arguments::class.java, ArgumentsDeserializer()).create()

        val minecraftVersionFile =
            File(
                launcherSettings.launcherDirectories.librariesDirectory,
                "versions/${launcherSettings.instanceSettings.minecraftVersion}.json"
            )
        val minecraftVersion = gson.fromJson(minecraftVersionFile.readText(), MinecraftVersion::class.java)

//        val minecraftForgeVersionFile = Paths.get("${launcherSettings.launcherDirectories.forgeDirectory.absolutePath}/${launcherSettings.instanceSettings.forgeInstallerFileName.substringBeforeLast('.')}/version.json", "UTF-8").toFile()
//        val minecraftForgeVersionFile = File(
//            launcherSettings.launcherDirectories.forgeDirectory,
//            "${launcherSettings.instanceSettings.forgeInstallerFileName.substringBeforeLast('.')}/version.json"
//        )

        launcherSettings.launcherDirectories.forgeDirectory.listFiles()?.forEach {
            log.info(it.absolutePath)
        }
//        val minecraftForgeVersionFile = launcherSettings.launcherDirectories.forgeDirectory.listFiles()!!.filter {
//            it.isDirectory &&
//                    it.name.contains(launcherSettings.instanceSettings.minecraftVersion) &&
//                    it.name.contains(launcherSettings.instanceSettings.forgeVersion)
//        }.first()

        val minecraftForgeVersionFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.forgeInstallerFileName.substringBeforeLast('.')}/version.json"
        )
        val minecraftForgeVersion =
            gson.fromJson(minecraftForgeVersionFile.readText(), MinecraftForgeVersion::class.java)

        // inulauncher/forge/forge-1.16.5-36.2.24-installer/version.json
        // \x66\x6F\x72\x67\x65\x2D\x31\x2E\x31\x36\x2E\x35\x2D\x33\x36\x2E\x32\x2E\x32\x34\x2D\x69\x6E\x73\x74\x61\x6C\x6C\x65\x72
        // inulauncher/forge/forge-1.16.5-36.2.34-installer/version.json
        // \x66\x6F\x72\x67\x65\x2D\x31\x2E\x31\x36\x2E\x35\x2D\x33\x36\x2E\x32\x2E\x33\x34\x2D\x69\x6E\x73\x74\x61\x6C\x6C\x65\x72

        val replacements = getReplacements(minecraftVersion, minecraftForgeVersion)

        val features = getFeatures()

        log.info("os: $OS_NAME")
        log.info("arch: $OS_ARCH")
        log.info("version: $OS_VERSION")

        val concatenator = CommandConcatenator(replacements, features)

        for (line in minecraftVersion.arguments.jvm) {
            concatenator.processLine(line)
        }

        for (line in minecraftForgeVersion.arguments.jvm) {
            concatenator.processLine(line)
        }

        val invalid = "-Dfml.ignoreInvalidMinecraftCertificates=true"
        concatenator.appendString(invalid)

        val invalid2 = "-Dfml.ignorePatchDiscrepancies=true"
        concatenator.appendString(invalid2)

//        val log4j = "-Dlog4j.configurationFile=\${log_configuration}"
//        appendString(log4j)

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.WINDOWS) {
            val heapDump = "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
            concatenator.appendString(heapDump)
        }

        val targetDirectory = "-Dminecraft.applet.TargetDirectory=\${game_directory}"
        concatenator.appendString(targetDirectory)

        if (launcherSettings.instanceSettings.mods.isEmpty()) {
            val mainClass = "-Xms256m net.minecraft.client.main.Main"
            concatenator.appendString(mainClass)
        } else {
            val mainClass = "-Xms1024m cpw.mods.modlauncher.Launcher"
            concatenator.appendString(mainClass)
        }

        for (line in minecraftVersion.arguments.game) {
            concatenator.processLine(line)
        }

        for (line in minecraftForgeVersion.arguments.game) {
            concatenator.processLine(line)
        }

        val serverIp = "--server ${launcherSettings.instanceSettings.ip}"
        val serverPort = "--port ${launcherSettings.instanceSettings.port}"

        concatenator.appendString(serverIp)
        concatenator.appendString(serverPort)

//        val clientId = "--clientId=\${clientId}"
//        appendString(clientId)
//
//        val xuid = "--xuid=\${auth_xuid}"
//        appendString(xuid)

        concatenator.removeLastCharacterIfBlank()

        return concatenator.toString()
    }

    fun getTotalLibraries(
        minecraftVersion: MinecraftVersion,
        minecraftForgeVersion: MinecraftForgeVersion?
    ): List<Library> {
        val latest = HashMap<String, String>()

        val totalLibrariesFirst =
            if (minecraftForgeVersion != null) minecraftVersion.libraries + minecraftForgeVersion.libraries else minecraftVersion.libraries

        totalLibrariesFirst.forEach { library ->
            val toAdd = (library.rules?.count {
                val isAllow = it.action == "allow"
                val osCorrect = it.os?.name == null || (it.os.name == OS_NAME || it.os.name == osNameAlternative)

                !(isAllow xor osCorrect)
            } ?: 0) == (library.rules?.size ?: 0) // 룰 조건 없으면 true

            if (toAdd && library.downloads.artifact.url.isNotEmpty()) {
                val split = library.name.split(":") // gradle like
                val key = "${split[0]}:${split[1]}"
                val previousValue = latest[key]
                val currentValue = split[2]

                if (previousValue == null) {
                    latest[key] = currentValue
                } else {
                    val previousVersion = previousValue.split(".").map { it.toInt() }
                    val currentVersion = currentValue.split(".").map { it.toInt() }

                    for (i in 0 until Integer.min(previousVersion.size, currentVersion.size)) {

                        if (currentVersion[i] > previousVersion[i]) {
                            latest[key] = currentValue // 업데이트
                            break
                        } else if (currentVersion[i] < previousVersion[i]) {
                            break // 현행 유지
                        }
                    }
                }
            }
        }

        return totalLibrariesFirst.filter {
            val split = it.name.split(":")
            val key = "${split[0]}:${split[1]}"
            val currentValue = split[2]
            val previousValue = latest[key]

            previousValue == currentValue
        }
    }

    fun getClasspath(minecraftVersion: MinecraftVersion, minecraftForgeVersion: MinecraftForgeVersion?): String {
        val builder = StringBuilder()

        val added = HashSet<String>()

        val totalLibraries = getTotalLibraries(minecraftVersion, minecraftForgeVersion)

        totalLibraries.forEach { library ->
            val toAdd = (library.rules?.count {
                val isAllow = it.action == "allow"
                val osCorrect = it.os?.name == null || (it.os.name == OS_NAME || it.os.name == osNameAlternative)

                !(isAllow xor osCorrect)
            } ?: 0) == (library.rules?.size ?: 0) // 룰 조건 없으면 true

            val isPreviousAdded = added.contains(library.name)

//            println("${library.name} ${library.rules != null} $toAdd")

            if (toAdd && !isPreviousAdded) {
                added.add(library.name)
                val path = File(
                    launcherSettings.launcherDirectories.librariesDirectory,
                    library.downloads.artifact.path
                ).absolutePath
                builder.append(path)
                builder.append(OS_CLASSPATH_SEPARATOR)
            }
        }

        val clientFile = File(
            launcherSettings.launcherDirectories.librariesDirectory,
            "versions/${minecraftVersion.id}.jar"
        ).absolutePath
        builder.append(clientFile)

        if (minecraftForgeVersion != null) {
            builder.append(OS_CLASSPATH_SEPARATOR)

            val forgeClientFile = File(
                launcherSettings.launcherDirectories.forgeDirectory,
                launcherSettings.instanceSettings.forgeFileName
            ).absolutePath
            builder.append(forgeClientFile)
        }

//        if(minecraftVersion.libraries.isNotEmpty()) {
//           builder.setLength(builder.length - 1)
//        }

        return builder.toString()
    }
}