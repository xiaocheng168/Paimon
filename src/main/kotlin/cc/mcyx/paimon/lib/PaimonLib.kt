package cc.mcyx.paimon.lib

import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.ui.PaimonUI
import cc.mcyx.paimon.support.plugin.BukkitBoot
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class PaimonLib : BukkitBoot() {
    override fun onEnabled() {
        logger.info("PaimonLib Enable")
        logger.info("Server ${Bukkit.getVersion()}")
        logger.info("Version ${getPluginYmlConfig().getString("version")}")

        PaimonCommand(this, "awa").paimonExec { sender, _, _ ->
            PaimonUI(PaimonUI.PaimonUIType.CHEST_56, "AWA").also {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        it.addItem(ItemStack(Material.APPLE))
                    }
                }, 10, 10)
                it.open(sender as Player)
            }
            return@paimonExec true
        }.register()
    }
}