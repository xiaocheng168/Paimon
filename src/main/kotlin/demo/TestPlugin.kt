package demo

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.command.PaimonSubCommand
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayerManager
import cc.mcyx.paimon.common.minecraft.network.PaimonSender
import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.minecraft.network.ProxyPlayerManager
import cc.mcyx.paimon.common.ui.PaimonUI
import cc.mcyx.paimon.common.minecraft.network.sendCommandHelp
import cc.mcyx.paimon.common.minecraft.network.sendMessage
import cc.mcyx.paimon.common.ui.Sign
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
            PaimonCommand(this, "sign").paimonExec { sender, command, args ->
            Sign.openSign(sender as Player, arrayOf("文本1", "文本2", "文本3","文本4"))
            return@paimonExec true
        })
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
                        paimonUI.setItem(0, ItemStack(Material.APPLE)) {
                            println(it.slot)
                        }
                        //取消界面所有点击
                        paimonUI.clickEvent {
                            if (it.slot == 2) {
                                paimonPlayer.closeUI(0)
                            }
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