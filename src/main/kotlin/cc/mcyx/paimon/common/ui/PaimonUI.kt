package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import cc.mcyx.paimon.common.ui.event.PaimonUIClickEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 虚拟UI类
 * @param paimonUIType UI类型
 * @param head UI标题
 */
open class PaimonUI(paimonUIType: PaimonUIType, head: String = "空空如也") {
    var paimonPlayer: PaimonPlayer? = null
    var gid = 0
    val buttons: HashMap<Int, ButtonInfo> = hashMapOf()

    var head: String
    var paimonUIType: PaimonUIType

    var isOpen: Boolean = false

    init {
        this.head = head
        this.paimonUIType = paimonUIType
    }

    /**
     * 关闭用户目前正在打开的界面
     * @param paimonPlayer 被关闭的玩家
     * @return 返回关闭的界面
     */
    fun close(paimonPlayer: PaimonPlayer): PaimonUI {
        paimonPlayer.closeUI(this.gid)
        return this
    }

    /**
     * 给一个代理玩家打开UI界面
     * @param paimonPlayer 代理玩家
     * @param isRender 是否渲染 buttons 里的物品
     * @return 返回界面类
     */
    fun open(paimonPlayer: PaimonPlayer, isRender: Boolean = true): PaimonUI {

        this.paimonPlayer = paimonPlayer
        this.gid = paimonPlayer.nextContainerCounter()

        //打开界面
        paimonPlayer.sendPacket(
            CraftBukkitPacket.createGUIPacket(
                this.gid,
                this.paimonUIType,
                this.head.replace("&", "§"),
                this.paimonUIType.size
            )
        )
        this.isOpen = true
        //渲染 buttons 里的物品 批量发送物品位置包
        if (isRender) for (value in buttons.values) {
            sendSetItemPacket(value.slot, value.itemStack)
        }

        paimonPlayer.packetListener {
            //玩家点击某个地方与按钮
            if (it.packet::class.java == CraftBukkitPacket.packetPlayInWindowClick) {
                val packetPlayInWindowClick = CraftBukkitPacket.packetPlayInWindowClick.cast(it.packet)
                val guiObject = CraftBukkitPacket.getObjects(packetPlayInWindowClick, Int::class.java)
                //只删除高版本的
                if (CraftBukkitPacket.serverId >= 1170) guiObject.removeAt(0)
                //判断点击的界面是否为本GUI界面
                if (guiObject[0] != this.gid) return@packetListener

                //获取点击的物品位置
                val clickSlot = if (CraftBukkitPacket.serverId > 1170)
                    CraftBukkitPacket.getObjects(
                        packetPlayInWindowClick,
                        Int::class.java
                    ).asReversed()[1] as Int
                else
                    CraftBukkitPacket.getObjects(
                        packetPlayInWindowClick,
                        Int::class.java
                    )[1] as Int

                //解析读取Item，如果出现错误将返回Item.AIR
                val itemStack: ItemStack = try {
                    CraftBukkitPacket.nmsItemToItemStack(
                        CraftBukkitPacket.itemStack.cast(
                            CraftBukkitPacket.getObject(
                                packetPlayInWindowClick,
                                "ItemStack"
                            )
                        )
                    )
                } catch (e: Exception) {
                    ItemStack(Material.AIR)
                }
                //构建事件
                val paimonUIClickEvent = PaimonUIClickEvent(
                    this,
                    paimonPlayer,
                    itemStack,
                    clickSlot,
                    false
                )
                //传递点击事件
                try {
                    clickEvent?.invoke(paimonUIClickEvent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //传递点击指定物品事件
                try {
                    buttons[clickSlot]?.callback?.invoke(paimonUIClickEvent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //如果取消了将刷新界面
                if (paimonUIClickEvent.isCancel) {
                    this.update()
                }
            }

            //如果玩家关闭界面
            if (it.packet::class.java == CraftBukkitPacket.packetPlayInCloseWindow) {
                if (CraftBukkitPacket.getObject(it.packet, "int") == this.gid) {
                    this.isOpen = false
                    //回调事件用于处理
                    this.closeEvent?.invoke(this)
                }
            }

        }
        this.openEvent?.invoke(this) //玩家打开界面
        return this
    }

    /**
     * 更新交互界面
     * 界面必须处于打开界面才可继续更新交互界面
     */
    fun update() {
        if (isOpen)
            if (this.paimonPlayer != null) {
                //更新玩家背包
                paimonPlayer?.player?.updateInventory()
                //更新玩家界面
                this.open(this.paimonPlayer!!)
            } else throw RuntimeException("the gui not init.. please invoke PaimonUI.open()")
    }

    //关闭界面事件
    private var closeEvent: ((PaimonUI) -> Unit)? = null

    //打开界面
    private var openEvent: ((PaimonUI) -> Unit)? = null

    //点击事件 点击事件为界面所有地方点击反馈 它的优先级大于 > itemClickEvent
    private var clickEvent: ((PaimonUIClickEvent) -> Unit)? = null

    //按钮点击回调
    private var itemClickEvent: ((PaimonUIClickEvent) -> Unit)? = null

    fun closeEvent(e: (PaimonUI) -> Unit) {
        this.closeEvent = e
    }

    fun openEvent(e: (PaimonUI) -> Unit) {
        this.openEvent = e
    }

    fun clickEvent(e: (PaimonUIClickEvent) -> Unit) {
        this.clickEvent = e
    }

    fun setItem(slot: Int, itemStack: ItemStack, itemClick: ((PaimonUIClickEvent) -> Unit)? = null): PaimonUI {
        //设置回调函数
        this.itemClickEvent = itemClick
        //设置该物品的点击事件
        buttons[slot] = ButtonInfo(itemStack, slot, itemClick)
        //发送设置物品包
        sendSetItemPacket(slot, itemStack)
        return this
    }

    /**
     * 清空界面
     */
    fun clear() {
        for (i in this.paimonUIType.size downTo 0) {
            sendSetItemPacket(i, ItemStack(Material.AIR))
        }
    }

    /**
     * 添加物品，返回再哪一个
     */
    fun addItem(itemStack: ItemStack) {
        for (i in 0..this.paimonUIType.size) {
            if (!buttons.containsKey(i)) {
                setItem(i, itemStack)
                return
            }
        }
    }

    /**
     * 发送物品按钮包
     * @param itemStack 物品堆
     * @param slot 位置
     */
    private fun sendSetItemPacket(slot: Int, itemStack: ItemStack) {
        paimonPlayer?.sendPacket(
            CraftBukkitPacket.createSlotItemPacket(
                gid,
                slot,
                itemStack
            )
        )
    }


    /**
     * 界面编号
     * @param type <= 1.13 字符串
     * @param size 尺寸
     * @param v14p >= 1.14 版本
     * @param v17p >= 1.17
     */
    enum class PaimonUIType(val type: String, val size: Int, val v14p: String, val v17p: String) {
        CHEST_9("minecraft:chest", 9, "GENERIC_9X1", "a"),
        CHEST_18("minecraft:chest", 18, "GENERIC_9X2", "b"),
        CHEST_24("minecraft:chest", 24, "GENERIC_9X3", "c"),
        CHEST_36("minecraft:chest", 36, "GENERIC_9X4", "d"),
        CHEST_48("minecraft:chest", 48, "GENERIC_9X5", "e"),
        CHEST_56("minecraft:chest", 56, "GENERIC_9X6", "f"),
        HOPPER("minecraft:hopper", 5, "HOPPER", "p"),
        ANVIL("minecraft:anvil", 2, "ANVIL", "h"),
        DISPENSER("minecraft:dispenser", 9, "DISPENSER", "g"),
        FURNACE("minecraft:furnace", 3, "FURNACE", "n"),
        BREWING_STAND("minecraft:brewing_stand", 4, "BREWING_STAND", "k"),
    }

    /**
     * 打开一个PaimonUI界面
     * @param player Bukkit 玩家
     */
    fun open(player: Player): PaimonUI {
        return this.open(PaimonPlayer(player))
    }
}