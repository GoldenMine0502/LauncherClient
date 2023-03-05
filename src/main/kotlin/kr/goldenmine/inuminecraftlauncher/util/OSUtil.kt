package kr.goldenmine.inuminecraftlauncher.util

import net.technicpack.utilslib.OperatingSystem

const val OS_NAME_WINDOWS = "windows"
const val OS_NAME_MAC = "macos"
const val OS_NAME_LINUX = "linux"

val OS_NAME = when (OperatingSystem.getOperatingSystem()) {
    OperatingSystem.WINDOWS -> OS_NAME_WINDOWS
    OperatingSystem.OSX -> OS_NAME_MAC
    OperatingSystem.LINUX -> OS_NAME_LINUX
    OperatingSystem.UNKNOWN -> OS_NAME_WINDOWS // OS 감지 실패시 윈도우로 적용
    null -> OS_NAME_WINDOWS
}


val osNameAlternative = if (OS_NAME == OS_NAME_MAC) "osx" else OS_NAME

val OS_ARCH = System.getProperty("os.arch")

val OS_VERSION = System.getProperty("os.version")

val OS_CLASSPATH_SEPARATOR = if (OperatingSystem.getOperatingSystem() == OperatingSystem.WINDOWS) ";" else ":"