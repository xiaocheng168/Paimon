package cc.mcyx.paimon.common.nbt

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket

/**
 * NBT 构建器
 * @param originNbt 原nbt 留空创建新NBT
 */
@Deprecated("临时弃用的方案")
class NbtBuilder(private val originNbt: Any? = null) {
    companion object {
        @JvmStatic
        val NBTTagCompound = CraftBukkitPacket.asNMSPacketClass("NBTTagCompound")

        @JvmStatic
        val NBTBase = CraftBukkitPacket.asNMSPacketClass("NBTBase")

        @JvmStatic
        val NBTTagList = CraftBukkitPacket.asNMSPacketClass("NBTTagList")
    }

    //正在构建的nbt对象
    private val buildNbt: Any = originNbt ?: NBTTagCompound.newInstance()


    /**
     * 设置一个数据到nbt
     */
    fun set(k: String, v: Any) {
        buildNbt.also { nbt ->
            var setType: Class<*> = v.javaClass

            //根据实现接口判断类型
            if (v.javaClass.interfaces.isNotEmpty() && v.javaClass.interfaces[0] == NBTBase) setType = NBTBase

            //根据继承判断类型
            if (v.javaClass.annotatedSuperclass.type.typeName.contains("NBTBase")) setType = NBTBase

            if (CraftBukkitPacket.serverId >= 1170) {
                NBTTagCompound.getDeclaredMethod("a", String::class.java, setType).invoke(nbt, k, v)
            } else {
                val setFun = "set${v.javaClass.simpleName}"
                NBTTagCompound.getDeclaredMethod(setFun, String::class.java, setType).invoke(nbt, k, v)
            }
        }
    }

    init {
        set("qwq", "")
        println(buildNbt)
    }

    class NBTList<T> {

        val nbtList: Any = NBTTagList.newInstance()
        fun add(v: T) {
            nbtList.javaClass.getDeclaredField("")
        }
    }
}