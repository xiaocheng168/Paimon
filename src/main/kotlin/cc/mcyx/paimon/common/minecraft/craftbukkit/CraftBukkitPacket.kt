package cc.mcyx.paimon.common.minecraft.craftbukkit

import cc.mcyx.paimon.common.ui.PaimonUI
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

/**
 * NMS类
 * 这里的类不能出现异常，否则会影响其他地方加载它！
 */
abstract class CraftBukkitPacket {
    companion object {

        //解析数字版本号
        private val versionSplit = Bukkit.getBukkitVersion().split("-")
        val serverId: Int = versionSplit[0].replace(".", "").toInt()

        //Bukkit Packet
        private val craftBukkit: String = Bukkit.getServer().javaClass.`package`.name
        private val nmsVersion: String = craftBukkit.replace("org.bukkit.craftbukkit.", "")
        val craftPlayer = asBukkitClass("entity.CraftPlayer")
        val craftItemStack = asBukkitClass("inventory.CraftItemStack")
        val craftServer = asBukkitClass("CraftServer")

        //NMS Packet
        val nmsPacket = if (serverId < 1170) "net.minecraft.server.$nmsVersion" else "net.minecraft"
        val packet = asNMSClass("Packet")
        val packetPlayOutOpenWindow = asNMSGamePacketClass("PacketPlayOutOpenWindow")
        val packetPlayInWindowClick = asNMSGamePacketClass("PacketPlayInWindowClick")
        val packetPlayOutSetSlot = asNMSGamePacketClass("PacketPlayOutSetSlot")
        val chatComponentText = asNMSClass("ChatComponentText")
        val iChatBaseComponent =
            if (serverId < 1170) asNMSClass("IChatBaseComponent") else asNMSClass("network.chat.IChatBaseComponent")
        val itemStack = if (serverId < 1170) asNMSClass("ItemStack") else asNMSClass("world.item.ItemStack")

        //这是一个1.14+=的类 低版本都没用
        val containers = if (serverId < 1170) asNMSClass("Containers") else asNMSClass("world.inventory.Containers")

        /**
        //         * 包.类 转 Class<*>
         *   @param classes 包.类 绝对路径
         *   @return 返回Class<*>
         */
        fun asNMSClass(classes: String): Class<*> {
            return try {
                //如果报错了就返回Nothing类
                Class.forName("$nmsPacket.$classes")
            } catch (e: Exception) {
                Unit::class.java
            }
        }

        fun asNMSGamePacketClass(packet: String): Class<*> {
            return if (serverId >= 1170) return asNMSClass("network.protocol.game.$packet") else this.asNMSClass(packet)
        }

        fun asNMSPacketClass(packet: String): Class<*> {
            return if (serverId >= 1170) return asNMSClass("network.protocol.$packet") else this.asNMSClass(packet)
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
         * 通过反射获取文本通信信息类
         *
         * @param str 通信内容
         * @return 返回对应版本的 ChatComponentText
         */
        fun getChatComponentText(str: String?): Any {
            return try {
                val aClass: Class<*>
                if (serverId >= 1190) {
                    //>= 1.19
                    aClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent")
                    val a = aClass.getMethod("a", String::class.java)
                    return a.invoke(aClass, str)
                }
                aClass = if (serverId >= 1180) {
                    //>= 1.17
                    Class.forName("net.minecraft.network.chat.ChatComponentText")
                } else {
                    // < 1.17
                    asNMSClass("ChatComponentText")
                }
                val constructor = aClass.getConstructor(String::class.java)
                constructor.newInstance(str)
            } catch (e: java.lang.Exception) {
                throw RuntimeException(e)
            }
        }


        /**
         * 创建一个GUI界面
         * @param nextContainerCounter 界面编码
         * @param head UI头标题
         * @param size 大小
         * @return 封装好的Packet数据包
         */
        fun createGUIPacket(
            nextContainerCounter: Int,
            paimonUIType: PaimonUI.PaimonUIType,
            head: String,
            size: Int = 9
        ): Any {

            if (containers != Unit.javaClass) {
                return if (paimonUIType == PaimonUI.PaimonUIType.ANVIL) {
                    packetPlayOutOpenWindow.getConstructor(
                        Int::class.java,
                        containers,
                        iChatBaseComponent,
                    ).newInstance(
                        nextContainerCounter,
                        containers.getDeclaredField(if (serverId >= 1170) paimonUIType.v17p else paimonUIType.v14p)
                            .get(containers),
                        getChatComponentText(head)
                    )
                } else packetPlayOutOpenWindow.getConstructor(
                    Int::class.java,
                    containers,
                    iChatBaseComponent,
                    Int::class.java
                ).newInstance(
                    nextContainerCounter,
                    containers.getDeclaredField(if (serverId >= 1170) paimonUIType.v17p else paimonUIType.v14p)
                        .get(containers),
                    getChatComponentText(head),
                    size
                )

            }

            //anvil特殊处理
            return if (paimonUIType == PaimonUI.PaimonUIType.ANVIL) {
                packetPlayOutOpenWindow.getConstructor(
                    Int::class.java,
                    String::class.java,
                    iChatBaseComponent,
                ).newInstance(nextContainerCounter, paimonUIType.type, getChatComponentText(head))
            } else {
                packetPlayOutOpenWindow.getConstructor(
                    Int::class.java,
                    String::class.java,
                    iChatBaseComponent,
                    Int::class.java
                ).newInstance(nextContainerCounter, paimonUIType.type, getChatComponentText(head), size)
            }
        }

        /**
         * 设置界面的物品
         * @param slot 位置
         * @param itemStack Bukkit 物品
         * @return 封装好的Packet数据包
         */

        fun createSlotItemPacket(nextContainerCounter: Int, slot: Int, itemStack: ItemStack): Any {
            if (serverId >= 1180) {
                return packetPlayOutSetSlot.getConstructor(
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    this.itemStack
                )
                    .newInstance(
                        nextContainerCounter,
                        slot,
                        0,
                        bukkitItemToNMSItem(itemStack)
                    )
            }
            return packetPlayOutSetSlot.getConstructor(Int::class.java, Int::class.java, this.itemStack)
                .newInstance(
                    nextContainerCounter,
                    slot,
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


        /**
         * 获取混淆类中的某个类型数据
         * @param getObject 来源对象
         * @param type 获取的类型
         * @return 返回获取到的数据
         */
        fun getObject(getObject: Any, type: String): Any {
            //获取高版本的 NetworkManager
            for (declaredField in getObject.javaClass.declaredFields) {
                if (declaredField.type.toString().endsWith(type)) {
                    declaredField.isAccessible = true
                    return declaredField.get(getObject)
                }
            }
            return Unit
        }
    }
}