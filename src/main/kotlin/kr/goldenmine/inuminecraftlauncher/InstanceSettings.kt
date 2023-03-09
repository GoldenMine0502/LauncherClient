package kr.goldenmine.inuminecraftlauncher

class InstanceSettings(
    val minecraftVersion: String,
    val assetVersion: String,
    val forgeVersion: String,
    val javaVersion: Int,
    val javaVersionSpecific: Map<String, String>,
    val xms: Int,
    val xmx: Int,
    val instanceName: String,
    val ip: String,
    val port: Int,
    val mods: List<String>,
    val shader: String,
    val version: String,
) {
    fun getForgeInstallerFileName() = "forge-$minecraftVersion-$forgeVersion-installer.jar"
    fun getForgeInstallerFileFolder() = "forge-$minecraftVersion-$forgeVersion-installer"
    fun getForgeFileName() = "forge-$minecraftVersion-$forgeVersion.jar"
}