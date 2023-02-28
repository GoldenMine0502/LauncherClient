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
import kr.goldenmine.inuminecraftlauncher.launcher.impl.MinecraftException
import kr.goldenmine.inuminecraftlauncher.util.*
import net.minecraftforge.installer.SimpleInstaller
import net.minecraftforge.installer.json.Util
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.stream.Collectors


class MinecraftLauncher(
    private val launcherSettings: LauncherSettings,
    private val minecraftCommandBuilder: MinecraftCommandBuilder
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftDownloader::class.java)

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
        val forgePath = File(launcherSettings.launcherDirectories.forgeDirectory, launcherSettings.instanceSettings.forgeInstallerFileName).absolutePath
        val classLoader = URLClassLoader(arrayOf(URL("file://$forgePath")))

        val className = "net.minecraftforge.installer.actions.ClientInstall"
        val loadedClass = classLoader.loadClass(className)

//        val clientInstall = loadedClass.newInstance() as ClientInstall
//        val clientInstall = ClientInstall()

//        val launcherProfile: Path = WrapperUtil.ensureLauncherProfile(mainDir)

//        val profile = Util.loadInstallProfile()
//
//        val versionField = Install::class.java.getDeclaredField("version")
//        versionField.isAccessible = true
//        val oldVersionId = versionField[profile] as String
//        versionField[profile] = launcherSettings.instanceSettings.forgeVersion
//
//        val monitor = ProgressCallback.withOutputs(System.out)
//
//        SimpleInstaller.headless = true
//
//        val installer = File(SimpleInstaller::class.java.protectionDomain.codeSource.location.toURI())
//        val install = ClientInstall(profile, monitor)
//
//        val success = install.run(mainDir, { a: String? -> true }, installer)
//
//        // This file should exist after the installation.
//
//        // This file should exist after the installation.
//        WrapperUtil.fixVersionMetaId(mainDir, oldVersionId, versionId)

