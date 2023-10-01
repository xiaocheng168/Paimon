package cc.mcyx.paimon.common.config

import cn.hutool.core.io.watch.Watcher
import java.nio.file.Path
import java.nio.file.WatchEvent

open class WatcherImpl : Watcher {
    override fun onCreate(p0: WatchEvent<*>?, p1: Path?) {
    }

    override fun onModify(p0: WatchEvent<*>?, p1: Path?) {
    }

    override fun onDelete(p0: WatchEvent<*>?, p1: Path?) {
    }

    override fun onOverflow(p0: WatchEvent<*>?, p1: Path?) {
    }
}