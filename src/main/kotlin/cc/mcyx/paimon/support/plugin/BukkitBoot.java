package cc.mcyx.paimon.support.plugin;

import cc.mcyx.paimon.common.PaimonPlugin;
import cc.mcyx.paimon.support.IPaimonBoot;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitBoot extends PaimonPlugin implements IPaimonBoot<JavaPlugin> {

    static {
        System.out.println(PaimonPlugin.isForge());
    }

    @Override
    public void init(JavaPlugin plugin) {

    }

    @Override
    public void enable(JavaPlugin plugin) {

    }

    @Override
    public void disable(JavaPlugin plugin) {

    }
}
