package cc.mcyx.paimon.common.config

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.plugin.Paimon
import cn.hutool.core.io.watch.WatchMonitor
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Path
import java.nio.file.WatchEvent

/**
 * 配置文件托管处理
 * @param paimonPlugin 插件
 * @param configName 配置文件名
 * @param folder 配置文件目录（可选默认在插件文件夹目录）
 * @param saveResource 是否从jar根目录写出文件
 */
class PaimonConfig(
    val paimonPlugin: PaimonPlugin,
    val configName: String,
    val folder: File = paimonPlugin.dataFolder,
    val saveResource: Boolean = false
) {

    companion object {
        //所有托管的配置表
        val paimonConfigMap: HashMap<Paimon, PaimonConfig> = hashMapOf()
    }

    //配置批处理
    var config: YamlConfiguration

    //配置文件
    val configFile: File

    init {
        //是否从jar写出资源
        if (saveResource) {
            PaimonPlugin.paimonPlugin.saveResource(configName, false)
        }

        //初始化文件
        configFile = File(
            folder,
            configName
        ).also {
            //新建目录
            if (!it.parentFile.isDirectory) it.parentFile.mkdirs()
        }.also {
            //新建文件
            if (!it.isFile) it.createNewFile()
            this.config = YamlConfiguration.loadConfiguration(it)
        }

        //监听文件变化
        val watchTask = WatchMonitor.create(configFile)
        watchTask.setWatcher(object : WatcherImpl() {
            override fun onModify(p0: WatchEvent<*>?, p1: Path?) {
                this@PaimonConfig.reloadConfig()
                this@PaimonConfig.configReload?.invoke(this@PaimonConfig)
            }
        })
        //启动!
        watchTask.start()
    }

    /**
     * 加载配置文件
     */
    fun reloadConfig(): YamlConfiguration {
        YamlConfiguration.loadConfiguration(this.configFile).also {
            this.config = it
            return it
        }
    }


    var configReload: ((PaimonConfig) -> Unit)? = null

    /**
     * 当资源文件重新加载时触发该事件，让开发者知道资源文件已加载
     * @param e 事件回调参数
     */
    fun configReload(e: ((PaimonConfig) -> Unit)) {
        this.configReload = e
    }
}