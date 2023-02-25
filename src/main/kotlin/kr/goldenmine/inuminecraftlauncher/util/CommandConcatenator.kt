package kr.goldenmine.inuminecraftlauncher.util

import kr.goldenmine.inuminecraftlauncher.assets.version.arguments.Argument

class CommandConcatenator(
    private val replacements: Map<String, String>,
    private val features: Map<String, Boolean>,
) {
    private val builder = StringBuilder()

    fun removeLastCharacterIfBlank() {
        if (builder.lastOrNull() == ' ')
            builder.setLength(builder.length - 1)
    }

    fun appendString(str: String) {
        var replaced = false
        for (pair in replacements) {
            if (str.contains(pair.key)) {
                builder.append(str.replace(pair.key, pair.value))
                replaced = true
            }
        }
        if (!replaced)
            builder.append(str)
        builder.append(" ") // 공백
    }

    fun processLine(line: Any) {
        if (line is String) {
            appendString(line)
        } else if (line is Argument) {
            // 룰이 하나 이상 충족하는 경우
            val condition = line.rules.any {
                val isAllow = it.action == "allow"

                val isRulePass = if (it.features != null) // check feature
                    it.features.filter { feature -> features[feature.key] ?: false }.count() == it.features.size
                else if (it.os != null) { // check os
                    val osVersionNoDot = if (OS_VERSION.contains('.')) OS_VERSION.substring(
                        0,
                        OS_VERSION.lastIndexOf('.')
                    ) else OS_VERSION


                    val isNameCorrect =
                        it.os.name == null || (it.os.name == OS_NAME || it.os.name == osNameAlternative)
                    val isVersionCorrect = it.os.version == null || it.os.version.contains(osVersionNoDot)
                    val isArchCorrect = it.os.arch == null || it.os.arch == OS_ARCH

//                        println("$isNameCorrect $isVersionCorrect $isArchCorrect ${line.value} ")

                    isNameCorrect && isVersionCorrect && isArchCorrect
                } else {
                    false
                }

//                    println("total: $isRulePass")
                !(isAllow xor isRulePass)
            }

            // 값 적용
            if (condition) {
                if (line.value is String) {
//                        println("append: ${line.value}")
                    appendString(line.value)
                } else if (line.value is List<*>) {
                    line.value.forEach {
                        if (it == "-Dos.name=Windows 10") {
                            appendString("-Dos.name=\"Windows 10\"")
                        } else {
                            appendString(it as String)
                        }
                    }
                }
            }
        }
    }

    fun toList(): List<String> {
        return builder.toString().split(" ")
    }

    override fun toString(): String {
        return builder.toString()
    }
}