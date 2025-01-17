package de.axelrindle.simplecoins

import de.axelrindle.pocketknife.PocketCommand
import de.axelrindle.pocketknife.PocketConfig
import de.axelrindle.pocketknife.PocketLang
import de.axelrindle.simplecoins.command.SimpleCoinsCommand
import de.axelrindle.simplecoins.hooks.SimpleCoinsEconomy
import de.axelrindle.simplecoins.hooks.SimpleCoinsPlaceholderExpansion
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import org.jetbrains.annotations.NotNull
import java.io.File
import java.io.IOException

/**
 * Main plugin class.
 */
class SimpleCoins : JavaPlugin {

    constructor() : super()
    constructor(
            loader: @NotNull JavaPluginLoader,
            description: @NotNull PluginDescriptionFile,
            dataFolder: @NotNull File,
            file: File?
    ) : super(loader, description, dataFolder, File(System.getProperty("user.dir", PLUGIN_FILE)))

    companion object {
        private const val PLUGIN_FILE = "src/main/kotlin/de/axelrindle/simplecoins/SimpleCoins.kt"
        const val prefix = "§7Sparks>"

        private var instance: SimpleCoins? = null

        /**
         * @return The active [SimpleCoins] instance.
         */
        fun get(): SimpleCoins {
            return instance!!
        }
    }

    internal val pocketConfig: PocketConfig = PocketConfig(this)
    internal val pocketLang = PocketLang(this, pocketConfig)

    override fun onEnable() {
        if (instance != null)
            throw IllegalStateException("Plugin has already been initialized!")
        instance = this

        // config init
        logger.info("Loading configuration...")
        try {
            pocketConfig.register("config", getResource("config.yml")!!)
            pocketConfig.register("database", getResource("database.yml")!!)
        } catch (e: IOException) {
            e.printStackTrace()
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // localization
        logger.info("Loading localization...")
        pocketLang.addLanguages("en", "de")
        pocketLang.init()

        // database init
        logger.info("Loading CoinManager...")
        CoinManager.init(pocketConfig)

        // hook into other plugins
        connectToVault()
        connectToPlaceholderApi()

        // register command
        PocketCommand.register(this, SimpleCoinsCommand())

        logger.info("SimpleCoins v${description.version} initialized.")
    }

    override fun onDisable() {
        CoinManager.close()
    }

    /**
     * Registers the [SimpleCoinsEconomy] class as an [Economy] service manager.
     */
    private fun connectToVault() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            server.servicesManager.register(
                    Economy::class.java,
                    SimpleCoinsEconomy(pocketConfig),
                    this,
                    ServicePriority.Normal
            )
            logger.info("Connected to Vault.")
        }
    }

    private fun connectToPlaceholderApi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (SimpleCoinsPlaceholderExpansion(this).register()) {
                logger.info("Connected to PlaceholderAPI.")
            } else {
                logger.warning("Failed to connect to PlaceholderAPI!")
            }
        }
    }


}
