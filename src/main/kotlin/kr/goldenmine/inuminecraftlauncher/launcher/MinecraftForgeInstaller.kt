package kr.goldenmine.inuminecraftlauncher.launcher

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeInstall
import kr.goldenmine.inuminecraftlauncher.assets.forge.ArtifactAdditional
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Arguments
import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.ArgumentsDeserializer
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Artifact
import kr.goldenmine.inuminecraftlauncher.util.getFileSHA1
import kr.goldenmine.inuminecraftlauncher.util.listFilesRecursively
import kr.goldenmine.inuminecraftlauncher.util.runProcessAndWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarFile

class MinecraftForgeInstaller(
    private val launcherSettings: LauncherSettings,
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftForgeInstaller::class.java)
    private val gson = GsonBuilder().registerTypeAdapter(Arguments::class.java, ArgumentsDeserializer()).create()

    fun copyUniversalFromExtractedJar() {
        val folder = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            launcherSettings.instanceSettings.getForgeInstallerFileFolder()
        )

        val mavenFolder = File(folder, "maven")

        val forgeFiles = listFilesRecursively(mavenFolder)
        forgeFiles.forEach {
            val path = it.absolutePath.replace(mavenFolder.absolutePath, "").substring(1)
            val dstFile = File(
                launcherSettings.launcherDirectories.librariesDirectory,
                path
            )

            it.copyTo(dstFile, true)
        }
    }

    fun executeServerAndCopyUniversal() {
        val fullVersion =
            "${launcherSettings.instanceSettings.minecraftVersion}-${launcherSettings.instanceSettings.forgeVersion}"
        val folder = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.getForgeInstallerFileFolder()}-server"
        )

        val universalFileName = "net/minecraftforge/forge/$fullVersion/forge-$fullVersion-universal.jar"
        val universalFile = File(folder, "libraries/$universalFileName")
        val dstFile = File(launcherSettings.launcherDirectories.librariesDirectory, universalFileName)

        if (!dstFile.exists()) {
            val forgeInstallerFile = File(
                launcherSettings.launcherDirectories.forgeDirectory,
                launcherSettings.instanceSettings.getForgeInstallerFileName()
            )

            val javaPath = launcherSettings.javaRepository.primary ?: throw RuntimeException("no java")
            val command =
                "${javaPath.absolutePath} -jar ${forgeInstallerFile.absolutePath} --installServer ${folder.absolutePath}"
            log.info("executing $command")
            val code = runProcessAndWait(command.split(" "), "MS949")
            log.info("code $code")

            universalFile.copyTo(dstFile)
        }
    }

    fun installForge() {
        val minecraftForgeInstallFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.getForgeInstallerFileFolder()}/install_profile.json"
        )
        val minecraftForgeInstall =
            gson.fromJson(minecraftForgeInstallFile.readText(), MinecraftForgeInstall::class.java)

//        val replacements = minecraftForgeInstall.processors
//            .asSequence()
//            .flatMap { it.args }
//            .filter { it.startsWith("{") && it.endsWith("}") }
//            .map { it.substring(1, it.length - 1) } // 중괄호 제거
//            .filter { minecraftForgeInstall.data.containsKey(it) }
//            .map { Pair("{$it}", minecraftForgeInstall.data[it]!!.client) }
//            .toMap()
//            .toMutableMap()

        val replacements = minecraftForgeInstall.data
            .map {
                Pair("{${it.key}}", it.value.client)
            }
            .toMap()
            .toMutableMap()

        // MINECRAFT_JAR는 수동으로 추가
        val minecraftClientFile = File(
            launcherSettings.launcherDirectories.librariesDirectory,
            "versions/${launcherSettings.instanceSettings.minecraftVersion}.jar"
        )

        // TODO 왜 맵에 넣는건 안될까
        replacements["{MINECRAFT_JAR}"] = minecraftClientFile.absolutePath

