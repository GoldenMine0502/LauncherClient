package kr.goldenmine.inuminecraftlauncher.download.java

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.*


object JavaDownloaderRequest {
    val SERVICE: JavaDownloaderService = Retrofit.Builder()
        .baseUrl("http://minecraft.goldenmine.kr:20200/")
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(JavaDownloaderService::class.java)
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