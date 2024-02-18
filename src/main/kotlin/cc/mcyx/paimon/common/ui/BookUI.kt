package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

/**
 * 虚拟书本UI
 * @param player 玩家
 * @param pagesLine 书本里的每一行数据
 */
class BookUI(
    val player: Player,
    private vararg val pagesLine: BaseComponent = arrayOf()
) {
    companion object {
        @JvmStatic
        val PacketPlayOutOpenBook = CraftBukkitPacket.asNMSPacketClass("PacketPlayOutOpenBook")

        @JvmStatic
        val EnumHand = CraftBukkitPacket.asNMSPacketClass("EnumHand")
    }

    private val paimonPlayer = PaimonPlayer(player)

    fun open() {
        val itemStack = ItemStack(Material.WRITTEN_BOOK).also {
            val itemMeta = it.itemMeta as BookMeta
            itemMeta.author = ""
            itemMeta.title = ""
            itemMeta.spigot().setPages(pagesLine)
            it.itemMeta = itemMeta
        }
        //发送书物品
        setInventoryItem(player.inventory.heldItemSlot, itemStack)
        //打开书
        if (CraftBukkitPacket.serverId >= 1140) {
            val action =
                EnumHand.getDeclaredField(if (CraftBukkitPacket.serverId >= 1170) "a" else "MAIN_HAND").get(null)
            paimonPlayer.sendPacket(
                PacketPlayOutOpenBook.getConstructor(EnumHand).newInstance(action)
            )
        }

        player.updateInventory()
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