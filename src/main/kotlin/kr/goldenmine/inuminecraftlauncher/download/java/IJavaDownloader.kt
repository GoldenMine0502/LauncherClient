package kr.goldenmine.inuminecraftlauncher.download.java

import kr.goldenmine.inuminecraftlauncher.util.runProcessAndGetAllText
import net.technicpack.utilslib.OperatingSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.regex.Pattern

private val versionSplitPattern = Pattern.compile("[\\._]")
private val log: Logger = LoggerFactory.getLogger(IJavaDownloader::class.java)

interface IJavaDownloader {
    val operatingSystem: OperatingSystem

    fun download()
    fun findAllExistingJava(): List<File>

    fun checkVersionSame(javaPath: File, javaVersion: Int): Boolean {
        // example
        // java version "17.0.6" 2023-01-17 LTS
        // Java(TM) SE Runtime Environment (build 17.0.6+9-LTS-190)
        // Java HotSpot(TM) 64-Bit Server VM (build 17.0.6+9-LTS-190, mixed mode, sharing)
        val text = runProcessAndGetAllText(listOf(javaPath.absolutePath, "-version"))[0]
        val javaString = text.substring(text.indexOf('\"') + 1, text.lastIndexOf('\"'))
        val split = javaString.split(versionSplitPattern).map { it.toInt() }
        log.info("$javaString $split")

        return if(javaVersion < 10) {
            split[0] == 1 && split[1] == javaVersion
        } else { // after java 11
            split[0] == javaVersion
        }
    }
}