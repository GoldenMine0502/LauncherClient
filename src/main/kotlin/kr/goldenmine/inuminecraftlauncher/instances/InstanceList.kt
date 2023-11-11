package kr.goldenmine.inuminecraftlauncher.instances

import kr.goldenmine.inuminecraftlauncher.instances.strategy.DownloadStrategy
import kr.goldenmine.inuminecraftlauncher.instances.strategy.StrategyHigh
import kr.goldenmine.inuminecraftlauncher.instances.strategy.StrategyLow

enum class InstanceList(
    val instanceName: String,
    val displayName: String,
    val strategy: DownloadStrategy
) {
    INU1165low("inu1165low", "저사양", StrategyLow()),
    INU1165high("inu1165", "고사양", StrategyHigh())

}

fun getInstanceName(displayName: String): String? {
    return InstanceList.values().firstOrNull { it.displayName == displayName }?.instanceName
}

fun getFirstInstance(): InstanceList {
    if(InstanceList.values().isEmpty()) throw RuntimeException("no instance")

    return InstanceList.values().first()
}