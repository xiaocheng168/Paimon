package demo

import cc.mcyx.paimon.common.Paimon
import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.command.PaimonSubCommand
import cc.mcyx.paimon.common.util.sendCommandHelp
import cc.mcyx.paimon.common.util.sendMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class TestPlugin : Paimon() {
    override fun onEnabled() {
        val rootCommand = PaimonCommand(this, "FastShop")
        rootCommand.description = "帮助命令"
        rootCommand.paimonExec { sender, c, args ->
            sendCommandHelp(sender, rootCommand)
            return@paimonExec true
        }
        rootCommand.addSubCommand(
            PaimonSubCommand(
                paimon = this,
                command = "open",
                rootCommand = rootCommand
            ).also { it ->

                it.addSubCommand(
                    PaimonSubCommand(
                        paimon = this,
                        command = "type",
                        rootCommand = rootCommand
                    ).also {
                        it.description = "[类型] 打开一个界面"
                    }.paimonTab { sender, command, args ->
                        val mutableListOf = mutableListOf<String>()
                        for (value in InventoryType.entries) {
                            mutableListOf.add(value.name)
                        }
                        return@paimonTab mutableListOf
                    }.paimonExec { sender, command, args ->
                        try {
                            val inventoryType = InventoryType.valueOf(args[args.size - 1])
                            val newGui = Bukkit.createInventory(null, inventoryType)
                            if (sender is Player) {
                                sender.openInventory(newGui)
                            }
                        } catch (e: Exception) {
                            sendMessage(sender, "打不来嘞类型 ${args[args.size - 1]}")
                        }
                        return@paimonExec true
                    }
                )

            })

        rootCommand.addSubCommand(PaimonSubCommand(
            paimon = this,
            command = "send",
            rootCommand = rootCommand
        ).also {
            it.description = "发送一条消息"
            it.paimonExec { sender, command, args ->
                sendMessage(sender, "发了哦！")
                return@paimonExec true
            }
        })

        rootCommand.register()
    }
}