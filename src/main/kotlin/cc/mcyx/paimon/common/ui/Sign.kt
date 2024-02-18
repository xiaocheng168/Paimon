package cc.mcyx.paimon.common.ui

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket
import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket.Companion.serverId
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer
import org.bukkit.entity.Player

class Sign(private val player: Player) {
    private companion object {
        /*
        * 共有类(1.8-最新版本)
        * */
        val BlockPosition = CraftBukkitPacket.asNMSPacketClass("BlockPosition")
        val PacketPlayOutBlockChange = CraftBukkitPacket.asNMSPacketClass("PacketPlayOutBlockChange")
        val Blocks = CraftBukkitPacket.asNMSPacketClass("Blocks")
        val Block = CraftBukkitPacket.asNMSPacketClass("Block")
        val PacketPlayOutTileEntityData = CraftBukkitPacket.asNMSPacketClass("PacketPlayOutTileEntityData")
        val NBTTagCompound = CraftBukkitPacket.asNMSPacketClass("NBTTagCompound")
        val PacketPlayOutOpenSignEditor = CraftBukkitPacket.asNMSPacketClass("PacketPlayOutOpenSignEditor")
        val PacketPlayInUpdateSign = CraftBukkitPacket.asNMSPacketClass("PacketPlayInUpdateSign")
        val MojangsonParser = CraftBukkitPacket.asNMSPacketClass("MojangsonParser")

        /*
        * 特殊类
        * */
        val ChatComponentText = CraftBukkitPacket.asNMSPacketClass("ChatComponentText")  //1.10以下
        val IChatBaseComponent = CraftBukkitPacket.asNMSPacketClass("IChatBaseComponent")  //1.10以下
        val World = CraftBukkitPacket.asNMSPacketClass("World")  //1.10以下
        val PacketPlayOutUpdateSign = CraftBukkitPacket.asNMSPacketClass("PacketPlayOutUpdateSign")  //1.10以下
        val IBlockData = CraftBukkitPacket.asNMSPacketClass("IBlockData")  //1.17以上
        val TileEntityTypes = CraftBukkitPacket.asNMSPacketClass("TileEntityTypes")  //1.17以上

        /*
        * 静态字段
        * */
        val sign = if (serverId >= 1170) "cE" else if (serverId >= 1165) "OAK_SIGN" else "STANDING_SIGN"
        val signIBlockData = CraftBukkitPacket.getStaticObject(Block, Blocks.getField(sign).get(null), "IBlockData")
    }

