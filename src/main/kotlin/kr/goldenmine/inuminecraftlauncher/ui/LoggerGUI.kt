package kr.goldenmine.inuminecraftlauncher.ui

import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter

class LoggerGUI(private val mainFrame: MainFrame): Loggable {
    override fun info(text: String?) {
        synchronized(this) {
            mainFrame.logArea.append("$text\n")
        }
    }

    override fun warn(text: String?) {
        synchronized(this) {
            mainFrame.logArea.append("warn: $text\n")
        }
    }

    override fun error(text: String?, ex: Throwable?) {
        mainFrame.logArea.append("error: $text\n")
        if(ex != null) {
            val stringWriter = StringWriter()
            ex.printStackTrace(PrintWriter(stringWriter))
            mainFrame.logArea.append("cause: $stringWriter")
        }
    }
}