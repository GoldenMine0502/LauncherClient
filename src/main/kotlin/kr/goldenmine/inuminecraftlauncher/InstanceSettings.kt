package kr.goldenmine.inuminecraftlauncher

class InstanceSettings(
    val minecraftVersion: String,
    val assetVersion: String,
    val forgeVersion: String,
    val javaVersion: Int,
    val javaVersionSpecific: Map<String, String>,
    val instanceName: String,
    val mods: List<String>,
) {
    @Transient val forgeInstallerFileName = "forge-$minecraftVersion-$forgeVersion-installer.jar"
    @Transient val forgeInstallerFileFolder = "forge-$minecraftVersion-$forgeVersion-installer"
    @Transient val forgeFileName = "forge-$minecraftVersion-$forgeVersion.jar"
}