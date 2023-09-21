package cc.mcyx.paimon.common.minecraft.craftbukkit

import org.bukkit.Bukkit

/**
 * NMS类
 */
abstract class CraftBukkitPacket {
    companion object {
        private val craftBukkit: String = Bukkit.getServer().javaClass.`package`.name
        val craftServer = "$craftBukkit.CraftServer"
    }
}