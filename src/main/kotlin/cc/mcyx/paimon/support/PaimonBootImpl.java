package cc.mcyx.paimon.support;

import cc.mcyx.paimon.common.PaimonPlugin;

public abstract class PaimonBootImpl<T> implements IPaimonBoot<T> {
    static {
        System.out.println(PaimonPlugin.isForge());
    }
}
