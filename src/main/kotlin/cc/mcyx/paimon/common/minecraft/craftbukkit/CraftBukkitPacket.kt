package cc.mcyx.paimon.common.minecraft.craftbukkit

import cc.mcyx.paimon.common.ui.PaimonUI
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

/**
 * NMS类
 */
abstract class CraftBukkitPacket {
    companion object {

        //Bukkit Packet
        private val craftBukkit: String = Bukkit.getServer().javaClass.`package`.name
        private val nmsVersion: String = craftBukkit.replace("org.bukkit.craftbukkit.", "")
        val craftPlayer = asBukkitClass("entity.CraftPlayer")
        val craftItemStack = asBukkitClass("inventory.CraftItemStack")
        val craftServer = asBukkitClass("CraftServer")


        //NMS Packet
        val nmsPacket = "net.minecraft.server.$nmsVersion"
        val packet = asNMSClass("Packet")
        val packetPlayOutOpenWindow = asNMSClass("PacketPlayOutOpenWindow")
        val packetPlayInWindowClick = asNMSClass("PacketPlayInWindowClick")
        val packetPlayOutSetSlot = asNMSClass("PacketPlayOutSetSlot")
        val chatComponentText = asNMSClass("ChatComponentText")
        val iChatBaseComponent = asNMSClass("IChatBaseComponent")
        val itemStack = asNMSClass("ItemStack")

        /**
         * 包.类 转 Class<*>
         *   @param classes 包.类 绝对路径
         *   @return 返回Class<*>
         */
        fun asNMSClass(classes: String): Class<*> {
            return Class.forName("$nmsPacket.$classes")
        }

        /**
         * 包.类 转 Class<*>
         *   @param classes 包.类 绝对路径
         *   @return 返回Class<*>
         */
        fun asBukkitClass(classes: String): Class<*> {
            return Class.forName("$craftBukkit.$classes")
        }

        /**
         * 获取NMS的 ChatMessage
         * @param msg 转换内容
         * @return 返回ChatMessage
         */
        fun stringAsChatComponentText(msg: String): Any {
            return chatComponentText.getConstructor(String::class.java).newInstance(msg)
        }


        /**
         * 创建一个GUI界面
         * @param nextContainerCounter 界面编码
         * @param head UI头标题
         * @param size 大小
         * @return 封装好的Packet数据包
         */
        fun createGUIPacket(nextContainerCounter: Int, minecraftUIName: String, head: String, size: Int = 9): Any {

            //anvil特殊处理
            if (minecraftUIName == PaimonUI.PaimonUIType.ANVIL.type) {
                return packetPlayOutOpenWindow.getConstructor(
                    Int::class.java,
                    String::class.java,
                    iChatBaseComponent,
                ).newInstance(nextContainerCounter, minecraftUIName, stringAsChatComponentText(head))
            }


            return packetPlayOutOpenWindow.getConstructor(
                Int::class.java,
                String::class.java,
                iChatBaseComponent,
                Int::class.java
            ).newInstance(nextContainerCounter, minecraftUIName, stringAsChatComponentText(head), size)
        }

        /**
         * 设置界面的物品
         * @param slot 位置
         * @param itemStack Bukkit 物品
         * @return 封装好的Packet数据包
         */

        fun createSlotItemPacket(nextContainerCounter: Int, slot: Int, itemStack: ItemStack): Any {
            return packetPlayOutSetSlot.getConstructor(Int::class.java, Int::class.java, this.itemStack)
                .newInstance(
                    nextContainerCounter, slot,
                    bukkitItemToNMSItem(itemStack)
                )
        }

        /**
         * @param nmsItem NMS的物品ItemStack
         * @return 返回Bukkit ItemStack
         */
        fun nmsItemToItemStack(nmsItem: Any): ItemStack {
            return craftItemStack.getDeclaredMethod("asBukkitCopy", itemStack)
                .invoke(craftItemStack, nmsItem) as ItemStack
        }

        /**
         * @param bkItem Bukkit ItemStack
         * @return 返回 NMS ItemStack
         */
        fun bukkitItemToNMSItem(bkItem: ItemStack): Any {
            return craftItemStack.getDeclaredMethod("asNMSCopy", ItemStack::class.java)
                .invoke(craftItemStack, bkItem)
        }
    }
}