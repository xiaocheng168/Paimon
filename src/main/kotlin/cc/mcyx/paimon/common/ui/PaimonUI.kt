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
                this.paimonUIType.type,
                this.head,
                this.paimonUIType.size
            )
        )
        this.isOpen = true
        //批量发送物品位置包
        for (value in buttons.values) {
            sendSetItemPacket(value.slot, value.itemStack)
        }

        paimonPlayer.packetListener { it ->
            //玩家点击某个地方与按钮
            if (it.packet::class.java == CraftBukkitPacket.asNMSClass("PacketPlayInWindowClick")) {
                val packetPlayInWindowClick = CraftBukkitPacket.asNMSClass("PacketPlayInWindowClick").cast(it.packet)
                packetPlayInWindowClick.javaClass.getDeclaredField("item").also {
                    it.isAccessible = true
                    //解析读取Item
                    CraftBukkitPacket.nmsItemToItemStack(
                        CraftBukkitPacket.itemStack.cast(
                            it.get(
                                packetPlayInWindowClick
                            )
                        )
                    ).apply {
                        val paimonUIClickEvent = PaimonUIClickEvent(
                            this@PaimonUI,
                            this,
                            it.javaClass.getDeclaredField("slot").apply { isAccessible = true }.get(it) as Int,
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
                }
            }
            //如果玩家关闭界面
            if (it.packet::class.java == CraftBukkitPacket.asNMSClass("PacketPlayInCloseWindow")) {
                if (CraftBukkitPacket.asNMSClass("PacketPlayInCloseWindow").getDeclaredField("id").apply {
                        isAccessible = true
                    }.get(it.packet) == this.gid) {
                    this.isOpen = false
                    //回调事件用于处理
                    this.closeEvent?.invoke(this)
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


    enum class PaimonUIType(val type: String, val size: Int) {
        CHEST_9("minecraft:chest", 9),
        CHEST_18("minecraft:chest", 18),
        CHEST_24("minecraft:chest", 24),
        CHEST_36("minecraft:chest", 36),
        CHEST_48("minecraft:chest", 48),
        CHEST_56("minecraft:chest", 56),
        HOPPER("minecraft:hopper", 5),
        ANVIL("minecraft:anvil", 3),
    }

}