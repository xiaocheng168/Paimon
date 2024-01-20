package cc.mcyx.paimon.lib

import cc.mcyx.paimon.common.PaimonPlugin
import cc.mcyx.paimon.common.command.PaimonCommand
import cc.mcyx.paimon.common.command.PaimonSubCommand
import org.bukkit.Bukkit

class PaimonLib : PaimonPlugin() {
    override fun onEnabled() {
        logger.info("PaimonLib Enable")
        logger.info("Server ${Bukkit.getVersion()}")
        logger.info("Version ${getPluginYmlConfig().getString("version")}")

        val paimonCommand = PaimonCommand(this, "nihao")

        paimonCommand.addSubCommand(PaimonSubCommand(this, "halo1", paimonCommand)
            .also { it ->
                it.addSubCommand(PaimonSubCommand(this, "nia", it))
                it.addSubCommand(PaimonSubCommand(this, "open", it))
                it.addSubCommand(PaimonSubCommand(this, "aaa", it).also {
                    it.addSubCommand(
                        PaimonSubCommand(
                            this,
                            "wc66",
                            it
                        )
                    )
                })
                it.addSubCommand(PaimonSubCommand(this, "ccc", it))
            })
        paimonCommand.addSubCommand(PaimonSubCommand(this, "b", paimonCommand))
        paimonCommand.register()
    }
}