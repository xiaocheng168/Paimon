package cc.mcyx.paimon.common.minecraft.network


import org.bukkit.entity.Player

abstract class PaimonPlayerManager {

    companion object {
        //代理玩家表
        private val paimonPlayers: HashMap<Player, PaimonPlayer> = hashMapOf()

        /**
         * 添加代理玩家
         * @param player BukkitPlayer 玩家对象
         * @return 返回代理玩家
         */
        fun addPaimonPlayer(player: Player): PaimonPlayer {
            return PaimonPlayer(player).also {
                paimonPlayers[player] = it
            }
        }

        /**
         * 获取代理玩家
         * @param player BukkitPlayer 玩家对象
         * @return 代理玩家
         */
        fun getPaimonPlayer(player: Player): PaimonPlayer {
            return if (!paimonPlayers.contains(player)) {
                addPaimonPlayer(player)
            } else paimonPlayers[player]!!
        }
    }
}