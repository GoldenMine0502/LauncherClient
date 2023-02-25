package kr.goldenmine.inuminecraftlauncher.download

import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.assets.version.libraries.Artifact
import kr.goldenmine.inuminecraftlauncher.download.java.writeResponseBodyToDisk
import kr.goldenmine.inuminecraftlauncher.launcher.LauncherDirectories
import kr.goldenmine.inuminecraftlauncher.util.getFileSHA1
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class MinecraftLibraryDownloadTask(
    private val launcherDirectories: LauncherDirectories,
    private val artifact: Artifact,
    private val classifier: Boolean = false
): ITask<Boolean> {

    private val log: Logger = LoggerFactory.getLogger(MinecraftLibraryDownloadTask::class.java)

    override fun download(): Boolean {
        val directory = if(classifier) launcherDirectories.temporaryDirectory else launcherDirectories.librariesDirectory
        
        val file = File(directory, artifact.path)
        file.parentFile.mkdirs()

        if(checkHash(file)) {
            log.info("already the library exists: ${artifact.path}")
            return true
        }

        val execute = AssetService.MINECRAFT_API.downloadFromUrl(artifact.url).execute()
        val body = execute.body()

        if (body != null) {
            writeResponseBodyToDisk(file, body)
            return checkHash(file)
        } else {
            log.error(execute.errorBody().toString())
        }

        return false
    }

    fun checkHash(file: File): Boolean {
        if (file.exists()) {
            val sha1 = getFileSHA1(file)

            return sha1 == artifact.sha1 && file.length() == artifact.size
        }

        return false
    }
}