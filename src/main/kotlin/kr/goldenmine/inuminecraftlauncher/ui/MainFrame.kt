package kr.goldenmine.inuminecraftlauncher.ui

import kr.goldenmine.inuminecraftlauncher.instances.InstanceList
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
    val instanceSelection: JComboBox<String>
//    private val idPasswordPanel = JPanel()

    init {
        val allInstances: Array<String> = InstanceList.values().map { it.displayName }.toTypedArray()
        instanceSelection = JComboBox<String>(allInstances)

        layout = null
        setWindowPositionMiddle(420, 380)
        defaultCloseOperation = EXIT_ON_CLOSE
//        idPasswordPanel.layout = null
//        microsoftPassword.echoChar = '*'
        logArea.isEditable = false
        val logAreaPane = JScrollPane(logArea)

//        idPasswordPanel.setBounds(10, 20, 200, 70)
//        microsoftId.setBounds(0, 0, 200, 30)
//        microsoftPassword.setBounds(0, 40, 200, 30)
        instanceSelection.setBounds(20, 15, 120, 25)
        loginMicrosoft.setBounds(20, 50, 290, 70)
        loginGuest.setBounds(320, 50, 80, 70)
        profileInfo.setBounds(20, 120, 380, 20)
        logoutMicrosoft.setBounds(20, 140, 380, 40)
        logArea.setBounds(20, 180, 380, 150)
        logAreaPane.setBounds(20, 180, 380, 150)

//        idPasswordPanel.add(microsoftId)
//        idPasswordPanel.add(microsoftPassword)

//        add(idPasswordPanel)
        add(instanceSelection)
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