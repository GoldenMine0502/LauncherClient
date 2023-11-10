package kr.goldenmine.inuminecraftlauncher.instances.strategy

interface DownloadStrategy {
    fun deletePreviousFile()
    fun download()
}