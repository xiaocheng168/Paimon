package cc.mcyx.paimon.common.util

import cc.mcyx.paimon.common.Paimon
import cc.mcyx.paimon.common.listener.PaimonAutoListener
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.util.*
import java.util.jar.JarFile

open class PaimonClassLoader(val paimon: Paimon) {
    /**
     * 加载某个插件
     */
    open fun loadPlugin() {
        val jarFile =
            JarFile(paimon.cl.getResource("plugin.yml")!!.file.replace("file:/", "").replace("!/plugin.yml", ""))
        //加载类
        this.loadJarClass(jarFile).forEach { loadClass(it) }
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

    /**
     * 查找jar里的所有类
     * @param jarFile jar文件
     * @return 类列表
     */
    open fun loadJarClass(jarFile: JarFile): MutableList<String> {
        val classesList = mutableListOf<String>()
        // STOPSHIP: 判断插件是否重复加载、开始对GUI框架进行编写
        jarFile.entries().iterator().forEach {
            //必须是class文件 且不能是目录
            if (!it.isDirectory && it.name.endsWith(".class") && !it.name.startsWith("META-INF")) {
                val classes = it.name.replace("/", ".").substring(0, it.name.length - 6)
                classesList.add(classes)
            }
        }
        return classesList
    }

    /**
     * 加载指定类库
     * @param classes 加载类绝对路径
     */
    open fun loadClass(classes: String) {
        try {
            Class.forName(classes)
            paimon.cl.loadClass(classes)
        } catch (_: Error) {
        } catch (_: ClassNotFoundException) {
        }
    }
}