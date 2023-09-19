package cc.mcyx.paimon.common

import cc.mcyx.paimon.common.util.loadPlugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Paimon Plugin 主类
 */
open class Paimon : JavaPlugin() {
    val cl = classLoader
    final override fun onEnable() {
        loadPlugin(this)
        this.onEnabled()
    }

    open fun onEnabled() {}
}