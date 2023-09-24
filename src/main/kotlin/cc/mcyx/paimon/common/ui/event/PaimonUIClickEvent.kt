package cc.mcyx.paimon.common.ui.event

import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import cc.mcyx.paimon.common.ui.PaimonUI
import org.bukkit.inventory.ItemStack

/**
 * 是否取消事件
 * @param paimonUI 界面对象
 * @param itemStack 按钮物品
 * @param slot 按钮位置
 * @param isCancel 是否取消
 */
class PaimonUIClickEvent(
    val paimonUI: PaimonUI,
    val paimonPlayer: PaimonPlayer,
    val itemStack: ItemStack,
    val slot: Int,
    var isCancel: Boolean
)