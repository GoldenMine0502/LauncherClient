package kr.goldenmine.inuminecraftlauncher.ui

import kr.goldenmine.inuminecraftlauncher.ui.LoggerGUI
import kr.goldenmine.inuminecraftlauncher.ui.MainFrame
import javax.swing.JPanel

class DefaultLoggerGUI(private val mainFrame: MainFrame): LoggerGUI {
    override fun log(text: String?) {
        synchronized(this) {
            mainFrame.logArea.append("$text\n")
        }
    }
}