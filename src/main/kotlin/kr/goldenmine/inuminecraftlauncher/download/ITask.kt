package kr.goldenmine.inuminecraftlauncher.download

interface ITask<T> {
    fun download(): T?
}