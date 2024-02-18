package cc.mcyx.paimon.common.minecraft.craftbukkit

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * 物品操作
 * @author zcc
 */
class CraftItemUtil {
    companion object {
        /**
         * 通过物品构建物品
         */
        @JvmStatic
        fun buildItem(
            itemStack: ItemStack, name: String, lore: List<String> = mutableListOf()
        ): ItemStack {
            return itemStack.clone().also {
                val itemMeta = it.itemMeta
                itemMeta.displayName = name
                itemMeta.lore = lore
                it.itemMeta = itemMeta
            }
        }

        /**
         * 通过物品类型构建物品
         */
        @JvmStatic
        fun buildItem(
            material: Material, name: String, lore: List<String> = mutableListOf()
        ): ItemStack {
            return ItemStack(material).also {
                val itemMeta = it.itemMeta
                itemMeta.displayName = name
                itemMeta.lore = lore
                it.itemMeta = itemMeta
            }
        }

        /**
         * Bukkit 物品转 NMS 物品
         * @param itemStack bk物品
         *
         * @return 返回nms物品
         */
        @JvmStatic
        fun bukkitItemAsNmsItem(itemStack: ItemStack): Any {
            return CraftBukkitPacket.bukkitItemToNMSItem(itemStack)
        }

        /**
         * NMS物品 转 Bukkit 物品
         * @param nmsItem NMS物品
         *
         * @return 返回 bk 物品
         */
        @JvmStatic
        fun nmsItemAsBukkitItem(nmsItem: Any): ItemStack {
            return CraftBukkitPacket.nmsItemToItemStack(nmsItem)
        }
    }
}