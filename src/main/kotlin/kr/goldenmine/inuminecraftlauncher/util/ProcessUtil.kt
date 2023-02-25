package kr.goldenmine.inuminecraftlauncher.util

import java.io.BufferedReader
import java.io.InputStreamReader


fun runProcessAndWait(commands: List<String>, encoding: String = "MS949"): Int {
    val processBuilder = ProcessBuilder(commands.toList())//.redirectErrorStream(true)
    val process = processBuilder.start()

    val reader = BufferedReader(InputStreamReader(process.inputStream, encoding))
    val errorReader = BufferedReader(InputStreamReader(process.errorStream, encoding))

    Thread {
        var line: String?
        while (run {
                line = reader.readLine()
                line
            } != null) {
            println(line)
        }
    }.start()

    Thread {
        var line: String?
        while (run {
                line = errorReader.readLine()
                line
            } != null) {
            println(line)
        }
    }.start()
    return process.waitFor()
}