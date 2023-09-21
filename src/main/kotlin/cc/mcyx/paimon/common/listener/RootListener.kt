package cc.mcyx.paimon.common.listener

import cc.mcyx.paimon.common.minecraft.network.ProxyPlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class RootListener : PaimonAutoListener {
    @EventHandler
    fun joinEvent(e: PlayerJoinEvent) {
        ProxyPlayerManager.addPaimonPlayer(e.player)
    }
}