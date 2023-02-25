package kr.goldenmine.inuminecraftlauncher.util

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.math.pow

class Compress {
    private val log: Logger = LoggerFactory.getLogger(Compress::class.java)
    /**
     * Windows 환경에서 동작 보장
     *
     * 압축(Zip)
     */
    fun zip(files: List<File>, targetPath: String) {
//        log.info("call zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(targetPath))).use { output ->
            files.forEach { file ->
//                log.info(file.absolutePath)
                executeZip(file, output)
            }
        }
    }

    /**
     * Windows 동작 보장
     *
     * 압축 해제(zip)
     */
    fun unZip(zipFilePath: String, targetPath: String) {
//        log.info("call unZip")
        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) {
                    File(targetPath, entry.name).mkdirs()
                } else {
                    zip.getInputStream(entry).use { input ->
                        File(targetPath, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    /**
     * Windows, Linux 동작 보장
     *
     * 묶음(tar)
     */
    fun tar(files: List<File>, targetPath: String): File? {
        log.info("call tar")
        try {
            // 압축하고자 하는 타겟이 폴더 1개일 때는 폴더 이하 리스트를 work list에 넣는다.
            var workList = if (files.size == 1 && files[0].isDirectory) files[0].listFiles()?.toList() else null
            // 타겟이 폴더 1개지만 폴더 이하 리스트가 없을 경우는 원래값을 work list에 넣는다.
            if (workList.isNullOrEmpty()) workList = files

            TarArchiveOutputStream(BufferedOutputStream(FileOutputStream(targetPath))).use { output ->
                workList.forEach { file ->
                    log.info(file.absolutePath)
                    executeTar(file, output)
                }
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
        return File(targetPath)
    }

    /**
     * Windows, Linux 동작 보장
     *
     * 묶음 해제(tar)
     */
    fun unTar(tarFilePath: String, targetPath: String): String? {
        log.info("call unTar")
        try {
            TarArchiveInputStream(BufferedInputStream(FileInputStream(tarFilePath))).use { tar ->
                var entry = tar.nextTarEntry
                while (entry != null) {
                    if (entry.isDirectory) {
                        File(targetPath, entry.name).mkdirs()
                    } else {
                        File(targetPath, entry.name).outputStream().use { output ->
                            tar.copyTo(output)
                        }
                    }
                    entry = tar.nextTarEntry
                }
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
        return targetPath
    }

    /**
     * Windows, Linux 동작 보장
     *
     * 압축 해제(tar.gz -> tar)
     */
    fun unGzip(gzipFilePath: String, targetPath: String): File? {
        val fileName: String?
        try {
            fileName = gzipFilePath.substringAfterLast(File.separator).split(".tar.gz")[0]
            GzipCompressorInputStream(BufferedInputStream(FileInputStream(gzipFilePath))).use { gzip ->
                File(targetPath, "$fileName.tar").outputStream().use { output ->
                    gzip.copyTo(output)
                }
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
        return File(targetPath, "$fileName.tar")
    }

    /**
     * Windows, Linux 동작 보장
     *
     * 압축 해제(tar.gz)
     */
    fun unTarGzip(tarGzipFilePath: String, targetPath: String): Boolean {
        try {
            unGzip(tarGzipFilePath, targetPath)?.let {
                unTar(it.absolutePath, targetPath)?.apply {
                    log.info("tarGzip success path : $this")
                }.apply {
                    it.delete()
                }
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            return false
        }
        return true
    }

    /**
     * Linux 동작 보장
     *
     * 압축(tar.gz)
     */
    fun tarGzip(files: List<File>, targetPath: String): File? {
        log.info("call tarGzip")
        return tar(files, "$targetPath.tar")?.let {
            log.info("tar success : ${it.name}, ${it.length() / 1024.0.pow(2)} MB")
            gzip(it, "$targetPath.tar.gz")?.apply {
                log.info("tarGzip success : ${name}, ${length() / 1024.0.pow(2)} MB")
            }.apply {
                it.delete()
            }
        }
    }

    private fun executeZip(file: File, zipOut: ZipOutputStream, parentPath: String = "") {
        if (file.isDirectory) {
            val entryPath = parentPath + File.separator + file.name
            zipOut.putNextEntry(ZipEntry(if (file.name.endsWith("/")) entryPath else entryPath + File.separator))
            zipOut.closeEntry()
            file.listFiles()?.let {
                it.toList().forEach { f -> executeZip(f, zipOut, entryPath) }
            }
        } else {
            val entry = ZipEntry(parentPath + File.separator + file.name)
            zipOut.putNextEntry(entry)
            FileInputStream(file).use { fileInputStream ->
                BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                    bufferedInputStream.copyTo(zipOut)
                }
            }
        }
    }

    private fun gzip(file: File, targetPath: String): File? {
        log.info("call gzip")
        try {
            GzipCompressorOutputStream(BufferedOutputStream(FileOutputStream(targetPath))).use { output ->
                FileInputStream(file).use { fileInputStream ->
                    BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                        bufferedInputStream.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
        return File(targetPath)
    }

    private fun executeTar(file: File, zipOut: TarArchiveOutputStream, parentPath: String = "") {
        log.info("file : ${file.absolutePath} parent : $parentPath")
        if (file.isDirectory) {
            val entryPath = parentPath + File.separator + if (file.name.endsWith(File.separatorChar)) {
                file.name
            } else {
                file.name + File.separator
            }
            zipOut.putArchiveEntry(TarArchiveEntry(entryPath))
            zipOut.closeArchiveEntry()
            file.listFiles()?.let {
                it.toList().forEach { f -> executeTar(f, zipOut, entryPath.substringBeforeLast(File.separatorChar)) }
            }
        } else {
            zipOut.putArchiveEntry(TarArchiveEntry("$parentPath${File.separator}${file.name}").apply {
                size = file.length()
            })
            FileInputStream(file).use { fileInputStream ->
                BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                    bufferedInputStream.copyTo(zipOut)
                }
            }
            zipOut.closeArchiveEntry()
        }
    }
}