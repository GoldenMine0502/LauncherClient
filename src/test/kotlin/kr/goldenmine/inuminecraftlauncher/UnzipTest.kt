package kr.goldenmine.inuminecraftlauncher

import kr.goldenmine.inuminecraftlauncher.util.Compress
import org.junit.jupiter.api.Test
import java.io.File

class UnzipTest {
    @Test
    fun unzip() {
        val file = File("java/mac/mac_jdk8_351.zip")
        val directory = File("java/mac")

        val checkExists = directory.listFiles()?.filter { it.isDirectory }?.size ?: 0

        if(checkExists == 0) {
            println("unzipping")
            val compress = Compress()
            compress.unZip(file.path, directory.path)
        }
    }
}