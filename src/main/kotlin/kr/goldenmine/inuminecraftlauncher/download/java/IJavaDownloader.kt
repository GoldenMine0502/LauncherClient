package kr.goldenmine.inuminecraftlauncher.download.java

import net.technicpack.utilslib.OperatingSystem
import java.io.File

interface IJavaDownloader {
    val destFile: File
    val requestFileName: String
    val javaRoute: String
    val operatingSystem: OperatingSystem
    fun download()
    fun findAllExistingJava(): List<File>
    fun getJavaVersionName(version: Int): String
}