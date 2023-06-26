package kr.goldenmine.inuminecraftlauncher.ui

import lombok.extern.slf4j.Slf4j
import java.awt.GraphicsEnvironment
import javax.swing.*

@Slf4j
class MainFrame(version: String?) : JFrame("INU Minecraft Launcher $version") {
//    val microsoftId = JTextField()
//    val microsoftPassword = JPasswordField()
    val loginMicrosoft = JButton("마이크로소프트로 로그인")
    val logoutMicrosoft = JButton("로그아웃")
    val loginGuest = JButton("게스트")
    val profileInfo = JLabel("계정: ")
    val logArea = JTextArea()
//    private val idPasswordPanel = JPanel()

    init {
        layout = null
        setWindowPositionMiddle(420, 350)
        defaultCloseOperation = EXIT_ON_CLOSE
//        idPasswordPanel.layout = null
//        microsoftPassword.echoChar = '*'
        logArea.isEditable = false
        val logAreaPane = JScrollPane(logArea)

//        idPasswordPanel.setBounds(10, 20, 200, 70)
//        microsoftId.setBounds(0, 0, 200, 30)
//        microsoftPassword.setBounds(0, 40, 200, 30)
        loginMicrosoft.setBounds(20, 20, 290, 70)
        loginGuest.setBounds(320, 20, 80, 70)
        profileInfo.setBounds(20, 90, 380, 20)
        logoutMicrosoft.setBounds(20, 110, 380, 40)
        logArea.setBounds(20, 150, 380, 150)
        logAreaPane.setBounds(20, 150, 380, 150)

//        idPasswordPanel.add(microsoftId)
//        idPasswordPanel.add(microsoftPassword)

//        add(idPasswordPanel)
        add(loginMicrosoft)
        add(loginGuest)
        add(profileInfo)
        add(logoutMicrosoft)
        contentPane.add(logAreaPane)

//        microsoftId.isVisible = true
//        microsoftPassword.isVisible = true
        loginMicrosoft.isVisible = true
        loginGuest.isVisible = true
        isVisible = true
    }

    fun setWindowPositionMiddle(width: Int, height: Int) {
        val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        val monitorWidth = gd.displayMode.width
        val monitorHeight = gd.displayMode.height
        val x = monitorWidth / 2 - width / 2
        val y = monitorHeight / 2 - height / 2
        setBounds(x, y, width, height)
    }
}