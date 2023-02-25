package kr.goldenmine.inuminecraftlauncher.download

import kr.goldenmine.inuminecraftlauncher.assets.AssetService
import kr.goldenmine.inuminecraftlauncher.assets.MinecraftVersionManifest

class VersionManifestDownloadTask(

): ITask<MinecraftVersionManifest> {
    override fun download(): MinecraftVersionManifest? {
        val call = AssetService.MINECRAFT_API.getVersionManifest()
        val response = call.execute()

        return response.body()
    }
}