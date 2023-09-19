package demo.listener

import cc.mcyx.paimon.common.listener.PaimonAutoListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent

class WC : PaimonAutoListener {
    @EventHandler
    fun qwq(playerChatEvent: AsyncPlayerChatEvent) {
        println(playerChatEvent.message)
    }
}