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

        if(ex != null) {
            val stringWriter = StringWriter()
            ex.printStackTrace(PrintWriter(stringWriter))
            synchronized(this) {
                mainFrame.logArea.append("error: $text\n")
                mainFrame.logArea.append("cause: $stringWriter")
            }
        } else {
            synchronized(this) {
                mainFrame.logArea.append("error: $text\n")
            }
        }
    }
}