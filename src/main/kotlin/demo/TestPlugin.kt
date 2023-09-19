package demo

import cc.mcyx.paimon.common.Paimon
import cc.mcyx.paimon.common.command.PaimonCommand
import org.bukkit.command.CommandSender

class TestPlugin : Paimon() {
    override fun onEnable() {
        val paimonCommand = PaimonCommand(this, "tpaccept")

        paimonCommand.addSubCommand(object : PaimonCommand(this, "wc") {

            init {

                addSubCommand(object : PaimonCommand(this@TestPlugin, "halowc") {

                    init {
                        addSubCommand(object : PaimonCommand(this@TestPlugin, "haloaaa") {


                        })
                    }
                })


                addSubCommand(object : PaimonCommand(this@TestPlugin, "nihao") {
                    init {
                        addSubCommand(object : PaimonCommand(this@TestPlugin, "nihaiaaaa") {

                            override fun paimonTab(
                                sender: CommandSender,
                                command: String,
                                args: Array<out String>
                            ): MutableList<String> {
                                return mutableListOf("你好啊!")
                            }

                            override fun paimonExec(
                                sender: CommandSender,
                                command: String,
                                args: Array<out String>
                            ): Boolean {
                                sender.sendMessage("halo啊")
                                return true
                            }
                        })
                    }
                })
            }
        })
        paimonCommand.register()
    }
}