//        log.info("{MINECRAFT_JAR}: ${replacements["{MINECRAFT_JAR}"]}")

        // run processors
        minecraftForgeInstall.processors.forEach { proc ->
            log.info("===============================================================================")

            val outputs = HashMap<String, String>()
            val caches = HashMap<String, ArtifactAdditional>() // key는 Artifact의 path로 설정

            fun getArtifactAdditional(key: String): String {
                val additional = ArtifactAdditional(key.substring(1, key.length - 1))
                caches[additional.path] = additional
                return additional.getLocalPath(launcherSettings.launcherDirectories.librariesDirectory).absolutePath
            }

            fun applyReplacements(text: String): String {
//                if(key == "{MINECRAFT_JAR}") return minecraftClientFile.absolutePath
                var text = text
                for ((key, value) in replacements) {
                    text = text.replace(key, value)
                }

                return text
            }

            fun replaceValue(value: String): String {
                return if (value.first() == '[' && value.last() == ']') {
                    getArtifactAdditional(value)
                } else { // 중괄호일때 처리
                    val replaced = applyReplacements(value) // 있으면 교체, 없으면 그대로

                    if (replaced.first() == '[' && replaced.last() == ']') {
                        getArtifactAdditional(replaced)
                    } else {
                        value
                    }
                }
            }

            if (!proc.outputs.isNullOrEmpty()) {
                var miss = false
                log.info("  Cache: ")
                for (e in proc.outputs.entries) {
                    // 키 처리
                    val key = replaceValue(e.key)

                    // 값 처리
                    val value = replaceValue(e.value)

                    // 출력 결과 체크 맵에 넣기
                    outputs[key] = value

                    // 키 체크
                    val artifact = File(key)
                    if (!artifact.exists()) {
                        log.info("    $key Missing")
                        miss = true
                    } else {
                        val sha = getFileSHA1(artifact)
                        if (sha == value) {
                            log.info("    $key Validated: $value")
                        } else {
                            log.info("    $key")
                            log.info("      Expected: $value")
                            log.info("      Actual:   $sha")
                            miss = true
//                            artifact.delete()
                        }
                    }
                }

                // 와 해시 맞았다 ㅋㅋㅋ
                if (!miss) {
                    log.info("  Cache Hit!")
                }
            }

            fun findArtifactFromName(name: String): Artifact {
                return minecraftForgeInstall.libraries.first { it.name == name }.downloads.artifact
            }

            val jar =
                findArtifactFromName(proc.jar).getLocalPath(launcherSettings.launcherDirectories.librariesDirectory)
            if (!jar.exists() || !jar.isFile) {
                log.error("  Missing Jar for processor: " + jar.absolutePath)
            }

            // Locate main class in jar file
            val jarFile = JarFile(jar)
            val mainClass = jarFile.manifest.mainAttributes.getValue(Attributes.Name.MAIN_CLASS)
            jarFile.close()

            if (mainClass == null || mainClass.isEmpty()) {
                log.error("  Jar does not have main class: " + jar.absolutePath)
                return
            }
            log.info("  MainClass: $mainClass")

            val classpath: MutableList<URL> = ArrayList()
            val err = StringBuilder()
            log.info("  Classpath:")
            log.info("    " + jar.absolutePath)
            classpath.add(jar.toURI().toURL())
            for (dep in proc.classpath) {
                val artifact = findArtifactFromName(dep)
                val lib = artifact.getLocalPath(launcherSettings.launcherDirectories.librariesDirectory)
                val additional = caches[artifact.path]

                if (!lib.exists() || !lib.isFile) err.append("\n  ").append(additional?.descriptor)
                classpath.add(lib.toURI().toURL())
                log.info("    " + lib.absolutePath)
//                log.info("    " + lib.toURI().toURL())
            }

            if (err.isNotEmpty()) {
                log.error("  Missing Processor Dependencies: $err")
                return@forEach
            }

            val args = ArrayList<String>()
            for (arg in proc.args) {
//                val start = arg[0]
//                val end = arg[arg.length - 1]

                // TODO Map 작동 안해서 하드코드
                val result = if (arg.contains("MINECRAFT_JAR"))
                    minecraftClientFile.absolutePath
                else if (arg.contains("BINPATCH"))
                    File(
                        launcherSettings.launcherDirectories.forgeDirectory,
                        "${launcherSettings.instanceSettings.getForgeInstallerFileFolder()}/data/client.lzma"
                    ).absolutePath
                else
                    replaceValue(arg)
                args.add(result)
                log.info("$arg $result")
//                if (start == '[' && end == ']') //Library
//                    args.add(getArtifactAdditional(arg))
//                else args.add(applyReplacements(arg))
            }

            val argsToStr = args.asSequence().map { a ->
                if (a.indexOf(' ') != -1 || a.indexOf(',') != -1) "\"$a\"" else a
            }.joinToString(" ")
            log.info("  Args: $argsToStr")

            @Synchronized
            fun getParentClassloader(): ClassLoader? { //Reflectively try and get the platform classloader, done this way to prevent hard dep on J9.
                //in 9+ the changed from 1.8 to just 9. So this essentially detects if we're <9
                if (!System.getProperty("java.version").startsWith("1.")) {
                    try {
                        val getPlatform = ClassLoader::class.java.getDeclaredMethod("getPlatformClassLoader")
                        return getPlatform.invoke(null) as ClassLoader
                    } catch (e: Exception) {
                        log.error("No platform classloader: " + System.getProperty("java.version"), e)
                    }
                }

                return null
            }

            val cl = URLClassLoader(classpath.toTypedArray(), getParentClassloader())

            // Set the thread context classloader to be our newly constructed one so that service loaders work
            val currentThread = Thread.currentThread()
            val threadClassloader = currentThread.contextClassLoader
            currentThread.contextClassLoader = cl
            try {
                val cls = Class.forName(mainClass, true, cl)
                val main = cls.getDeclaredMethod("main", Array<String>::class.java)
                main.invoke(null, args.toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
                log.error(
                    """
                        Failed to run processor: ${e.javaClass.name}:${e.message}
                        See log for more details.
                        """.trimIndent()
                )
                return@forEach
            } finally {
                // Set back to the previous classloader
                currentThread.contextClassLoader = threadClassloader
            }

            if (outputs.isNotEmpty()) {
                for ((key, value) in outputs) {
                    val artifact = File(key)
                    if (!artifact.exists()) {
                        err.append("\n    ").append(key).append(" missing")
                    } else {
                        val sha = getFileSHA1(artifact)
                        if (sha == value) {
                            log.info("  Output: $key Checksum Validated: $sha")
                        } else {
                            err.append("\n    ").append(key)
                                .append("\n      Expected: ").append(value)
                                .append("\n      Actual:   ").append(sha)
//                            if (!SimpleInstaller.debug && !artifact.delete()) err.append("\n      Could not delete file")
                        }
                    }
                }
                if (err.isNotEmpty()) {
                    log.error("  Processor failed, invalid outputs:$err")
//                    return
                }
            }

        }
    }

    fun install() {
//        launcherSettings.logToGUI("installing forge...")
        installForge()
        copyUniversalFromExtractedJar()
    }
}