package kr.goldenmine.inuminecraftlauncher

class InstanceSettings(
    val minecraftVersion: String,
    val assetVersion: String,
    val forgeVersion: String,
    val javaVersion: Int,
    val forgeMods: Boolean
) {
    val forgeInstallerFileName = "forge-$minecraftVersion-$forgeVersion-installer.jar"
    val forgeInstallerFileFolder = "forge-$minecraftVersion-$forgeVersion-installer"
    val forgeFileName = "forge-$minecraftVersion-$forgeVersion.jar"
}