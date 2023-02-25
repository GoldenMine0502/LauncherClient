package kr.goldenmine.inuminecraftlauncher.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*

fun getFileMD5(file: File): String {
    val inputStream = FileInputStream(file)
    val messageDigest = MessageDigest.getInstance("MD5")

    val bytes = inputStream.readBytes()
    inputStream.close()

    messageDigest.update(bytes)
    val digest = messageDigest.digest()
    return Base64.getEncoder().encodeToString(digest)
}