//        if (launcherProfile != null) {
//            Files.delete(launcherProfile)
//        }
//        myClass.doSomething()

        val javaPath = launcherSettings.javaRepository.primary?.absolutePath ?: throw MinecraftException("no java")

        val minecraftForgeInstallFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.forgeInstallerFileFolder}/install_profile.json"
        )
        val minecraftForgeInstall =
            gson.fromJson(minecraftForgeInstallFile.readText(), MinecraftForgeInstall::class.java)

        val replacements = minecraftForgeInstall.processors
            .asSequence()
            .flatMap { it.args }
            .filter { it.startsWith("{") && it.endsWith("}") }
            .map { it.substring(1, it.length - 1) } // 중괄호 제거
            .filter { minecraftForgeInstall.data.containsKey(it) }
            .map { Pair("{$it}", minecraftForgeInstall.data[it]!!.client) }
            .toMap()

        fun replaceTextInSequreBracket(key: String): String {
            return ArtifactAdditional(key.substring(1, key.length - 1)).getLocalPath(launcherSettings.launcherDirectories.librariesDirectory).absolutePath
        }

        fun replaceTextInBrace(key: String): String {
            return replacements[key] ?: key // 있으면 교체, 없으면 그대로
        }

        minecraftForgeInstall.processors.forEach { proc ->
            // processors.forEach랑 똑같음
            log.info("===============================================================================")

            val outputs: MutableMap<String, String> = HashMap()
            if (proc.outputs.isNotEmpty()) {
                var miss = false
                log.info("  Cache: ")
                for (e in proc.outputs.entries) {
                    // 키 처리
                    val key = if (e.key[0] == '[' && e.key[e.key.length - 1] == ']') {
                        replaceTextInSequreBracket(e.key)
                    } else { // 중괄호일때 처리인듯
                        replaceTextInBrace(e.key)
                    }

                    // 값 처리
                    val value = replaceTextInBrace(e.value)

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
                            artifact.delete()
                        }
                    }
                }

                // 와 해시 맞았다 ㅋㅋㅋ
                if (!miss) {
                    log.info("  Cache Hit!")
                }
            }

            val jar: File = proc.jar.getLocalPath(librariesDir)
            if (!jar.exists() || !jar.isFile) {
                log.error("  Missing Jar for processor: " + jar.absolutePath)
            }

            // Locate main class in jar file

            // Locate main class in jar file
            val jarFile = JarFile(jar)
            val mainClass = jarFile.manifest.mainAttributes.getValue(Attributes.Name.MAIN_CLASS)
            jarFile.close()

            if (mainClass == null || mainClass.isEmpty()) {
                error("  Jar does not have main class: " + jar.absolutePath)
                return
            }
            monitor.message("  MainClass: $mainClass", MessagePriority.LOW)

            val classpath: MutableList<URL> = ArrayList()
            val err = StringBuilder()
            monitor.message("  Classpath:", MessagePriority.LOW)
            monitor.message("    " + jar.absolutePath, MessagePriority.LOW)
            classpath.add(jar.toURI().toURL())
            for (dep in proc.getClasspath()) {
                val lib: File = dep.getLocalPath(librariesDir)
                if (!lib.exists() || !lib.isFile) err.append("\n  ").append(dep.getDescriptor())
                classpath.add(lib.toURI().toURL())
                monitor.message("    " + lib.absolutePath, MessagePriority.LOW)
            }
            if (err.length > 0) {
                error("  Missing Processor Dependencies: $err")
                return false
            }

            val args: MutableList<String> = ArrayList()
            for (arg in proc.getArgs()) {
                val start = arg[0]
                val end = arg[arg.length - 1]
                if (start == '[' && end == ']') //Library
                    args.add(
                        Artifact.from(arg.substring(1, arg.length - 1)).getLocalPath(librariesDir).getAbsolutePath()
                    ) else args.add(
                    Util.replaceTokens(data, arg)
                )
            }
            if (err.length > 0) {
                error("  Missing Processor data values: $err")
                return false
            }
            monitor.message("  Args: " + args.stream().map<String> { a: String ->
                if (a.indexOf(
                        ' '
                    ) != -1 || a.indexOf(',') != -1
                ) '"'.toString() + a + '"' else a
            }.collect(Collectors.joining(", ")), MessagePriority.LOW)

            val cl: ClassLoader = URLClassLoader(classpath.toTypedArray<URL>(), getParentClassloader())
            // Set the thread context classloader to be our newly constructed one so that service loaders work
            // Set the thread context classloader to be our newly constructed one so that service loaders work
            val currentThread = Thread.currentThread()
            val threadClassloader = currentThread.contextClassLoader
            currentThread.contextClassLoader = cl
            try {
                val cls = Class.forName(mainClass, true, cl)
                val main = cls.getDeclaredMethod("main", Array<String>::class.java)
                main.invoke(null, args.toTypedArray() as Any)
            } catch (ite: InvocationTargetException) {
                val e = ite.cause
                e!!.printStackTrace()
                if (e!!.message == null) error(
                    """
            Failed to run processor: ${e!!.javaClass.name}
            See log for more details.
            """.trimIndent()
                ) else error(
                    """
            Failed to run processor: ${e!!.javaClass.name}:${e!!.message}
            See log for more details.
            """.trimIndent()
                )
                return false
            } catch (e: Throwable) {
                e.printStackTrace()
                if (e.message == null) error(
                    """
            Failed to run processor: ${e.javaClass.name}
            See log for more details.
            """.trimIndent()
                ) else error(
                    """
            Failed to run processor: ${e.javaClass.name}:${e.message}
            See log for more details.
            """.trimIndent()
                )
                return false
            } finally {
                // Set back to the previous classloader
                currentThread.contextClassLoader = threadClassloader
            }

            if (!outputs.isEmpty()) {
                for ((key, value): Map.Entry<String, String> in outputs) {
                    val artifact = File(key)
                    if (!artifact.exists()) {
                        err.append("\n    ").append(key).append(" missing")
                    } else {
                        val sha: String = DownloadUtils.getSha1(artifact)
                        if (sha == value) {
                            log("  Output: $key Checksum Validated: $sha")
                        } else {
                            err.append("\n    ").append(key)
                                .append("\n      Expected: ").append(value)
                                .append("\n      Actual:   ").append(sha)
                            if (!SimpleInstaller.debug && !artifact.delete()) err.append("\n      Could not delete file")
                        }
                    }
                }
                if (err.length > 0) {
                    error("  Processor failed, invalid outputs:$err")
                    return false
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

    fun convertMavenStringToRoute(maven: String): String {
        val split = maven.split(":")

        val builder = StringBuilder()

        // .이 곧 폴더이므로
        builder.append(split[0].replace(".", "/"))
        builder.append("/")
        builder.append(split[1])
        builder.append("/")
        builder.append(split[2])

        // 파일 이름
        builder.append("/${split[1]}-${split[2]}.jar")

        return builder.toString()
    }
//
//    // https://github.com/MinecraftForge/Installer/blob/2.0/src/main/java/net/minecraftforge/installer/actions/PostProcessors.java
//    fun process(librariesDir: File, minecraft: File, root: File, installer: File): Boolean {
//        val isClient = true
//        val data = HashMap<String, String>()
//
//        return try {
//            if (data.isNotEmpty()) {
//                val err = StringBuilder()
//                val temp = Files.createTempDirectory("forge_installer")
//                log.info("Created Temporary Directory: $temp")
//                val steps = data.size
//                var progress = 1
//                for (key in data.keys) {
//                    log.info("percent: ${progress++ / steps}")
//                    val value: String = data[key]!!
//                    if (value[0] == '[' && value[value.length - 1] == ']') { //Artifact
//                        data[key] = Artifact.from(value.substring(1, value.length - 1)).getLocalPath(librariesDir).getAbsolutePath()
//                    } else if (value[0] == '\'' && value[value.length - 1] == '\'') { //Literal
//                        data.put(key, value.substring(1, value.length - 1))
//                    } else {
//                        val target: File = Paths.get(temp.toString(), value).toFile()
//                        log.info("  Extracting: $value")
//                        Compress.unZip(value, target)
////                        if (!(value, target)) err.append("\n  ").append(value)
//                        data[key] = target.absolutePath
//                    }
//                }
//                if (err.isNotEmpty()) {
//                    error("Failed to extract files from archive: $err")
//                    return false
//                }
//            }
//            data.put("SIDE", if (isClient) "client" else "server")
//            data.put("MINECRAFT_JAR", minecraft.absolutePath)
//            data.put("MINECRAFT_VERSION", profile.getMinecraft())
//            data.put("ROOT", root.absolutePath)
//            data.put("INSTALLER", installer.absolutePath)
//            data.put("LIBRARY_DIR", librariesDir.absolutePath)
//            var progress = 1
//            if (processors.size() === 1) {
//                log.info("Building Processor")
//            } else {
//                log.info("Building Processors")
//            }
//            for (proc in processors) {
//                log.info("${progress++.toDouble() / processors.size()}")
//                log.info("===============================================================================")
//                val outputs = HashMap<String, String>()
//                if (!proc.getOutputs().isEmpty()) {
//                    var miss = false
//                    log.info("  Cache: ")
//                    for (e in proc.getOutputs().entrySet()) {
//                        var key: String? = e.getKey()
//                        if (key!![0] == '[' && key[key.length - 1] == ']') key =
//                            Artifact.from(key.substring(1, key.length - 1)).getLocalPath(librariesDir)
//                                .getAbsolutePath() else key = Util.replaceTokens(data, key)
//                        var value: String? = e.getValue()
//                        if (value != null) value = Util.replaceTokens(data, value)
//                        if (key == null || value == null) {
//                            error("  Invalid configuration, bad output config: [" + e.getKey() + ": " + e.getValue() + "]")
//                            return false
//                        }
//                        outputs.put(key, value)
//                        val artifact = File(key)
//                        if (!artifact.exists()) {
//                            log.info("    $key Missing")
//                            miss = true
//                        } else {
//                            val sha: String = DownloadUtils.getSha1(artifact)
//                            if (sha == value) {
//                                log.info("    $key Validated: $value")
//                            } else {
//                                log.info("    $key")
//                                log.info("      Expected: $value")
//                                log.info("      Actual:   $sha")
//                                miss = true
//                                artifact.delete()
//                            }
//                        }
//                    }
//                    if (!miss) {
//                        log.info("  Cache Hit!")
//                        continue
//                    }
//                }
//                val jar: File = proc.getJar().getLocalPath(librariesDir)
//                if (!jar.exists() || !jar.isFile) {
//                    error("  Missing Jar for processor: " + jar.absolutePath)
//                    return false
//                }
//
//                // Locate main class in jar file
//                val jarFile = JarFile(jar)
//                val mainClass: String = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS)
//                jarFile.close()
//                if (mainClass == null || mainClass.isEmpty()) {
//                    error("  Jar does not have main class: " + jar.absolutePath)
//                    return false
//                }
//                log.info("  MainClass: $mainClass")
//                val classpath: List<URL> = ArrayList()
//                val err = StringBuilder()
//                log.info("  Classpath:")
//                log.info("    " + jar.absolutePath)
//                classpath.add(jar.toURI().toURL())
//                for (dep in proc.getClasspath()) {
//                    val lib: File = dep.getLocalPath(librariesDir)
//                    if (!lib.exists() || !lib.isFile) err.append("\n  ").append(dep.getDescriptor())
//                    classpath.add(lib.toURI().toURL())
//                    log.info("    " + lib.absolutePath)
//                }
//                if (err.isNotEmpty()) {
//                    error("  Missing Processor Dependencies: $err")
//                    return false
//                }
//                val args: List<String> = ArrayList()
//                for (arg in proc.getArgs()) {
//                    val start = arg[0]
//                    val end = arg[arg.length - 1]
//                    if (start == '[' && end == ']') //Library
//                        args.add(
//                            Artifact.from(arg.substring(1, arg.length - 1)).getLocalPath(librariesDir).getAbsolutePath()
//                        ) else args.add(
//                        Util.replaceTokens(data, arg)
//                    )
//                }
//                if (err.length > 0) {
//                    error("  Missing Processor data values: $err")
//                    return false
//                }
//                monitor.message(
//                    "  Args: " + args.stream()
//                        .map { a -> if (a.indexOf(' ') !== -1 || a.indexOf(',') !== -1) '"' + a + '"'.code else a }
//                        .collect(Collectors.joining(", ")), MessagePriority.LOW)
//                val cl: ClassLoader = URLClassLoader(classpath.toArray(arrayOfNulls<URL>(classpath.size())), getParentClassloader())
//                // Set the thread context classloader to be our newly constructed one so that service loaders work
//                val currentThread = Thread.currentThread()
//                val threadClassloader = currentThread.contextClassLoader
//                currentThread.contextClassLoader = cl
//                try {
//                    val cls = Class.forName(mainClass, true, cl)
//                    val main: Method = cls.getDeclaredMethod("main", Array<String>::class.java)
//                    main.invoke(null, args.toArray(arrayOfNulls<String>(args.size())) as Any?)
//                } catch (ite: InvocationTargetException) {
//                    val e: Throwable = ite.getCause()
//                    e.printStackTrace()
//                    if (e.message == null) error(
//                        """
//                                 Failed to run processor: ${e.javaClass.name}
//                                 See log for more details.
//                                 """.trimIndent()
//                    ) else error(
//                        """
//                                 Failed to run processor: ${e.javaClass.name}:${e.message}
//                                 See log for more details.
//                                 """.trimIndent()
//                    )
//                    return false
//                } catch (e: Throwable) {
//                    e.printStackTrace()
//                    if (e.message == null) error(
//                        """
//                                 Failed to run processor: ${e.javaClass.name}
//                                 See log for more details.
//                                 """.trimIndent()
//                    ) else error(
//                        """
//                                 Failed to run processor: ${e.javaClass.name}:${e.message}
//                                 See log for more details.
//                                 """.trimIndent()
//                    )
//                    return false
//                } finally {
//                    // Set back to the previous classloader
//                    currentThread.contextClassLoader = threadClassloader
//                }
//                if (!outputs.isEmpty()) {
//                    for (e in outputs) {
//                        val artifact = File(e.getKey())
//                        if (!artifact.exists()) {
//                            err.append("\n    ").append(e.getKey()).append(" missing")
//                        } else {
//                            val sha = getFileSHA1(artifact)
//                            if (sha == e.getValue()) {
//                                log.info("  Output: " + e.getKey() + " Checksum Validated: " + sha)
//                            } else {
//                                err.append("\n    ").append(e.getKey())
//                                    .append("\n      Expected: ").append(e.getValue())
//                                    .append("\n      Actual:   ").append(sha)
////                                if (!SimpleInstaller.debug && !artifact.delete()) err.append("\n      Could not delete file")
//                            }
//                        }
//                    }
//                    if (err.isNotEmpty()) {
//                        log.error("  Processor failed, invalid outputs:$err")
//                        return false
//                    }
//                }
//            }
//            true
//        } catch (e: IOException) {
//            e.printStackTrace()
//            false
//        }
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
//        deleteRecursive(launcherSettings.launcherDirectories.temporaryDirectory)
//        launcherSettings.launcherDirectories.temporaryDirectory.mkdirs()

        // copy all files to virtual
        copyVirtual()

        // copy forge file to libraries folder
//        val forgeSrcFile =
//            File(launcherSettings.launcherDirectories.forgeDirectory, launcherSettings.instanceSettings.forgeFileName)
//
//        val forgeVersionName =
//            "${launcherSettings.instanceSettings.minecraftVersion}-${launcherSettings.instanceSettings.forgeVersion}"
//        val forgeDstFile = File(
//            launcherSettings.launcherDirectories.librariesDirectory,
//            "net/minecraftforge/forge/$forgeVersionName/forge-$forgeVersionName-client.jar"
//        )
//
//        val forgeUniversalSrcFile = listFilesRecursively(
//            File(
//                launcherSettings.launcherDirectories.forgeDirectory,
//                "${launcherSettings.instanceSettings.forgeInstallerFileName.substringBeforeLast('.')}/maven"
//            )
//        ).first { it.name.contains("universal") }
//        val forgeUniversalDstFile = File(
//            launcherSettings.launcherDirectories.librariesDirectory,
//            "net/minecraftforge/forge/$forgeVersionName/forge-$forgeVersionName-universal.jar"
//        )
//
//        forgeSrcFile.copyTo(forgeDstFile, true)
//        forgeUniversalSrcFile.copyTo(forgeUniversalDstFile, true)
    }

    fun launchMinecraft(): Int {
        preProcess()
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