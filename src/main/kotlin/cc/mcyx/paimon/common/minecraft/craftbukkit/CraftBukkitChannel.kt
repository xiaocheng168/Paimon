package cc.mcyx.paimon.common.minecraft.craftbukkit

import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayerManager
import cc.mcyx.paimon.common.plugin.Paimon
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

/**
 * 通信通道
 * @param paimon 插件实例
 * @param nameSpace 通道命名空间
 */
class CraftBukkitChannel(
    private val paimon: Paimon,
    private val nameSpace: String
) : PluginMessageListener {

    override fun onPluginMessageReceived(p0: String, p1: Player, p2: ByteArray) {
        messageEvent.invoke(
            p2,
            PaimonPlayerManager.getPaimonPlayer(p1),
            p0
        )
    }

    fun unregister(): CraftBukkitChannel {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(paimon, nameSpace, this)
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(paimon, nameSpace)
        return this
    }

    fun register(): CraftBukkitChannel {
        if (paimon.getServerId() > 1122) {
            if (nameSpace.split(":").size < 2) {
                throw RuntimeException("服务器 > 1.12.2,通道必须用 : 命名空间分割案例 test:test1")
            }
        }
        Bukkit.getMessenger().registerIncomingPluginChannel(paimon, nameSpace, this)
        Bukkit.getMessenger().registerOutgoingPluginChannel(paimon, nameSpace)
        return this
    }

    /**
     * 以服务器自身发送信息
     * @param message 数据
     */
    fun sendMessage(message: ByteArray): CraftBukkitChannel {
        Bukkit.getServer().sendPluginMessage(paimon, this.nameSpace, message)
        return this
    }

    /**
     * 以玩家身份发送信息
     * @param player 玩家
     * @param message 信息
     */
    fun sendMessageForPlayer(player: Player, message: ByteArray): CraftBukkitChannel {
        player.sendPluginMessage(paimon, this.nameSpace, message)
        return this
    }

    private lateinit var messageEvent: ((message: ByteArray, paimonPlayer: PaimonPlayer, nameSpace: String) -> Unit)
    fun messageEvent(e: ((message: ByteArray, paimonPlayer: PaimonPlayer, nameSpace: String) -> Unit)) = messageEvent
}