package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import cc.mcyx.paimon.common.ui.event.PaimonUIClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class PaimonUI(paimonUIType: PaimonUIType, head: String = "空空如也") {
    var paimonPlayer: PaimonPlayer? = null
    private var gid = 0
    private val buttons: HashMap<ItemStack, ButtonInfo> = hashMapOf()

    var head: String
    var paimonUIType: PaimonUIType

    var isOpen: Boolean = false

    init {
        this.head = head
        this.paimonUIType = paimonUIType
    }

    fun open(paimonPlayer: PaimonPlayer): PaimonUI {

        this.paimonPlayer = paimonPlayer
        this.gid = paimonPlayer.nextContainerCounter()

        //打开界面
        paimonPlayer.sendPacket(
            CraftBukkitPacket.createGUIPacket(
                this.gid,
                this.paimonUIType,
                this.head,
                this.paimonUIType.size
            )
        )
        this.isOpen = true
        //批量发送物品位置包
        for (value in buttons.values) {
            sendSetItemPacket(value.slot, value.itemStack)
        }

        paimonPlayer.packetListener {
            //玩家点击某个地方与按钮
            if (it.packet::class.java == CraftBukkitPacket.packetPlayInWindowClick) {
                val packetPlayInWindowClick = CraftBukkitPacket.packetPlayInWindowClick.cast(it.packet)
                try {

                    val guiObject = CraftBukkitPacket.getObjects(packetPlayInWindowClick, Int::class.java)
                    guiObject.removeAt(0)

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

                    //解析读取Item 如果是空就不解析与触发！
                    val itemStack =
                        CraftBukkitPacket.getObject(packetPlayInWindowClick, "ItemStack") ?: return@packetListener

                    CraftBukkitPacket.nmsItemToItemStack(
                        CraftBukkitPacket.itemStack.cast(itemStack)
                    ).apply {
                        val paimonUIClickEvent = PaimonUIClickEvent(
                            this@PaimonUI,
                            paimonPlayer,
                            this,
                            clickSlot,
                            false
                        )
                        clickEvent?.invoke(paimonUIClickEvent)!!
                        //如果取消了将刷新界面
                        if (paimonUIClickEvent.isCancel) {
                            this@PaimonUI.update()
                        }
                        //触发按钮独立事件
                        buttons[this]?.callback?.invoke(paimonUIClickEvent)
                        //如果取消了将刷新界面
                        if (paimonUIClickEvent.isCancel) {
                            this@PaimonUI.update()
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }
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

    //点击事件 点击事件为界面所有地方点击反馈 它的优先级大于 > itemClickEvent
    private var clickEvent: ((PaimonUIClickEvent) -> Unit)? = null

    //按钮点击回调
    private var itemClickEvent: ((PaimonUIClickEvent) -> Unit)? = null

    fun closeEvent(e: (PaimonUI) -> Unit) {
        this.closeEvent = e
    }

    fun clickEvent(e: (PaimonUIClickEvent) -> Unit) {
        this.clickEvent = e
    }

    fun setItem(slot: Int, itemStack: ItemStack, itemClick: ((PaimonUIClickEvent) -> Unit)? = null): PaimonUI {
        //设置回调函数
        this.itemClickEvent = itemClick
        //设置该物品的点击事件
        buttons[itemStack] = ButtonInfo(itemStack, slot, itemClick)
        //发送设置物品包
        sendSetItemPacket(slot, itemStack)
        return this
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
                min(slot, this.paimonUIType.size - 1),
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
        CHEST_18("minecraft:chest", 18, "GENERIC_9X2", "a"),
        CHEST_24("minecraft:chest", 24, "GENERIC_9X3", "a"),
        CHEST_36("minecraft:chest", 36, "GENERIC_9X4", "a"),
        CHEST_48("minecraft:chest", 48, "GENERIC_9X5", "a"),
        CHEST_56("minecraft:chest", 56, "GENERIC_9X6", "a"),
        HOPPER("minecraft:hopper", 5, "HOPPER", "p"),
        ANVIL("minecraft:anvil", 2, "ANVIL", "h"),
    }

}