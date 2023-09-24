package cc.mcyx.paimon.common.minecraft.craftbukkit.event

import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Bukkit 玩家数据包事件
 *
 * @param paimonPlayer 代理玩家
 * @param packet 数据包
 * @param isCancel 是否取消事件
 */
@Deprecated("wait...")
class PaimonPlayerInPacketEvent(val paimonPlayer: PaimonPlayer, val packet: Any, var isCancel: Boolean = false) :
    PlayerEvent(paimonPlayer.player),
    Cancellable {
    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()

    /*    @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }*/
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(p0: Boolean) {
        this.isCancel = p0
    }
}