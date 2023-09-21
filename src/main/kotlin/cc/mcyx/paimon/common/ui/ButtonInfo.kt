package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.ui.event.PaimonUIClickEvent
import org.bukkit.inventory.ItemStack

/**
 * 按钮信息
 * @param itemStack 点击的物品
 * @param slot 点击物品的位置
 * @param callback 事件回调
 */
class ButtonInfo(val itemStack: ItemStack, val slot: Int, val callback: ((PaimonUIClickEvent) -> Unit)?)