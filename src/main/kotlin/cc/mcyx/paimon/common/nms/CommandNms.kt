package cc.mcyx.paimon.common.nms

import cc.mcyx.paimon.common.Paimon
import cc.mcyx.paimon.common.command.PaimonCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.SimpleCommandMap

//CraftServer Class
val craftServer: Class<*> = Class.forName(NMSPacket.craftServer)

//Craft SimpleCommandMap 命令表对象
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

/**
 * 获取CraftServer中的命令集合
 * @return 命令表
 */
fun getKnownCommands(): HashMap<String, Command> {
    return SimpleCommandMap::class.java.getDeclaredField("knownCommands").also { it.isAccessible = true }
        .get(simpleCommandMap) as HashMap<String, Command>
}

/**
 * 删除某个Paimon命令
 * @param command 命令批处理对象
 */
fun removeCommand(command: Command) {
    getKnownCommands().remove(command.name, command)
}

/**
 * 卸载某个Paimon插件注册的所有命令
 * @param paimon 插件主体
 */
fun removePluginCommand(paimon: Paimon) {
    val pluginName = paimon.name.lowercase()
    val knownCommands = getKnownCommands()
    knownCommands.values.filter { it.label.startsWith(pluginName) }.forEach { removeCommand(it) }
}