package kr.goldenmine.inuminecraftlauncher.assets

import kr.goldenmine.inuminecraftlauncher.assets.assets.MinecraftAsset

data class MinecraftPackage(
    val objects: Map<String, MinecraftAsset>
) {
    fun printAll() {
        objects.forEach { (t, u) ->
            println("=== $t ===")
            println("hash: ${u.hash}")
            println("size: ${u.size}")
        }
    }
}