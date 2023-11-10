package kr.goldenmine.inuminecraftlauncher.launcher.downloader.minecraft

interface IDownloader {
    val name: String
    fun download(): Boolean // 다운로드 성공 여부
}