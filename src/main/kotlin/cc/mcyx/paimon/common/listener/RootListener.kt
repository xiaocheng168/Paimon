package cc.mcyx.paimon.common.listener

import cc.mcyx.paimon.common.minecraft.network.PaimonPlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

class RootListener : PaimonAutoListener {
    @EventHandler(
        priority = EventPriority.HIGH,
        ignoreCancelled = true
    )
    fun joinEvent(e: PlayerJoinEvent) {
        try {
            PaimonPlayerManager.addPaimonPlayer(e.player)
        } catch (_: Throwable) {

        }
    }
}