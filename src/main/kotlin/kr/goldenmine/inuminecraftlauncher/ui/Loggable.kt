package kr.goldenmine.inuminecraftlauncher.ui

interface Loggable {
    fun info(text: String?)
    fun warn(text: String?)
    fun error(text: String?, ex: Throwable? = null)
}