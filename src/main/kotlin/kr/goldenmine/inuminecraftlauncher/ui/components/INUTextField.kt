package kr.goldenmine.inuminecraftlauncher.ui.components

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.BorderFactory
import javax.swing.JTextField


class INUTextField(val hint: String) : JTextField(), FocusListener {
    private var showingHint = true

    init {
        background = Color.WHITE
        foreground = Color.GRAY.brighter()
        columns = 30
        border = BorderFactory.createCompoundBorder()

        addFocusListener(this)
    }

    override fun paintBorder(g: Graphics) {
        super.paintBorder(g)

        val g2d = g as Graphics2D
        g2d.stroke = BasicStroke(12f)
        g2d.color = Color.blue
        g2d.drawRoundRect(x, y, width - 1, height - 1, 25, 25)
    }

    override fun focusGained(e: FocusEvent?) {
        if(this.text?.isEmpty() == true) {
            super.setText("")
            showingHint = false
        }
    }

    override fun focusLost(e: FocusEvent?) {
        if(this.text?.isEmpty() == true) {
            super.setText(hint)
            showingHint = true
        }
    }

    override fun getText(): String? {
        return if (showingHint) "" else super.getText()
    }
}