package de.axelrindle.simplecoins.command

import de.axelrindle.pocketknife.util.sendMessageF
import de.axelrindle.simplecoins.CoinManager
import de.axelrindle.simplecoins.SimpleCoins
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for retrieving the amount of coins for a given player.
 */
internal class GetCommand : CoinCommand() {

    override fun getName(): String {
        return "get"
    }

    override fun getDescription(): String {
        return "Returns the amount of coins for yourself or the given player."
    }

    override fun getUsage(): String {
        return "/simplecoins get [player]"
    }

    override fun getPermission(): String {
        return "simplecoins.get"
    }

    override fun handle(sender: CommandSender, command: Command, args: Array<out String>): Boolean {
        if (sender !is Player && args.isEmpty()) {
            sender.sendMessageF("&cThe console does not have any balance!")
            return true
        }

        val targetName = if (args.isNotEmpty()) args[0] else sender.name
        val player = validate(args, sender) ?: return true

        val currency = CoinManager.getCurrentName()
        val got = CoinManager.getCoins(player.uniqueId.toString())
        sender.sendMessageF("${SimpleCoins.prefix} The player &a$targetName &rcurrently has &a$got $currency&r.")

        return true
    }

    override fun validateArguments(args: Array<out String>): Boolean {
        return args.size in 0..1
    }
}