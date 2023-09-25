package cc.mcyx.paimon.common.minecraft.network

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.ui.PaimonUI
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import org.bukkit.entity.Player

class PaimonPlayer(val player: Player) {

    //EntityPlayer 对象
    val entityPlayer: Any =
        CraftBukkitPacket.craftPlayer.getDeclaredMethod("getHandle").invoke(player)

    //PlayerConnection 玩家连接
    val connection: Any = CraftBukkitPacket.getObject(entityPlayer, "PlayerConnection")

    //Network 网络类
    val network: Any = CraftBukkitPacket.getObject(connection, "NetworkManager")

    //NIO Channel通道对象
    val channel: Channel = CraftBukkitPacket.getObject(network, "Channel") as Channel

    //发包方法
    val sendPacketMethod =
        CraftBukkitPacket.getClassMethod(connection::class.java, Void::class.java, CraftBukkitPacket.packet)

    //数据包处理对象
    private val packetHandler = object : ChannelDuplexHandler() {
        override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
            //数据包不能为null 且是否拦截数据包
            if (msg != null) {
                val packet = Packet(msg, false)
                packetListener?.invoke(packet)
                //是否不拦截数据包
                if (!packet.isCancel) super.channelRead(ctx, msg)
            }
        }
    }

    init {
        //代理玩家初始化
        try {
            //尝试删除该玩家的通道
            channel.pipeline().remove(player.name)
            //报错代表不存在将添加通道用于监听收到的数据包Packet<*>
            channel.pipeline().addBefore("packet_handler", player.name, packetHandler)
        } catch (e: Exception) {
            //报错代表不存在将添加通道用于监听收到的数据包Packet<*>
            channel.pipeline().addBefore("packet_handler", player.name, packetHandler)
        }
    }

    fun nextContainerCounter(): Int {
        return entityPlayer.javaClass.getDeclaredMethod("nextContainerCounter").invoke(entityPlayer) as Int
    }


    //数据包回调对象
    private var packetListener: ((Packet) -> Unit)? = null

    /**
     * 数据包回调函数
     * @param packetListener
     * @return 数据对象本身
     */
    fun packetListener(packetListener: ((Packet) -> Unit)?): PaimonPlayer {
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
                for (genericInterface in it.genericInterfaces) {
                    if (genericInterface.toString().endsWith(".PacketListenerPlayOut>")) {
                        //查找对应的发包方法
                        for (declaredMethod in connection.javaClass.declaredMethods) {
                            //判断该方法是否为发包方法
                            if (declaredMethod.parameterTypes.size == 1 &&
                                declaredMethod.parameterTypes[0] == CraftBukkitPacket.packet
                            ) {
                                //发送数据包
                                sendPacketMethod?.invoke(connection, packet)
                                return@also
                            }
                        }

                    }
                }
                throw RuntimeException("Packet not is a Packet<PacketListenerPlayOut> type")
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
}