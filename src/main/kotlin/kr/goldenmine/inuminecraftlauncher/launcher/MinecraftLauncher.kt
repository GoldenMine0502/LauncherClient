package kr.goldenmine.inuminecraftlauncher.launcher

import com.google.gson.GsonBuilder
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeInstall
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftPackage
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersion
import kr.goldenmine.inuminecraftlauncher.assets.forge.ArtifactAdditional
import kr.goldenmine.inuminecraftlauncher.assets.forge.Data
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

                    !(isActionAllow xor isOsSame)
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
                "${launcherSettings.instanceSettings.minecraftVersion}/natives/${it.name}"
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

    fun installForge() {
        val minecraftForgeInstallFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.forgeInstallerFileFolder}/install_profile.json"
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
                Pair(it.key, it.value.client)
            }
            .toMap()
            .toMutableMap()

        // MINECRAFT_JAR는 수동으로 추가
        val minecraftClientFile = File(
            launcherSettings.launcherDirectories.librariesDirectory,
            "versions/${launcherSettings.instanceSettings.minecraftVersion}.jar"
        )

        // TODO 왜 맵에 넣는건 안될까
        replacements["MINECRAFT_JAR"] = minecraftClientFile.absolutePath

        for ((key, value) in replacements) {
            log.info("$key: $value")
        }
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

            fun applyReplacements(key: String): String {
//                if(key == "{MINECRAFT_JAR}") return minecraftClientFile.absolutePath
                return replacements[key.substring(1, key.length - 1)] ?: key
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
                        "${launcherSettings.instanceSettings.forgeInstallerFileFolder}/data/client.lzma"
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

//        minecraftForgeInstall.processors.forEach { processor ->
//            val builder = CommandConcatenator(replacements, mapOf())
//
//            fun getAbsolutePath(route: String): String {
//                return File(
//                    launcherSettings.launcherDirectories.librariesDirectory,
//                    convertMavenStringToRoute(route)
//                ).absolutePath
//            }
//
//            // java file
//            builder.appendString(javaPath)
//            builder.appendString("-jar")
//            builder.appendString(getAbsolutePath(processor.jar))
//
//            // classpath
//            val cp = processor.classpath.joinToString(OS_CLASSPATH_SEPARATOR) { getAbsolutePath(it) } //+
////                    OS_CLASSPATH_SEPARATOR +
////                    getAbsolutePath(processor.jar)
//            builder.appendString("-cp")
//            builder.appendString(cp)
////            builder.appendString(OS_CLASSPATH_SEPARATOR)
////            builder.appendString(getAbsolutePath(processor.jar))
////            processor.classpath.forEach {
////                builder.appendString(getAbsolutePath(it))
////            }
//
//            // args
//            processor.args.forEach {
//                builder.processLine(it)
//            }
//            builder.removeLastCharacterIfBlank()
//
//            log.info(builder.toString())
//            log.info("\n" + builder.toString().replace("jar:", "jar:\n").replace(" -", "\n -"))
//
//            runProcessAndWait(builder.toList(), "UTF-8")
//
//            // TODO check output hash
//            // ~~~
//        }
    }

//    fun convertMavenStringToRoute(maven: String): String {
//        val split = maven.split(":")
//
//        val builder = StringBuilder()
//
//        // .이 곧 폴더이므로
//        builder.append(split[0].replace(".", "/"))
//        builder.append("/")
//        builder.append(split[1])
//        builder.append("/")
//        builder.append(split[2])
//
//        // 파일 이름
//        builder.append("/${split[1]}-${split[2]}.jar")
//
//        return builder.toString()
//    }

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

        // run processors
        installForge()
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

            if (OperatingSystem.getOperatingSystem() == OperatingSystem.OSX)
                System.setProperty("jdk.lang.Process.launchMechanism", "FORK")

            val code = runProcessAndWait(command.split(" "))

            log.info("process finished with exit code $code")
            return code
        } else {
            return -1
        }
    }
}