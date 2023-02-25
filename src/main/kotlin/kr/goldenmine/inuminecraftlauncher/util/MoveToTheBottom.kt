package kr.goldenmine.inuminecraftlauncher.util

import java.util.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

class MoveToTheBottom constructor(private val parent: JTextComponent?) : DocumentListener {
        init {
            parent!!.document.addDocumentListener(this)
        }

        override fun insertUpdate(e: DocumentEvent) {
            parent!!.caretPosition = e.document.length
        }

        override fun removeUpdate(e: DocumentEvent) {
            parent!!.caretPosition = e.document.length
        }

        override fun changedUpdate(e: DocumentEvent) {
            parent!!.caretPosition = e.document.length
        }

        companion object {
            private val registry = WeakHashMap<JTextComponent?, DocumentListener>(25)
            fun install(parent: JTextComponent?) {
                val bottom = MoveToTheBottom(parent)
                registry[parent] = bottom
            }

            fun uninstall(parent: JTextComponent) {
                val listener = registry.remove(parent)
                if (listener != null) {
                    parent.document.removeDocumentListener(listener)
                }
            }
        }
    }