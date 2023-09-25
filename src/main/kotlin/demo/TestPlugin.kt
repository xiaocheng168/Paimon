package demo

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.command.PaimonSubCommand
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayerManager
import cc.mcyx.paimon.common.minecraft.network.PaimonSender
import cc.mcyx.paimon.common.ui.PaimonUI
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
            PaimonSender.sendCommandHelp(sender, rootCommand)
            return@paimonExec true
        }
        rootCommand.addSubCommand(
            PaimonSubCommand(
                paimon = this,
                command = "open"
            ).also { it ->

                it.paimonExec { sender, command, args ->
                    if (sender is Player) {
                        val paimonPlayer = PaimonPlayerManager.getPaimonPlayer(sender)
                        val paimonUI = PaimonUI(PaimonUI.PaimonUIType.ANVIL).open(paimonPlayer)
                        //单独对这个按钮做操作
                        paimonUI.setItem(2, ItemStack(Material.APPLE)) {
                            it.isCancel = true
                        }
                        //取消界面所有点击
                        paimonUI.clickEvent { it.isCancel = true }
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
                            PaimonSender.sendMessage(sender, "打不来嘞类型 ${args[args.size - 1]}")
                        }
                        return@paimonExec true
                    }
                )
            })

        rootCommand.addSubCommand(
            PaimonSubCommand(
                paimon = this,
                command = "send"
            ).also {
                it.description = "发送一条消息"
                it.paimonExec { sender, _, _ ->
                    PaimonSender.sendMessage(sender, "发了哦！")
                    return@paimonExec true
                }
            })
        rootCommand.register()
    }
}