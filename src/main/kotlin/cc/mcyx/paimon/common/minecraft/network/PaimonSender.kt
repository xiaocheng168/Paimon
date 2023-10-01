package cc.mcyx.paimon.common.minecraft.network

import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 *  信息发送者
 */
class PaimonSender {
    companion object {

        /**
         *
         * 发送消息
         * @param sender 接收者
         * @param msg 消息内容
         */
        fun sendMessage(sender: CommandSender, msg: String) {
            sender.sendMessage(msg)
        }

        /**
         * 发送System Bar 该方法仅限1.8+
         * @param player 玩家
         * @param msg 发送内容
         */
        fun sendActionBar(player: Player, msg: String) {
            if (CraftBukkitPacket.serverId >= 180) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(msg))
            } else {
                throw UnsupportedClassVersionError("the version unsupported! 1.8+")
            }
        }

        /**
         *
         * 向玩家发送某个Paimon命令的帮助才是
         * @param sender 接收者
         * @param paimonCommand 命令批处理对象
         */
        fun sendCommandHelp(sender: CommandSender, paimonCommand: PaimonCommand) {
            val helpCommand = mutableListOf<String>()
            //根命令
            helpCommand.add("${paimonCommand.command} §a${paimonCommand.description}")

            helpCommand.addAll(findSendSubMessageHelp(paimonCommand))

            //发送帮助命令
            helpCommand.forEach {
                sendMessage(
                    sender,
                    "/${if (it.startsWith(paimonCommand.command)) it else "${paimonCommand.command} $it"}"
                )
            }

        }

        /**
         * 查找某个PaimonCommand命令中的所有子命令
         * @param paimonCommand 命令批处理对象
         * @return 返回查找到的子命令
         *
         */
        private fun findSendSubMessageHelp(paimonCommand: PaimonCommand): MutableList<String> {
            val subCmd = mutableListOf<String>()
            if (paimonCommand.subCommand.size <= 0) {
                return subCmd
            }

            //循环所有子命令
            for (sub in paimonCommand.subCommand.values) {
                subCmd.add("${paimonCommand.command} ${sub.command} §a${sub.description}")
                //找到子命令继续递归查找
                subCmd.addAll(findSendSubMessageHelp(sub))
            }
            return subCmd
        }
    }
}