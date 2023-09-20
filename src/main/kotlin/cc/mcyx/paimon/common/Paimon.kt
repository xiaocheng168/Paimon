package cc.mcyx.paimon.common

import cc.mcyx.paimon.common.nms.removePluginCommand
import cc.mcyx.paimon.common.util.loadPlugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Paimon Plugin 主类
 */
open class Paimon : JavaPlugin() {
    val cl: ClassLoader = classLoader
    final override fun onEnable() {
        loadPlugin(this)
        this.onEnabled()
    }

    final override fun onDisable() {
        removePluginCommand(this)
        this.onDisabled()
    }


    /**
     * 插件加载完成
     */
    open fun onEnabled() {}

    /**
     * 插件卸载
     */
    open fun onDisabled() {}
}