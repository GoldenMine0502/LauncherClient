package kr.goldenmine.inuminecraftlauncher.util

import okhttp3.ResponseBody
import java.io.*

class RequestUtil {
}


@Throws(IOException::class)
fun writeResponseBodyToDisk(dst: File, body: ResponseBody, bufferSize: Int = 1024) {
    if (!dst.exists()) dst.createNewFile()

    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        val fileReader = ByteArray(bufferSize)
        inputStream = body.byteStream()
        outputStream = FileOutputStream(dst)
        while (true) {
            val read = inputStream.read(fileReader)
            if (read == -1) break

            outputStream.write(fileReader, 0, read)
        }
        outputStream.flush()
    } finally {
        inputStream?.close()
        outputStream?.close()
    }
}