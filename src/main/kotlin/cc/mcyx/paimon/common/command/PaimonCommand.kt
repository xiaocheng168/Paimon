package cc.mcyx.paimon.common.command

import cc.mcyx.paimon.common.minecraft.craftbukkit.registerCommand
import cc.mcyx.paimon.common.minecraft.network.PaimonSender
import cc.mcyx.paimon.common.plugin.Paimon
import org.bukkit.command.Command
import org.bukkit.command.CommandSender


/**
 * 命令批处理类
 * @param paimon 插件主体
 * @param command 命令头
 * @param parentCommand 父命令
 */
open class PaimonCommand(
    val paimon: Paimon,
    val command: String,
    val parentCommand: PaimonCommand? = null
) :
    Command(command) {

    companion object {
        /**
         * 获取当前命令的默认权限节点
         */
        fun getRoot(paimonCommand: PaimonCommand): String {
            if (paimonCommand.parentCommand == null) {
                return paimonCommand.name
            }
            return "${getRoot(paimonCommand.parentCommand)}.${paimonCommand.name}"
        }

        /**
         * 某个对象是否拥有此权限系欸但
         *
         * @return 返回权限是否存在
         */
        fun hasPermission(sender: CommandSender, paimonCommand: PaimonCommand): Boolean {
            return sender.hasPermission(paimonCommand.permission)
        }

    }

    init {
        permission = getRoot(this)
        permissionMessage = "§c你没有权限使用该命, 缺少权限 $permission"
    }

    //子命令
    val subCommand: HashMap<String, PaimonCommand> = LinkedHashMap()

    final override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        //循环所有命令参数表
        for (p in p2) {
            //循环当前命令的所有子命令
            for (mutableEntry in subCommand) {
                //如果存在与该命令名一致的将执行该命令批处理对象
                if (mutableEntry.key == p) {
                    //给予子命令自身处理
                    return mutableEntry.value.execute(p0, p1, p2)
                }
            }
        }
        //如果都没有表示到达命令当前位置，默认给予当前命令所有子命令
        //处理反应
        return this.paimonExec(p0, p1, p2)
    }

    final override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        val tabCommand = mutableListOf<String>()
        if (args.size < 2) {
            //权限拦截
            subCommand.forEach { if (hasPermission(sender, it.value)) tabCommand.add(it.key) }
            tabCommand.addAll(this.paimonTab(sender, command, args))
            return tabCommand.filter { it.lowercase().startsWith(args[args.size - 1].lowercase()) }.toMutableList()
        }
        //循环所有命令参数表
        for (p in args) {
            //循环当前命令的所有子命令
            for (mutableEntry in subCommand) {
                //如果存在与该命令名一致的将执行该命令批处理对象
                if (mutableEntry.key == p) {
                    //给予子命令自身处理
                    return mutableEntry.value.tabComplete(sender, alias, args)
                        .filter { it.lowercase().startsWith(args[args.size - 1].lowercase()) }.toMutableList()
                }
            }
        }

        //如果都没有表示到达命令当前位置，默认给予当前命令所有子命令
        if (args[args.size - 2] != "")
            subCommand.forEach { if (hasPermission(sender, it.value)) tabCommand.add(it.key) }
        //给与命令Tab处理反应 且结尾必须是空的 或者目前子命令为当前批处理对象
        if (args[args.size - 1] == "" || args[args.size - 2] == this.command) {
            tabCommand.addAll(this.paimonTab(sender, command, args))
        }
        return tabCommand.filter { it.startsWith(args[args.size - 1]) }.toMutableList()
    }


    /**
     * 命令执行触发方法
     * @param sender 发送着
     * @param command 命令头
     * @param args 参数表
     * @return 返回命令是否执行有效
     */
    private fun paimonExec(sender: CommandSender, command: String, args: Array<out String>): Boolean {
        //处理权限判断
        if (!hasPermission(sender, this)) {
            PaimonSender.sendMessage(sender, permissionMessage)
            return false
        }
        return paimonExec?.invoke(sender, command, args) ?: return false
    }


    /**
     * Tab命令补全请求
     * @param sender 发送着
     * @param command 命令头
     * @param args 参数表
     * @return 返回命令是否执行有效
     */
    private fun paimonTab(sender: CommandSender, command: String, args: Array<out String>): MutableList<String> {
        return paimonTab?.invoke(sender, command, args) ?: return mutableListOf()
    }


    /**
     * 注册子命令
     * @param paimonSubCommand 子命令批处理类
     * @return 该子命令的父命令
     */
    fun addSubCommand(paimonSubCommand: PaimonCommand): PaimonCommand {
        subCommand[paimonSubCommand.command] = paimonSubCommand
        return this
    }


    /**
     * 注册命令
     * @return 该命令本体
     */
    open fun register(): PaimonCommand {
        registerCommand(this)
        return this
    }


    private var paimonTab: ((sender: CommandSender, command: String, args: Array<out String>) -> MutableList<String>)? =
        null
    private var paimonExec: ((sender: CommandSender, command: String, args: Array<out String>) -> Boolean)? =
        null

    /**
     * Tab命令补全请求
     * @param event 批处理对象
     */
    fun paimonTab(event: (sender: CommandSender, command: String, args: Array<out String>) -> MutableList<String>): PaimonCommand {
        this.paimonTab = event
        return this
    }

    /**
     * 命令执行触发方法
     * @param event 批处理对象
     */
    fun paimonExec(event: (sender: CommandSender, command: String, args: Array<out String>) -> Boolean): PaimonCommand {
        this.paimonExec = event
        return this
    }
}