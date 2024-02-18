package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BookUI(val player: Player) {
    companion object {
        @JvmStatic
        val PacketPlayOutOpenBook = CraftBukkitPacket.asNMSPacketClass("PacketPlayOutOpenBook")

        @JvmStatic
        val EnumHand = CraftBukkitPacket.asNMSPacketClass("EnumHand")
    }

    private val paimonPlayer = PaimonPlayer(player)

    fun open() {
        if (CraftBukkitPacket.serverId >= 1140) {
            val action =
                EnumHand.getDeclaredField(if (CraftBukkitPacket.serverId >= 1170) "a" else "MAIN_HAND").get(null)
            paimonPlayer.sendPacket(
                PacketPlayOutOpenBook.getConstructor(EnumHand).newInstance(action)
            )
            setInventoryItem(player.inventory.heldItemSlot, ItemStack(Material.APPLE))
        }
    }

    private fun setInventoryItem(slot: Int, itemStack: ItemStack) {
        paimonPlayer.sendPacket(
            CraftBukkitPacket.createSlotItemPacket(
                -2,
                slot,
                itemStack
            )
        )
    }
}