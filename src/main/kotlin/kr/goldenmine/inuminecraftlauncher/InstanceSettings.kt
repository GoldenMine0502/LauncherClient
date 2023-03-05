package kr.goldenmine.inuminecraftlauncher

class InstanceSettings(
    val minecraftVersion: String,
    val assetVersion: String,
    val forgeVersion: String,
    val javaVersion: Int,
    val javaVersionSpecific: Map<String, String>,
    val instanceName: String,
    val ip: String,
    val port: Int,
    val mods: List<String>,
) {
    fun getForgeInstallerFileName() = "forge-$minecraftVersion-$forgeVersion-installer.jar"
    fun getForgeInstallerFileFolder() = "forge-$minecraftVersion-$forgeVersion-installer"
    fun getForgeFileName() = "forge-$minecraftVersion-$forgeVersion.jar"
}