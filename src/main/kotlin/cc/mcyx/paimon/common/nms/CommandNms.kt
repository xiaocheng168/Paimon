package cc.mcyx.paimon.common.nms

import cc.mcyx.paimon.common.command.PaimonCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.SimpleCommandMap

val craftServer: Class<*> = Class.forName(NMSPacket.craftServer)

val simpleCommandMap: SimpleCommandMap =
    craftServer.getDeclaredField("commandMap")
        .also { it.isAccessible = true }.get(Bukkit.getServer()) as SimpleCommandMap

/**
 * 注册命令
 * @param paimonCommand 注册的批处理对象
 */
fun registerCommand(paimonCommand: PaimonCommand) {
    simpleCommandMap.register(
        paimonCommand.paimon.name,
        paimonCommand
    )
}

@Deprecated("等待", ReplaceWith("TODO(\"无法使用\")"))
fun removeCommand(paimonCommand: PaimonCommand) {
    TODO("无法使用")
}