package demo

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.command.PaimonSubCommand
import cc.mcyx.paimon.common.ui.PaimonUI
import cc.mcyx.paimon.common.util.sendCommandHelp
import cc.mcyx.paimon.common.util.sendMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

class TestPlugin : PaimonPlugin() {
    override fun onEnabled() {
        val rootCommand = PaimonCommand(this, "FastShop")
        rootCommand.description = "帮助命令"
        rootCommand.paimonExec { sender, _, _ ->
            sendCommandHelp(sender, rootCommand)
            return@paimonExec true
        }
        rootCommand.addSubCommand(
            PaimonSubCommand(
                paimon = this,
                command = "open"
            ).also { it ->

                it.paimonExec { sender, command, args ->
                    if (sender is Player) {

                        PaimonUI(InventoryType.HOPPER, "halo").open(sender)
                            .open {
                                println(it)
                            }.click {
                                it.isCancelled = true
                            }.setButton(0, ItemStack(Material.APPLE)) { player, slot, item ->
                                player.kickPlayer("你！")
                            }.setButton(2, ItemStack(Material.BOOK)) { player, slot, item ->
                                player.sendMessage("你点了我这个物品！！！")
                            }

                    }
                    return@paimonExec true
                }

                it.addSubCommand(
                    PaimonSubCommand(
                        paimon = this,
                        command = "type"
                    ).also {
                        it.description = "[类型] 打开一个界面"
                    }.paimonTab { _, _, _ ->
                        val mutableListOf = mutableListOf<String>()
                        for (value in InventoryType.entries) {
                            mutableListOf.add(value.name)
                        }
                        return@paimonTab mutableListOf
                    }.paimonExec { sender, _, args ->
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
            command = "send"
        ).also {
            it.description = "发送一条消息"
            it.paimonExec { sender, _, _ ->
                sendMessage(sender, "发了哦！")
                return@paimonExec true
            }
        })

        rootCommand.register()
    }
}