package cc.mcyx.paimon.common.command

import cc.mcyx.paimon.common.plugin.Paimon

/**
 * 子命令批处理类
 * @param paimon 插件主体
 * @param command 命令头
 */
class PaimonSubCommand(paimon: Paimon, command: String, permissionNode: String = "") :
    PaimonCommand(paimon, command, permissionNode)