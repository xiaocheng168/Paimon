package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.listener.PaimonAutoListener
import cc.mcyx.paimon.common.minecraft.craftbukkit.registerListener
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class PaimonUI(private var inventoryType: InventoryType, private var title: String) : PaimonAutoListener {
    private var inventory: Inventory = Bukkit.createInventory(null, this.inventoryType, this.title)

    @EventHandler
    fun click(e: InventoryClickEvent) {
        if (e.view.title == this.inventory.title) {
            click?.invoke(e)
            val clickButton = e.currentItem
            //点击的物品不能是空
            if (clickButton != null && e.whoClicked is Player) {
                //从监听回调里寻找，回调函数 且回调此事件
                buttons[clickButton]?.invoke(e.whoClicked as Player, e.slot, clickButton)
            }
        }
    }

    @EventHandler
    fun closeGUI(e: InventoryCloseEvent) {
        if (e.view.title == this.inventory.title) {
            close?.invoke(e)
        }
    }

    @EventHandler
    fun closeGUI(e: InventoryOpenEvent) {
        if (e.view.title == this.inventory.title) {
            open?.invoke(e)
        }
    }

    private var click: ((InventoryClickEvent) -> Unit)? = null
    private var open: ((InventoryOpenEvent) -> Unit)? = null
    private var close: ((InventoryCloseEvent) -> Unit)? = null
    private var buttons: HashMap<ItemStack, ((player: Player, slot: Int, item: ItemStack) -> Unit)> = hashMapOf()

    /**
     * 界面点击事件
     * @param event 回调事件
     */
    fun click(event: ((InventoryClickEvent) -> Unit)): PaimonUI {
        this.click = event
        return this
    }

    /**
     * 界面点击事件
     * @param event 回调事件
     */
    fun open(event: ((InventoryOpenEvent) -> Unit)): PaimonUI {
        this.open = event
        return this
    }

    /**
     * 界面点击事件
     * @param event 回调事件
     */
    fun close(event: ((InventoryCloseEvent) -> Unit)): PaimonUI {
        this.close = event
        return this
    }

    /**
     * 设置按钮图标
     * @param slot 位置
     * @param itemStack 物品
     * @return 返回该界面对象
     */
    fun setButton(
        slot: Int,
        itemStack: ItemStack,
        buttonClick: ((player: Player, slot: Int, item: ItemStack) -> Unit)? = null
    ): PaimonUI {
        inventory.setItem(slot, itemStack)
        //将此会函数加入HasMap调用时查找使用
        if (buttonClick != null) {
            buttons[itemStack] = buttonClick
        }
        return this
    }

    /**
     * 给一个玩家打开该界面
     * @param player 玩家
     * @return 返回该界面对象
     */
    fun open(player: Player): PaimonUI {
        player.openInventory(this.inventory)
        registerListener(this)
        return this
    }
}