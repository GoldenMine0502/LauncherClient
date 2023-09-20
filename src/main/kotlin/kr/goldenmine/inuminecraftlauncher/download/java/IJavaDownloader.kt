package kr.goldenmine.inuminecraftlauncher.download.java

import net.technicpack.utilslib.OperatingSystem
import java.io.File

interface IJavaDownloader {
    val javaRoute: String
    val operatingSystem: OperatingSystem

    fun getFile(): File
    fun download()
    fun findAllExistingJava(): List<File>
    fun getJavaVersionName(version: Int): List<String>
}