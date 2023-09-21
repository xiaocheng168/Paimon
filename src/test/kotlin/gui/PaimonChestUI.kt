package gui

import PaimonUI
import org.bukkit.Bukkit

@Deprecated("废弃方法")
class PaimonChestUI(title: String, size: ChestSize) : PaimonUI() {

    init {
        this.inventory = Bukkit.createInventory(null, size.slots, title)
    }

}

enum class ChestSize(val slots: Int) {
    Line1(9),
    Line2(9 * 2),
    Line3(9 * 3),
    Line4(9 * 4),
    Line5(9 * 5),
    Line6(9 * 6),
}