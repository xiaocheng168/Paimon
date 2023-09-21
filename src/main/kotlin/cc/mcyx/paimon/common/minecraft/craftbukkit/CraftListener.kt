package cc.mcyx.paimon.common.minecraft.craftbukkit

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.listener.PaimonAutoListener
import org.bukkit.Bukkit

/**
 * 注册监听器
 * @param paimonAutoListener 监听器接口
 */
fun registerListener(paimonAutoListener: PaimonAutoListener) {
    Bukkit.getPluginManager().registerEvents(paimonAutoListener, PaimonPlugin.paimonPlugin)
}