package cc.mcyx.paimon.common.nms

import org.bukkit.Bukkit

/**
 * NMS类
 */
abstract class NMSPacket {
    companion object {
        private val craftBukkit: String = Bukkit.getServer().javaClass.`package`.name
        val craftServer = "${craftBukkit}.CraftServer"
    }
}