    /*
    * 显示牌子编辑界面并显示文字
    * */
    fun open(line: Array<String> = arrayOf()): Sign {
        line.forEachIndexed { index, s ->
            line[index] = s.replace("&","§")
        }
        val paimonPlayer = PaimonPlayer(player)
        val block = player.location.block
        val blockPosition = BlockPosition.getConstructor(
            Int::class.java, Int::class.java, Int::class.java
        ).newInstance(block.x, block.y, block.z)

        //创建改变方块数据包(改变成牌子)
        val packetPlayOutBlockChange =
            if (serverId >= 1170)
                PacketPlayOutBlockChange.getDeclaredConstructor(BlockPosition, IBlockData)
                    .newInstance(blockPosition, signIBlockData)
            else PacketPlayOutBlockChange.newInstance().also {
                it.javaClass.getDeclaredField(CraftBukkitPacket.getObjectName(it, "BlockPosition"))
                    .apply { isAccessible = true }.set(it, blockPosition)
                it.javaClass.getDeclaredField(CraftBukkitPacket.getObjectName(it, "IBlockData"))
                    .set(it, signIBlockData)
            }
        paimonPlayer.sendPacket(packetPlayOutBlockChange)

        //创建编辑牌子数据包
        if (serverId >= 1100) {
            val nBTTagCompound =
                MojangsonParser.getMethod(if (serverId >= 1180) "a" else "parse", String::class.java).invoke(
                    null,
                    if (serverId >= 1180) """
                    {front_text:{messages:['
                    {"text":"${if (line.isNotEmpty()) line[0] else ""}"}',
                    '{"text":"${if (line.size >= 2) line[1] else ""}"}',
                    '{"text":"${if (line.size >= 3) line[2] else ""}"}',
                    '{"text":"${if (line.size >= 4) line[3] else ""}"}']}}
                """.trimIndent()
                    else """
                    {Text1:"{\"text\":\"${if (line.isNotEmpty()) line[0] else ""}\"}",
                    Text2:"{\"text\":\"${if (line.size >= 2) line[1] else ""}\"}",
                    Text3:"{\"text\":\"${if (line.size >= 3) line[2] else ""}\"}",
                    Text4:"{\"text\":\"${if (line.size >= 4) line[3] else ""}\"}"}
                """.trimIndent()
                )
            val packetPlayOutTileEntityData =
                if (serverId >= 1180)
                    PacketPlayOutTileEntityData.getDeclaredConstructor(BlockPosition, TileEntityTypes, NBTTagCompound)
                        .apply { isAccessible = true }
                        .newInstance(blockPosition, TileEntityTypes.getDeclaredField("h").get(null), nBTTagCompound)
                else PacketPlayOutTileEntityData.getConstructor(BlockPosition, Int::class.java, NBTTagCompound)
                    .newInstance(blockPosition, 9, nBTTagCompound)
            paimonPlayer.sendPacket(packetPlayOutTileEntityData)
        } else {
            val chatComponents = java.lang.reflect.Array.newInstance(IChatBaseComponent, 4)
            line.forEachIndexed { index, s ->
                val chatSerializer: Any = IChatBaseComponent.declaredClasses[0].newInstance()
                val o = chatSerializer.javaClass.getMethod("a", String::class.java)
                    .invoke(chatSerializer, "{\"text\":\"${if (line.size >= index + 1) s else ""}\"}")
                java.lang.reflect.Array.set(chatComponents, index, o)
            }
            val packetPlayOutUpdateSign: Any = PacketPlayOutUpdateSign.getConstructor(
                World, BlockPosition,
                chatComponents.javaClass
            ).newInstance(paimonPlayer.getNMSWorld(), blockPosition, chatComponents)
            paimonPlayer.sendPacket(packetPlayOutUpdateSign)
        }

        //创建打开牌子数据包
        val packetPlayOutOpenSignEditor =
            if (serverId >= 1200)
                PacketPlayOutOpenSignEditor.getConstructor(BlockPosition, Boolean::class.java)
                    .newInstance(blockPosition, true)
            else PacketPlayOutOpenSignEditor.getConstructor(BlockPosition).newInstance(blockPosition)
        paimonPlayer.sendPacket(packetPlayOutOpenSignEditor)

        var first = true
        paimonPlayer.packetListener {
            if (first && it.packet.javaClass.simpleName == "PacketPlayInUpdateSign") {
                first = false
                val packetPlayInUpdateSign = PacketPlayInUpdateSign.cast(it.packet)
                val lineValue = (CraftBukkitPacket.getStaticObject(
                    PacketPlayInUpdateSign,
                    packetPlayInUpdateSign,
                    if (serverId >= 1100) "String;" else "IChatBaseComponent;"
                ) as Array<*>).toMutableList()
                if (serverId < 1100)
                    repeat(lineValue.size) { i ->
                        lineValue[i] = ChatComponentText.cast(lineValue[i]).javaClass.getDeclaredField("b")
                            .apply { isAccessible = true }.get(lineValue[i])
                    }
                this.line?.invoke(player, lineValue)
                paimonPlayer.player.sendBlockChange(
                    player.location,
                    player.location.block.type,
                    player.location.block.data
                )
                it.isCancel = true
            }
        }

        return this
    }

    private var line: ((Player, List<*>) -> Unit)? = null

    fun getLine(line: ((Player, List<*>) -> Unit)?) {
        this.line = line
    }

}