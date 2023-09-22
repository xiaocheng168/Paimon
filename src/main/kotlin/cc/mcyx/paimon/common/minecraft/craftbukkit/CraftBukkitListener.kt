package cc.mcyx.paimon.common.minecraft.craftbukkit

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.listener.PaimonAutoListener
import org.bukkit.Bukkit

//自动注册监听表
val autoRegisterListenerList: MutableList<PaimonAutoListener> = mutableListOf()

/**
 * 注册监听器
 * @param paimonAutoListener 监听器接口
 */
fun registerListener(paimonAutoListener: PaimonAutoListener) {
    paimonAutoListener.also {
        Bukkit.getPluginManager().registerEvents(it, PaimonPlugin.paimonPlugin)
        autoRegisterListenerList.add(it)
    }
}