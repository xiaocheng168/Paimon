package cc.mcyx.paimon.common.minecraft.network

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.ui.PaimonUI
import cc.mcyx.paimon.common.ui.Sign
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.entity.Player

/**
 * 代理玩家 Bukkit 代理玩家
 * @param player 玩家类
 */
class PaimonPlayer(val player: Player) {

    //EntityPlayer 对象
    val entityPlayer = CraftBukkitPacket.craftPlayer.getDeclaredMethod("getHandle").invoke(player)

    //PlayerConnection 玩家连接
    val connection = if (PaimonPlugin.isForge())
        CraftBukkitPacket.getObject(
            CraftBukkitPacket.getObject(
                entityPlayer,
                "NetHandlerPlayServer"
            ), "NetworkDispatcher"
        )
    else
        CraftBukkitPacket.getObject(entityPlayer, "PlayerConnection")

    //Network 网络类
    val network = if (PaimonPlugin.isForge())
        CraftBukkitPacket.getObject(
            connection,
            "NetworkManager"
        )
    else
        CraftBukkitPacket.getObject(connection, "NetworkManager")

    //NIO Channel通道对象
    val channel = CraftBukkitPacket.getObject(network, "Channel")

    //发包方法
    val sendPacketMethod =
        CraftBukkitPacket.getClassMethod(connection::class.java, Void::class.java, CraftBukkitPacket.packet)

    //数据包处理对象
    private val packetHandler = object : ChannelDuplexHandler() {
        override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
            //数据包不能为null 且是否拦截数据包
            if (msg != null) {
                val paimonPacket = PaimonPacket(msg, false)
                packetListener?.invoke(paimonPacket)
                //是否不拦截数据包
                if (!paimonPacket.isCancel) super.channelRead(ctx, msg)
            }
        }

        override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
            //数据包不能为null 且是否拦截数据包
            if (msg != null) {
                val paimonPacket = PaimonPacket(msg, false)
                packetListener?.invoke(paimonPacket)
                //是否不拦截数据包
                if (!paimonPacket.isCancel) super.write(ctx, msg, promise)
            }
        }
    }

    init {
        //代理玩家初始化
        try {
            //尝试删除该玩家的通道
            (channel as Channel).pipeline().remove(player.name)
            //报错代表不存在将添加通道用于监听收到的数据包Packet<*>
            channel.pipeline().addBefore("packet_handler", player.name, packetHandler)
        } catch (e: Exception) {
            //报错代表不存在将添加通道用于监听收到的数据包Packet<*>
            (channel as Channel).pipeline().addBefore("packet_handler", player.name, packetHandler)
        }
    }

    fun nextContainerCounter(): Int {
        return entityPlayer.javaClass.getDeclaredMethod("nextContainerCounter").invoke(entityPlayer) as Int
    }


    //数据包发送到客户端
    private var packetSendListener: ((PaimonPacket) -> Unit)? = null

    /**
     * 数据包回调函数
     * @param packetSendListener
     * @return 数据对象本身
     */
    fun packetSendListener(packetSendListener: ((PaimonPacket) -> Unit)?): PaimonPlayer {
        this.packetSendListener = packetSendListener
        return this
    }

    //服务器收到客户端数据
    private var packetListener: ((PaimonPacket) -> Unit)? = null

    /**
     * 数据包回调函数
     * @param packetListener
     * @return 数据对象本身
     */
    fun packetListener(packetListener: ((PaimonPacket) -> Unit)?): PaimonPlayer {
        this.packetListener = packetListener
        return this
    }

    /**
     * 发送数据包
     * @param packet 实现 NMS 的Packet数据包
     */
    fun sendPacket(packet: Any) {
        packet.javaClass.also {
            //判断发送的是否为 Packet 数据包
            if (it.interfaces.contains(CraftBukkitPacket.packet)) {
                //发送数据包
                sendPacketMethod?.invoke(connection, packet)
            } else throw RuntimeException("Packet not is a Packet<T> type")
        }
    }

    /**
     * 打开一个界面
     * @param paimonUI Paimon UI
     */
    fun openUI(paimonUI: PaimonUI) {
        paimonUI.open(this)
    }

    /**
     * 关闭玩家目前正在查看的界面
     */
    fun closeUI(gid: Int) {
        sendPacket(
            CraftBukkitPacket.packetPlayOutCloseWindow.getConstructor(Int::class.java)
                .newInstance(gid)
        )
    }

    /**
     * 返回玩家当前所在世界的nms World
     * @return NMS World
     */
    fun getNMSWorld(): Any {
        CraftBukkitPacket.asBukkitClass("CraftWorld").cast(player.world).also {
            return it.javaClass.getDeclaredMethod("getHandle").invoke(it)
        }
    }

    /**
     * 打开Sign牌子界面
     * @param line 行数 最长4行
     * @return 返回 Sign 对象
     */
    fun openSignUI(vararg line: String): Sign {
        return Sign(player).open(line.toList().toTypedArray())
    }
}