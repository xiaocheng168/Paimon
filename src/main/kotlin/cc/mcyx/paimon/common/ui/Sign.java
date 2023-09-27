package cc.mcyx.paimon.common.ui;

import cc.mcyx.paimon.common.minecraft.craftbukkit.CraftBukkitPacket;
import cc.mcyx.paimon.common.minecraft.network.PaimonPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Sign {
    static Class<?> BlockPosition = CraftBukkitPacket.Companion.asNMSPacketClass("BlockPosition");
    static Class<?> Blocks = CraftBukkitPacket.Companion.asNMSPacketClass("Blocks");
    static Class<?> PacketPlayOutBlockChange = CraftBukkitPacket.Companion.asNMSPacketClass("PacketPlayOutBlockChange");
    static Class<?> PacketPlayOutTileEntityData = CraftBukkitPacket.Companion.asNMSPacketClass("PacketPlayOutTileEntityData");
    static Class<?> NBTTagCompound = CraftBukkitPacket.Companion.asNMSPacketClass("NBTTagCompound");
    static Class<?> PacketPlayOutOpenSignEditor = CraftBukkitPacket.Companion.asNMSPacketClass("PacketPlayOutOpenSignEditor");

    //117
    static Class<?> IBlockData = CraftBukkitPacket.Companion.asNMSPacketClass("IBlockData");
    static Class<?> TileEntityTypes = CraftBukkitPacket.Companion.asNMSPacketClass("TileEntityTypes");
    static Class<?> MojangsonParser = CraftBukkitPacket.Companion.asNMSPacketClass("MojangsonParser");

    //112
    static Class<?> CraftWorld = CraftBukkitPacket.Companion.asBukkitClass("CraftWorld");
    static Class<?> Entity = CraftBukkitPacket.Companion.asNMSPacketClass("Entity");
    static Class<?> World = CraftBukkitPacket.Companion.asNMSPacketClass("World");

    public static void openSign(Player player, String[] texts) {
        try {
            PaimonPlayer paimonPlayer = new PaimonPlayer(player);
            if (CraftBukkitPacket.Companion.getServerId() >= 1170) {
                //创建坐标数据包
                Block block = player.getLocation().getBlock();
                Object bp = BlockPosition.getConstructor(int.class, int.class, int.class).newInstance(block.getX(), block.getY(), block.getZ());

                //创建牌子数据包
                Object STANDING_SIGN = Blocks.getField("cE").get(Blocks);
                Object bc = PacketPlayOutBlockChange.getConstructor(BlockPosition, IBlockData).newInstance(bp,
                        STANDING_SIGN.getClass().getMethod("n").invoke(STANDING_SIGN));
                paimonPlayer.sendPacket(bc);

                //改变牌子文本数据包
                Object tileEntityTypes = TileEntityTypes.getField("h").get(TileEntityTypes);
                Constructor<?> constructor = PacketPlayOutTileEntityData.getDeclaredConstructor(BlockPosition, TileEntityTypes, NBTTagCompound);
                constructor.setAccessible(true);

                String s = String.format("{front_text:{messages:['{\"text\":\"%s\"}','{\"text\":\"%s\"}','{\"text\":\"%s\"}','{\"text\":\"%s\"}']}}", texts[0], texts[1], texts[2], texts[3]);
                Object packetSignTextData = MojangsonParser.getMethod("a", String.class).invoke(MojangsonParser, s);

                Object packetPlayOutTileEntityData = constructor.newInstance(bp, tileEntityTypes, packetSignTextData);
                paimonPlayer.sendPacket(packetPlayOutTileEntityData);

                //打开牌子数据包
                paimonPlayer.sendPacket(PacketPlayOutOpenSignEditor.getConstructor(BlockPosition, boolean.class).newInstance(bp, true));

                //移除牌子数据包
                Object AIR = Blocks.getField("a").get(Blocks);
                bc = PacketPlayOutBlockChange.getConstructor(BlockPosition, IBlockData).newInstance(bp, AIR.getClass().getMethod("n").invoke(AIR));
                paimonPlayer.sendPacket(bc);
            }  else if (CraftBukkitPacket.Companion.getServerId() == 1165) {
                //创建坐标数据包
                Block block = player.getLocation().getBlock();
                Object bp = BlockPosition.getConstructor(int.class, int.class, int.class).newInstance(block.getX(), block.getY(), block.getZ());

                //创建牌子数据包
                Object STANDING_SIGN = Blocks.getField("OAK_SIGN").get(Blocks);
                Object bc = PacketPlayOutBlockChange.getConstructor(BlockPosition, IBlockData).newInstance(bp,
                        STANDING_SIGN.getClass().getMethod("getBlockData").invoke(STANDING_SIGN));
                paimonPlayer.sendPacket(bc);

                Object packetSignTextData = NBTTagCompound.getConstructor().newInstance();
                Method method = packetSignTextData.getClass().getMethod("setString", String.class, String.class);
                method.invoke(packetSignTextData, "id", "minecraft:sign");
                method.invoke(packetSignTextData, "Text1", String.format("{\"text\":\"%s\"}", texts[0]));
                method.invoke(packetSignTextData, "Text2", String.format("{\"text\":\"%s\"}", texts[1]));
                method.invoke(packetSignTextData, "Text3", String.format("{\"text\":\"%s\"}", texts[2]));
                method.invoke(packetSignTextData, "Text4", String.format("{\"text\":\"%s\"}", texts[3]));
                Method method1 = packetSignTextData.getClass().getMethod("setInt", String.class, int.class);
                method1.invoke(packetSignTextData, "x", block.getX());
                method1.invoke(packetSignTextData, "y", block.getY());
                method1.invoke(packetSignTextData, "z", block.getZ());

                //改变牌子文本数据包
                Object packetPlayOutTileEntityData = PacketPlayOutTileEntityData.getConstructor(BlockPosition, int.class, NBTTagCompound).newInstance(bp, 9, packetSignTextData);
                paimonPlayer.sendPacket(packetPlayOutTileEntityData);

                //打开牌子数据包
                paimonPlayer.sendPacket(PacketPlayOutOpenSignEditor.getConstructor(BlockPosition).newInstance(bp));
            } else {
                //创建坐标数据包
                Object world = CraftWorld.cast(player.getWorld());
                System.out.println(Arrays.toString(BlockPosition.getConstructors()));

                Object bp = BlockPosition.getConstructor(Entity).newInstance(paimonPlayer.getEntityPlayer());

                //创建牌子数据包
                Object bc = PacketPlayOutBlockChange.getConstructor(World, BlockPosition).newInstance(
                        world.getClass().getMethod("getHandle").invoke(world), bp);
                Object STANDING_SIGN = Blocks.getField("STANDING_SIGN").get(Blocks);
                bc.getClass().getField("block").set(bc,
                        STANDING_SIGN.getClass().getMethod("getBlockData").invoke(STANDING_SIGN));
                paimonPlayer.sendPacket(bc);

                Object packetSignTextData = NBTTagCompound.getConstructor().newInstance();
                Method method = packetSignTextData.getClass().getMethod("setString", String.class, String.class);
                method.invoke(packetSignTextData, "id", "minecraft:sign");
                method.invoke(packetSignTextData, "Text1", String.format("{\"text\":\"%s\"}", texts[0]));
                method.invoke(packetSignTextData, "Text2", String.format("{\"text\":\"%s\"}", texts[1]));
                method.invoke(packetSignTextData, "Text3", String.format("{\"text\":\"%s\"}", texts[2]));
                method.invoke(packetSignTextData, "Text4", String.format("{\"text\":\"%s\"}", texts[3]));

                //改变牌子文本数据包
                Object packetPlayOutTileEntityData = PacketPlayOutTileEntityData.getConstructor(BlockPosition, int.class, NBTTagCompound).newInstance(bp, 9, packetSignTextData);
                paimonPlayer.sendPacket(packetPlayOutTileEntityData);

                //打开牌子数据包
                paimonPlayer.sendPacket(PacketPlayOutOpenSignEditor.getConstructor(BlockPosition).newInstance(bp));

                //移除牌子数据包
                Object AIR = Blocks.getField("AIR").get(Blocks);
                bc.getClass().getField("block").set(bc, AIR.getClass().getMethod("getBlockData").invoke(AIR));
                paimonPlayer.sendPacket(bc);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
