package demo.listener

import cc.mcyx.paimon.common.listener.PaimonAutoListener
import cc.mcyx.paimon.common.util.info
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class PlayerGame : PaimonAutoListener {
    @EventHandler
    fun join(playerJoinEvent: PlayerJoinEvent) {
        info(playerJoinEvent.player)
    }
}