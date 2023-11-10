package kr.goldenmine.inuminecraftlauncher.download.java

import net.technicpack.utilslib.OperatingSystem
import java.io.File

interface IJavaDownloader {
    val operatingSystem: OperatingSystem

    fun getFile(): File
    fun download()
    fun findAllExistingJava(): List<File>
}