package kr.goldenmine.inuminecraftlauncher.instances

import kr.goldenmine.inuminecraftlauncher.instances.strategy.DownloadStrategy
import kr.goldenmine.inuminecraftlauncher.instances.strategy.StrategyHigh
import kr.goldenmine.inuminecraftlauncher.instances.strategy.StrategyLow

enum class InstanceList(
    val instanceName: String,
    val displayName: String,
    val strategy: DownloadStrategy,
    val default: Boolean = false
) {
    INU1165_low("inu1165low", "저사양", StrategyLow(), default=true),
    INU1165_high("inu1165", "고사양", StrategyHigh())

}

fun getInstanceName(displayName: String): String? {
    return InstanceList.values().firstOrNull { it.displayName == displayName }?.instanceName
}

fun getDefaultInstance(): InstanceList {
    if(InstanceList.values().count { it.default } != 1) throw RuntimeException("기본 인스턴스가 없거나 너무 많습니다.")

    return InstanceList.values().first { it.default }
}