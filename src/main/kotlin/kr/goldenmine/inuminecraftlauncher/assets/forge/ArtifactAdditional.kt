package kr.goldenmine.inuminecraftlauncher.assets.forge

import java.io.File

class ArtifactAdditional {
    private val domain: String
    private val name: String
    private val version: String
    private val classifier: String?
    private val ext: String

    //Caches so we don't rebuild every time we're asked.
    private val path: String
    private val file: String
    private val descriptor: String


    constructor(descriptor: String) {
        this.descriptor = descriptor

        val pts = descriptor.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        domain = pts[0]
        name = pts[1]
        val last = pts.size - 1
        val idx = pts[last].indexOf('@')
        if (idx != -1) {
            ext = pts[last].substring(idx + 1)
            pts[last] = pts[last].substring(0, idx)
        } else {
            ext = "jar"
        }
        version = pts[2]

        classifier = if(pts.size > 3) pts[3] else null
        file = "$name-$version" + (if(classifier != null) "-$classifier" else "") + '.' + ext

        this.path = this.domain.replace('.', '/') + '/' + name + '/' + version + '/' + file
    }

    fun getLocalPath(base: File): File {
        return File(base, path.replace('/', File.separatorChar))
    }
}