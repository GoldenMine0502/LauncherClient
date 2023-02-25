package kr.goldenmine.inuminecraftlauncher.launcher

import com.google.gson.Gson
import kr.goldenmine.inuminecraftlauncher.LauncherSettings
import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeVersion
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftPackage
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersion
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftForgeInstall
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Library
import kr.goldenmine.inuminecraftlauncher.download.*
import kr.goldenmine.inuminecraftlauncher.download.java.writeResponseBodyToDisk
import kr.goldenmine.inuminecraftlauncher.launcher.impl.MinecraftException
import kr.goldenmine.inuminecraftlauncher.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MinecraftDownloader(
    private val launcherSettings: LauncherSettings
) {
    private val log: Logger = LoggerFactory.getLogger(MinecraftDownloader::class.java)

    private val gson = Gson()

    fun downloadVanillaXml(minecraftVersion: MinecraftVersion) {
        val xmlFileRoute = "objects/${
            minecraftVersion.logging.client.file.sha1.substring(
                0,
                2
            )
        }/${minecraftVersion.logging.client.file.id}"
        val xmlFile = File(launcherSettings.launcherDirectories.assetsDirectory, xmlFileRoute)
        if (!xmlFile.exists() || getFileSHA1(xmlFile) != minecraftVersion.logging.client.file.sha1) {
            println("downloading xml")
            val xmlBody =
                AssetService.MINECRAFT_API.downloadFromUrl(minecraftVersion.logging.client.file.url).execute()
                    .body()
            if (xmlBody != null) {
                xmlFile.parentFile.mkdirs()
                writeResponseBodyToDisk(xmlFile, xmlBody)
            }
        } else {
            println("xml exists")
        }
    }

    fun downloadVanillaMapping(minecraftVersion: MinecraftVersion) {
        // downloading mapping
        val mappingFileRoute = "versions/${launcherSettings.instanceSettings.minecraftVersion}.txt"
        val mappingFile = File(launcherSettings.launcherDirectories.librariesDirectory, mappingFileRoute)
        if (!mappingFile.exists() || getFileSHA1(mappingFile) != minecraftVersion.downloads.clientMappings.sha1) {
            println("downloading mapping")
            val mappingBody =
                AssetService.MINECRAFT_API.downloadFromUrl(minecraftVersion.downloads.clientMappings.url)
                    .execute().body()
            if (mappingBody != null) {

                mappingFile.parentFile.mkdirs()

                writeResponseBodyToDisk(mappingFile, mappingBody)
            }
        } else {
            println("mapping exists")
        }
    }

    fun downloadVanillaClient(minecraftVersion: MinecraftVersion) {
        // downloading client
        val clientFile = File(
            launcherSettings.launcherDirectories.librariesDirectory,
            "versions/${launcherSettings.instanceSettings.minecraftVersion}.jar"
        )

        if (!clientFile.exists() || getFileSHA1(clientFile) != minecraftVersion.downloads.client.sha1) {
            println("downloading client")

            val clientBody =
                AssetService.MINECRAFT_API.downloadFromUrl(minecraftVersion.downloads.client.url).execute()
                    .body()
            if (clientBody != null) {

                clientFile.parentFile.mkdirs()

                writeResponseBodyToDisk(clientFile, clientBody)
            }
        } else {
            println("client exists")
        }
    }

    fun downloadVanillaAssets(minecraftPackage: MinecraftPackage) {
        // downloading objects
        minecraftPackage.objects.forEach {
            println("downloading ${it.key}")

            val assetDownloadTask =
                MinecraftAssetDownloadTask(launcherSettings.launcherDirectories, it.key, it.value)

            while (!assetDownloadTask.download()) {
                println("retrying ${it.key}")
            }
        }
    }

    fun downloadVanilla() {
        val versionManifest = VersionManifestDownloadTask().download()

        if (versionManifest != null) {
            val minecraftVersion =
                MinecraftJsonDownloadTask(
                    launcherSettings.launcherDirectories,
                    versionManifest.versions.first { it.id == launcherSettings.instanceSettings.minecraftVersion }
                ).download()

            if (minecraftVersion != null) {
                downloadVanillaXml(minecraftVersion)
                downloadVanillaMapping(minecraftVersion)
                downloadVanillaClient(minecraftVersion)

                downloadLibraries(minecraftVersion.libraries)

                val minecraftPackage =
                    MinecraftPackageDownloadTask(
                        launcherSettings.launcherDirectories,
                        minecraftVersion.assetIndex
                    ).download()

                if (minecraftPackage != null) {
                    downloadVanillaAssets(minecraftPackage)
                }
            }
        }
    }

    fun downloadForgeFile(forgeFile: File) {
        // TODO 해시 체크
        if (!forgeFile.exists()) {
            val forgeDownloadTask = MinecraftForgeDownloadTask(
                launcherSettings.launcherDirectories,
                launcherSettings.instanceSettings.minecraftVersion,
                launcherSettings.instanceSettings.forgeVersion
            )
            val result = forgeDownloadTask.download()

            println("downloaded: $result")
        } else {
            println("forge already exists")
        }
    }

    fun downloadForgeInstaller(forgeInstallerFile: File, javaPath: String) {
        if (forgeInstallerFile.exists()) {
            runProcessAndWait(
                listOf(
                    javaPath,
                    "-jar",
                    forgeInstallerFile.absolutePath,
                    "--extract",
                    forgeInstallerFile.parentFile.absolutePath
                )
            )
        } else {
            println("exists ${forgeInstallerFile.exists()}")
            println("javaPath $javaPath")
        }
    }

    fun downloadForgeVersionLibraries() {
        // get version.json and download all libraries
        val minecraftForgeVersionFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.forgeInstallerFileFolder}/version.json"
        )
        val minecraftForgeVersion =
            gson.fromJson(minecraftForgeVersionFile.readText(), MinecraftForgeVersion::class.java)

        downloadLibraries(minecraftForgeVersion.libraries)
    }

    fun downloadLibraries(libraries: List<Library>) {
        libraries.forEach { library ->
            log.info("downloading ${library.name} (forge)")

            if (library.downloads.artifact.url.isNotEmpty()) { // 기본 포지는 url이 비어 있음
                var count = 0
                while (!MinecraftLibraryDownloadTask(
                        launcherSettings.launcherDirectories,
                        library.downloads.artifact
                    ).download()
                ) {
                    log.warn("retrying")
                    count++
                    if (count == 5) throw MinecraftException("failed to download libraries...")
                }

                count = 0
                library.downloads.classifiers?.forEach { (_, u) ->
                    while (!MinecraftLibraryDownloadTask(
                            launcherSettings.launcherDirectories,
                            u,
                            classifier = true
                        ).download()
                    ) {
                        log.warn("retrying")
                        count++
                        if (count == 5)
                            throw MinecraftException("failed to download libraries... (classifiers)")
                    }
                }
            } else {
                log.warn("no url $library")
            }
        }
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

    fun downloadForgeInstallProfile() {
        val javaPath = launcherSettings.javaRepository.primary?.absolutePath ?: throw MinecraftException("no java")

        val minecraftForgeInstallFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            "${launcherSettings.instanceSettings.forgeInstallerFileFolder}/install_profile.json"
        )
        val minecraftForgeInstall =
            gson.fromJson(minecraftForgeInstallFile.readText(), MinecraftForgeInstall::class.java)

        downloadLibraries(minecraftForgeInstall.libraries)

        val replacements = minecraftForgeInstall.processors
            .asSequence()
            .flatMap { it.args }
            .filter { it.startsWith("{") && it.endsWith("}") }
            .map { it.substring(1, it.length - 1) } // 중괄호 제거
            .filter { minecraftForgeInstall.data.containsKey(it) }
            .map { Pair("{$it}", minecraftForgeInstall.data[it]!!.client) }
            .toMap()


        minecraftForgeInstall.processors.forEach { processor ->
            val builder = CommandConcatenator(replacements, mapOf())

            fun getAbsolutePath(route: String): String {
                return File(
                    launcherSettings.launcherDirectories.librariesDirectory,
                    convertMavenStringToRoute(route)
                ).absolutePath
            }

            // java file
            builder.appendString(javaPath)
            builder.appendString("-jar")
            builder.appendString(getAbsolutePath(processor.jar))

            // classpath
            val cp = processor.classpath.joinToString(OS_CLASSPATH_SEPARATOR) { getAbsolutePath(it) } //+
//                    OS_CLASSPATH_SEPARATOR +
//                    getAbsolutePath(processor.jar)
            builder.appendString("-cp")
            builder.appendString(cp)
//            builder.appendString(OS_CLASSPATH_SEPARATOR)
//            builder.appendString(getAbsolutePath(processor.jar))
//            processor.classpath.forEach {
//                builder.appendString(getAbsolutePath(it))
//            }

            // args
            processor.args.forEach {
                builder.processLine(it)
            }
            builder.removeLastCharacterIfBlank()

            log.info(builder.toString())
            log.info("\n" + builder.toString().replace("jar:", "jar:\n").replace(" -", "\n -"))

            runProcessAndWait(builder.toList(), "UTF-8")

            // TODO check output hash
            // ~~~
        }
    }

    fun downloadForge() {
        val javaPath = launcherSettings.javaRepository.primary?.absolutePath
            ?: throw MinecraftException("no java found. stopping downloading forge")

        val forgeFile =
            File(launcherSettings.launcherDirectories.forgeDirectory, launcherSettings.instanceSettings.forgeFileName)
        downloadForgeFile(forgeFile)

        val forgeInstallerFile = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            launcherSettings.instanceSettings.forgeInstallerFileName
        )

        downloadForgeInstaller(forgeInstallerFile, javaPath)
        log.info("forgeFile: ${forgeFile.absolutePath} ${forgeFile.exists()}")

        // unzip
        if (!forgeInstallerFile.exists())
            throw MinecraftException("no forge found. stopping downloading forge")

        val dstFolder = File(
            launcherSettings.launcherDirectories.forgeDirectory,
            launcherSettings.instanceSettings.forgeInstallerFileFolder
        )

        unzipJar(forgeInstallerFile, dstFolder)
        log.info("forge installer unzipped")

        downloadForgeVersionLibraries()

        downloadForgeInstallProfile()
    }

    fun download() {
        downloadVanilla()
        downloadForge()
    }
}