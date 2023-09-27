package cc.mcyx.paimon.common.minecraft.network

/**
 * 数据包信息类
 * @param packet Packet数据包
 * @param isCancel 是否拦截数据吧
 */
class PaimonPacket(
    val packet: Any, var isCancel: Boolean
)