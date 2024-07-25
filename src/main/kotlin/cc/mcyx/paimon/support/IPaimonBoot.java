package cc.mcyx.paimon.support;

public interface IPaimonBoot<T> {
    void init(T plugin);

    void enable(T plugin);

    void disable(T plugin);
}
