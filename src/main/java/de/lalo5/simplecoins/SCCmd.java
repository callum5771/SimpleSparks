package de.lalo5.simplecoins;

import de.lalo5.simplecoins.util.Perms;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;

import static de.lalo5.simplecoins.SimpleCoins.*;

/**
 * Created by Axel on 27.12.2015.
 *
 * Project MinecraftPlugins
 */
class SCCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(cmd.getName().equalsIgnoreCase("sc")) {
                if(p.hasPermission(Perms.MAIN.perm())) {
                    if(args.length == 0) {

                        SimpleCoins.sendHelp(p);

                        return true;
                    } else if(args[0].equalsIgnoreCase("add")) {
                        if(p.hasPermission(Perms.ADD.perm())) {
                            if(args.length == 3) {

                                Player p_ = Bukkit.getPlayer(args[1]);
                                if(p_ != null) {
                                    double amount;
                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException e) {
                                        p.sendMessage(SimpleCoins.colorize(PREFIX + "&cPlease enter a number as the second argument!"));
                                        return false;
                                    }

                                    CoinManager.addCoins(p_, amount);
                                    if(vaultEnabled && econ != null) {
                                        EconomyResponse r = econ.depositPlayer(p_, amount);
                                        if(r.transactionSuccess() && fileConfiguration.getBoolean("UseVaultMessages")) {
                                            p_.sendMessage(String.format("[Vault] You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
                                        } else {
                                            p_.sendMessage(String.format("[Vault] An error occured: %s", r.errorMessage));
                                        }
                                    }

                                    String message = fileConfiguration.getString("Messages.Coins_Received");
                                    message = message.replaceAll("%amountrec%", String.valueOf(amount));
                                    message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                    message = message.replaceAll("%playername%", p_.getName());
                                    message = message.replaceAll("%coinname%", fileConfiguration.getString("CoinsName"));

                                    p_.sendMessage(SimpleCoins.colorize(PREFIX + message));
                                } else {
                                    p.sendMessage(SimpleCoins.colorize(PREFIX + "&cThis player does not exist!"));
                                }

                                return true;
                            }
                        } else {
                            p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                            return true;
                        }
                    } else if(args[0].equalsIgnoreCase("remove")) {
                        if(p.hasPermission(Perms.REMOVE.perm())) {
                            if(args.length == 3) {

                                Player p_ = Bukkit.getPlayer(args[1]);
                                if(p_ != null) {
                                    double amount;
                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException e) {
                                        p.sendMessage(SimpleCoins.colorize(PREFIX + "&cPlease enter a number as the second argument!"));
                                        return false;
                                    }

                                    double now = CoinManager.getCoins(p_);
                                    if(now != 0) {
                                        if(amount <= now) {
                                            CoinManager.removeCoins(p_, amount);
                                            if(vaultEnabled && econ != null) {
                                                EconomyResponse r = econ.withdrawPlayer(p_, amount);
                                                if(r.transactionSuccess() && fileConfiguration.getBoolean("UseVaultMessages")) {
                                                    p_.sendMessage(String.format("[Vault] You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
                                                } else {
                                                    p_.sendMessage(String.format("[Vault] An error occured: %s", r.errorMessage));
                                                }
                                            }

                                            String message = fileConfiguration.getString("Messages.Coins_Taken");
                                            message = message.replaceAll("%amountrec%", String.valueOf(amount));
                                            message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                            message = message.replaceAll("%playername%", p_.getName());
                                            message = message.replaceAll("%coinname%", fileConfiguration.getString("CoinsName"));

                                            p_.sendMessage(SimpleCoins.colorize(PREFIX + message));
                                        } else {
                                            p.sendMessage(SimpleCoins.colorize(PREFIX + "&cAmount muss be less than or equals to &2" + now));
                                        }
                                    } else {
                                        p.sendMessage(SimpleCoins.colorize(PREFIX + "&cPlayer " + p_.getName() +  "has 0 &9" + fileConfiguration.getString("CoinsName") + "&c!"));
                                    }
                                } else {
                                    p.sendMessage(SimpleCoins.colorize(PREFIX + "&cThis player does not exist!"));
                                }

                                return true;
                            }
                        } else {
                            p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                            return true;
                        }
                    } else if(args[0].equalsIgnoreCase("set")) {
                        if(p.hasPermission(Perms.SET.perm())) {
                            if(args.length == 3) {

                                Player p_ = Bukkit.getPlayer(args[1]);
                                if(p_ != null) {
                                    double amount;
                                    try {
                                        amount = Double.parseDouble(args[2]);
                                    } catch (NumberFormatException e) {
                                        p.sendMessage(SimpleCoins.colorize(PREFIX + "&cPlease enter a number as the second argument!"));
                                        return false;
                                    }

                                    if(amount > 0) {
                                        CoinManager.setCoins(p_, amount);

                                        String message = fileConfiguration.getString("Messages.Coins_Set");
                                        message = message.replaceAll("%amount%", String.valueOf(CoinManager.getCoins(p_)));
                                        message = message.replaceAll("%playername%", p_.getName());
                                        message = message.replaceAll("%coinname%", fileConfiguration.getString("CoinsName"));

                                        p_.sendMessage(SimpleCoins.colorize(PREFIX + message));
                                    } else {
                                        p.sendMessage(SimpleCoins.colorize(PREFIX + "&cAmount must be greater than or equals to &20&c!"));
                                    }
                                } else {
                                    p.sendMessage(SimpleCoins.colorize(PREFIX + "&cThis player does not exist!"));
                                }

                                return true;
                            }
                        } else {
                            p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                            return true;
                        }
                    } else if(args[0].equalsIgnoreCase("get")) {
                        if(args.length == 2) {
                            if(p.hasPermission(Perms.GETOTHER.perm())) {

                                Player p_ = Bukkit.getPlayer(args[1]);
                                if(p_ != null) {
                                    double amount = CoinManager.getCoins(p_);

                                    String message = fileConfiguration.getString("Messages.Coins_Get_Other");
                                    message = message.replaceAll("%amount%", String.valueOf(amount));
                                    message = message.replaceAll("%playername%", p.getName());
                                    message = message.replaceAll("%otherplayername%", p_.getName());
                                    message = message.replaceAll("%coinname%", fileConfiguration.getString("CoinsName"));

                                    p.sendMessage(SimpleCoins.colorize(PREFIX + message));

                                    return true;
                                } else {
                                    p.sendMessage(SimpleCoins.colorize(PREFIX + "&cThis player does not exist!"));
                                }

                            } else {
                                p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                                return true;
                            }
                        } else if(args.length == 1) {
                            if(p.hasPermission(Perms.GETSELF.perm())) {
                                double amount = CoinManager.getCoins(p);

                                String message = fileConfiguration.getString("Messages.Coins_Get_Self");
                                message = message.replaceAll("%amount%", String.valueOf(amount));
                                message = message.replaceAll("%playername%", p.getName());
                                message = message.replaceAll("%coinname%", fileConfiguration.getString("CoinsName"));

                                p.sendMessage(SimpleCoins.colorize(PREFIX + message));
                                return true;
                            } else {
                                p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                                return true;
                            }
                        }
                    } else if(args[0].equalsIgnoreCase("reload")) {
                        if(p.hasPermission(Perms.RELOAD.perm())) {
                            if(args.length == 1) {
                                if(!vaultEnabled) {
                                    try {
                                        if(SimpleCoins.useSQL) {
                                            SimpleCoins.sqlManager.closeConnection();
                                            SimpleCoins.sqlManager.connect();
                                        } else {
                                            fileConfiguration.save(SimpleCoins.configFile);
                                            fileConfiguration = YamlConfiguration.loadConfiguration(SimpleCoins.configFile);
                                            CoinManager.saveFiles();
                                            CoinManager.loadFiles();
                                        }

                                        p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.Reload")));
                                    } catch (SQLException | IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    p.sendMessage(SimpleCoins.colorize(PREFIX + "No need to reload. Vault is handling everything."));
                                }
                            }
                        } else {
                            p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                            return true;
                        }
                    } /*else if(args[0].equalsIgnoreCase("sync")) {
                        if(p.hasPermission(Perms.SYNC.perm())) {
                            if(args.length == 2) {

                                int mode = 2;
                                try {
                                    mode = Integer.parseInt(args[1]);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }

                                if(mode == 0 || mode == 1) {
                                    CoinManager.sync(mode);
                                } else {
                                    p.sendMessage(SimpleCoins.colorize(PREFIX + "&cMode must be &20 &cor &21&c!"));
                                }
                            }
                        }
                    }*/
                } else {
                    p.sendMessage(SimpleCoins.colorize(PREFIX + fileConfiguration.getString("Messages.NoPermission")));
                    return true;
                }
            }
        }

        return false;
    }
}