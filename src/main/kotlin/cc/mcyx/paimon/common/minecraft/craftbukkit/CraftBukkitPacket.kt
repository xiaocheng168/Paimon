package cc.mcyx.paimon.common.minecraft.craftbukkit

import cc.mcyx.paimon.common.ui.PaimonUI
import cn.hutool.core.util.ClassUtil
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method

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

        //NMS Packet
        val nmsPacket = if (serverId < 1170) "net.minecraft.server.$nmsVersion" else "net.minecraft"

        val craftPlayer = asBukkitClass("CraftPlayer")
        val craftServer = asBukkitClass("CraftServer")
        val craftItemStack = asBukkitClass("CraftItemStack")


        val packet = asNMSPacketClass("Packet")
        val packetPlayOutOpenWindow = asNMSPacketClass("PacketPlayOutOpenWindow")
        val packetPlayOutCloseWindow = asNMSPacketClass("PacketPlayOutCloseWindow")
        val packetPlayInWindowClick = asNMSPacketClass("PacketPlayInWindowClick")
        val packetPlayInCloseWindow = asNMSPacketClass("PacketPlayInCloseWindow")
        val packetPlayOutSetSlot = asNMSPacketClass("PacketPlayOutSetSlot")
        val chatComponentText = asNMSPacketClass("ChatComponentText")
        val iChatBaseComponent = asNMSPacketClass("IChatBaseComponent")
        val itemStack = asNMSPacketClass("ItemStack")

        //这是一个1.14+=的类 低版本都没用
        val containers = if (serverId < 1140) Unit::class.java else asNMSPacketClass("Containers")

        /**
         * 获取NMS的类
         * 扫描路径 nms 包
         * @param className 类名
         */

        fun asNMSPacketClass(className: String): Class<*> {
            return classNameAsClass(nmsPacket, className)
        }

        /**
         *  包.类 转 Class<*>
         *   @param classes 包.类 绝对路径
         *   @return 返回Class<*>
         */
        fun asBukkitClass(classes: String): Class<*> {
            return classNameAsClass(craftBukkit, classes)
        }

        /**
         * 获取混淆类中的某个类型数据
         * @param c 哪个类
         * @param getObject 来源对象
         * @param type 获取的类型
         * @return 返回获取到的数据
         */
        fun getObject(c: Class<*>, getObject: Any, type: String): Any {
            //获取高版本的 NetworkManager
            for (declaredField in c.declaredFields) {
                if (declaredField.type.toString().endsWith(type)) {
                    declaredField.isAccessible = true
                    return declaredField.get(getObject)
                }
            }
            return Unit
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
        /**
         * 获取混淆类中的某个类型数据
         * @param getObject 来源对象
         * @param type 获取的类型
         * @return 返回获取到的数据
         */
        fun getObjectOrNUll(getObject: Any, type: String): Any? {
            //获取高版本的 NetworkManager
            for (declaredField in getObject.javaClass.declaredFields) {
                if (declaredField.type.toString().endsWith(type)) {
                    declaredField.isAccessible = true
                    return declaredField.get(getObject)
                }
            }
            return Unit
        }

        /**
         * 获取一个对象中的某个类型的所有字段
         * @param getObject 来源对象
         * @param type 获取类型
         * @return 返回获得到的对应的属性数据
         */
        fun getObjects(getObject: Any, type: Class<*>): MutableList<Any> {
            val objects: MutableList<Any> = mutableListOf()
            for (declaredField in getObject.javaClass.declaredFields) {
                declaredField.isAccessible = true
                if (declaredField.type == type) {
                    objects.add(declaredField.get(getObject))
                }
            }
            return objects
        }

        /**
         * 扫描某个包下的指定类 如果存在返回Class 反之返回 Unit
         * @param `package` 扫描的包路径 列如扫描nms玩家数据包 可以这样 net.minecraft
         * @param className 类名 Class Name
         * @return 返回对应的Class类 如果没用找到返回 Any(Object)
         */
        @Synchronized
        fun classNameAsClass(`package`: String, className: String): Class<*> {
            val clazzs = ClassUtil.scanPackage(`package`).filter {
                try {
                    it.name.endsWith(".$className")
                } catch (e: Exception) {
                    throw RuntimeException("无法找到对应类 类名 $className 查找包路径 $`package`")
                }
            }
            if (clazzs.isNotEmpty()) {
                return clazzs[0]
            }
            println("[Paimon!!!] 无法找到对应类 类名 $className 查找包路径 $`package`")
            return Unit::class.java
        }


        /**
         * 通过返回类型、参数来获取一个类中的方法
         * @param forClass 获得的对象
         * @param rClass 返回类型
         * @param pClass 方法参数类型
         * @return 方法
         */
        fun getClassMethod(forClass: Class<*>, rClass: Class<*>, vararg pClass: Class<*>): Method? {
            //遍历此类的所有方法
            for (method in forClass.declaredMethods) {
                val parameterTypes = method.parameterTypes
                //正确了几个
                var successSize = 0
                if (parameterTypes.size == pClass.size) {
                    for ((index, clazz) in pClass.withIndex()) {
                        if (parameterTypes[index] == clazz) successSize++
                    }
                    //循环完判断是否正确数量完整
                    if (parameterTypes.size == successSize) {
                        return method
                    }
                }
            }
            return null
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
                    aClass = asNMSPacketClass("IChatBaseComponent")
                    val a = aClass.getMethod("a", String::class.java)
                    return a.invoke(aClass, str)
                }
                aClass = if (serverId >= 1180) {
                    //>= 1.17
                    chatComponentText
                } else {
                    // < 1.17
                    chatComponentText
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
            //1.17+
            if (serverId >= 1170) {
                return if (paimonUIType == PaimonUI.PaimonUIType.ANVIL) {
                    packetPlayOutOpenWindow.getConstructor(
                        Int::class.java,
                        containers,
                        iChatBaseComponent,
                    ).newInstance(
                        nextContainerCounter,
                        containers.getDeclaredField(paimonUIType.v17p)
                            .get(containers),
                        getChatComponentText(head)
                    )
                } else packetPlayOutOpenWindow.getConstructor(
                    Int::class.java,
                    containers,
                    iChatBaseComponent,
                ).newInstance(
                    nextContainerCounter,
                    containers.getDeclaredField(paimonUIType.v17p)
                        .get(containers),
                    getChatComponentText(head),
                )
            }

            //1.14+
            if (serverId >= 1140) {
                return if (paimonUIType == PaimonUI.PaimonUIType.ANVIL) {
                    packetPlayOutOpenWindow.getConstructor(
                        Int::class.java,
                        containers,
                        iChatBaseComponent,
                        Int::class.java
                    ).newInstance(
                        nextContainerCounter,
                        containers.getDeclaredField(paimonUIType.v14p)
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
                    containers.getDeclaredField(paimonUIType.v14p)
                        .get(containers),
                    getChatComponentText(head),
                    size
                )
            }

            // <= 1.13
            return if (paimonUIType == PaimonUI.PaimonUIType.ANVIL) {
                packetPlayOutOpenWindow.getConstructor(
                    Int::class.java,
                    String::class.java,
                    iChatBaseComponent
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
                        slot,
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
    }
}