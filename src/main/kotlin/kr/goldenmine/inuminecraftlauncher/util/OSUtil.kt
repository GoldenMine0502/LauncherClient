package kr.goldenmine.inuminecraftlauncher.util

import net.technicpack.utilslib.OperatingSystem

val OS_NAME = when (OperatingSystem.getOperatingSystem()) {
    OperatingSystem.WINDOWS -> "windows"
    OperatingSystem.OSX -> "macos"
    OperatingSystem.LINUX -> "linux"
    OperatingSystem.UNKNOWN -> "windows" // OS 감지 실패시 윈도우로 적용
    null -> "windows"
}


val osNameAlternative = if (OS_NAME == "macos") "osx" else OS_NAME

val OS_ARCH = System.getProperty("os.arch")

val OS_VERSION = System.getProperty("os.version")

val OS_CLASSPATH_SEPARATOR = if (OperatingSystem.getOperatingSystem() == OperatingSystem.WINDOWS) ";" else ":"