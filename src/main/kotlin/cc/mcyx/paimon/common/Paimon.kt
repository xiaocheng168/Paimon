package cc.mcyx.paimon.common

import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.util.loadPlugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Paimon Plugin 主类
 */
open class Paimon : JavaPlugin() {
    val cl = classLoader
    final override fun onEnable() {
        loadPlugin(this)


        PaimonCommand(this, "paimonUI4", "cc.mcyx.fast.gui.shop").paimonExec { sender, command, args ->
            return@paimonExec true
        }.paimonTab { sender, command, args ->
            println(sender)
            return@paimonTab mutableListOf<String>()
        }.addSubCommand(

            PaimonCommand(this, "halo", "cc.mcyx.cc").paimonExec { sender, command, args ->
                println("halo")
                return@paimonExec true
            }.paimonTab { sender, command, args ->
                println("tab!!!")
                return@paimonTab mutableListOf<String>()
            }
        ).addSubCommand(PaimonCommand(this, "wa", "cc.ncyc.wa").paimonExec { sender, command, args ->
            println("Wa")
            return@paimonExec true
        }).paimonTab { sender, command, args ->
            println("wa tab")
            return@paimonTab mutableListOf<String>()
        }.register()

        this.onEnabled()
    }


    open fun onEnabled() {}
}