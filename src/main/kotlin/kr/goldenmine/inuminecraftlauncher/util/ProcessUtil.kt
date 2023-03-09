package kr.goldenmine.inuminecraftlauncher.util

import net.technicpack.utilslib.OperatingSystem
import java.io.BufferedReader
import java.io.InputStreamReader


fun runProcessAndWait(commands: List<String>, encoding: String = "MS949"): Int {
    if(OperatingSystem.getOperatingSystem() == OperatingSystem.WINDOWS) {
        System.setProperty("jdk.lang.Process.launchMechanism", "PIPE")
    }
    if (OperatingSystem.getOperatingSystem() == OperatingSystem.OSX) {
        System.setProperty("jdk.lang.Process.launchMechanism", "FORK")
    }

    val processBuilder = ProcessBuilder(commands.toList())//.redirectErrorStream(true) // for encoding
    val process = processBuilder.start()

    readTextFromStream(BufferedReader(InputStreamReader(process.inputStream, encoding)))
    readTextFromStream(BufferedReader(InputStreamReader(process.errorStream, encoding)))

    return process.waitFor()
}

fun readTextFromStream(reader: BufferedReader) {
    Thread {
        var line: String?
        while (run {
                line = reader.readLine()
                line
            } != null) {
            println(line)
        }
    }.start()
}