package cc.mcyx.paimon.common.util

import cc.mcyx.paimon.common.Paimon
import cc.mcyx.paimon.common.listener.PaimonAutoListener
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.*
import java.util.jar.JarFile

/**
 * 加载某个插件
 * @param paimon Paimon插件对象
 */
fun loadPlugin(paimon: Paimon) {
    val entries =
        JarFile(paimon.cl.getResource("plugin.yml")!!.file.replace("file:/", "").replace("!/plugin.yml", "")).entries()
    // STOPSHIP: 判断插件是否重复加载、开始对GUI框架进行编写
    entries.iterator().forEach {
        if (!it.isDirectory) {
            //结尾必须是class
            if (it.name.endsWith(".class") && it.name.startsWith(Class.forName(paimon.description.main).`package`.name)) {
                val classes = it.name.replace("/", ".").substring(0, it.name.length - 6)
                try {
                    //如果类不存在将导入
                    Class.forName(classes)
                } catch (e: ClassNotFoundException) {
                    //加载该类
                    paimon.cl.loadClass(classes)
                }
            }
        }
    }

    //反射获取插件所有加载列表
    val declaredField = ClassLoader::class.java.getDeclaredField("classes")
    declaredField.isAccessible = true
    val classes = declaredField.get(paimon.cl) as Vector<Class<*>>
    //获取当前插件主类包路径
    val pluginPacket = Class.forName(paimon.description.main).`package`.name
    //获取插件加载类表
    val paimonPluginClass = classes.filter { it.toString().contains(pluginPacket) }

    //遍历加载列表
    paimonPluginClass.iterator().forEach {
        //如果这个类不是接口
        if (!it.isInterface) {
            //判断这个类是否存在 PaimonListener
            if (it.interfaces.contains(PaimonAutoListener::class.java)) {
                Bukkit.getPluginManager().registerEvents(it.newInstance() as Listener, paimon)
            }
        }
    }
}