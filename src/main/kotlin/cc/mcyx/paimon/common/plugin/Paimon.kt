package cc.mcyx.paimon.common.plugin

import cc.mcyx.paimon.common.minecraft.craftbukkit.removePluginCommand
import cc.mcyx.paimon.common.util.PaimonClassLoader
import org.bukkit.plugin.java.JavaPlugin

/**
 * Paimon Plugin Kt 主类
 */
abstract class Paimon : JavaPlugin() {
    final override fun onLoad() {
        this.onLoaded()
    }

    val cl: ClassLoader = classLoader
    final override fun onEnable() {
        //加载插件本体
        val paimonClassLoader = PaimonClassLoader(this)
        paimonClassLoader.loadPlugin()
        this.onEnabled()
    }

    final override fun onDisable() {
        removePluginCommand(this)
        this.onDisabled()
    }

    /**
     * 插件初始化
     *
     */
    open fun onLoaded() {}

    /**
     * 插件加载完成
     */
    open fun onEnabled() {}

    /**
     * 插件卸载
     */
    open fun onDisabled() {}
}