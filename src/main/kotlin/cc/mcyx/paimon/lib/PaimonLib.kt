package cc.mcyx.paimon.lib

import cc.mcyx.paimon.common.PaimonPlugin
import org.bukkit.Bukkit

class PaimonLib : PaimonPlugin() {
    override fun onEnabled() {
        logger.info("PaimonLib Enable")
        logger.info("Server ${Bukkit.getVersion()}")
        logger.info("Version ${getPluginYmlConfig().getString("version")}")
    }
}