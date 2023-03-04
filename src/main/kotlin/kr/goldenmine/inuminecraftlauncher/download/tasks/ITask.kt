package kr.goldenmine.inuminecraftlauncher.download.tasks

interface ITask<T> {
    fun download(): T?
}