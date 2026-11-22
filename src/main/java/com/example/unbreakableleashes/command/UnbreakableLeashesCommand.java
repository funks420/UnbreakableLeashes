package com.ranull.unbreakableleashes.command;

import com.ranull.unbreakableleashes.UnbreakableLeashes;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnbreakableLeashesCommand implements CommandExecutor, TabExecutor {
    private final UnbreakableLeashes plugin;

    public UnbreakableLeashesCommand(UnbreakableLeashes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String string, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.GOLD + "UnbreakableLeashes " + ChatColor.DARK_GRAY
                    + ChatColor.RESET + ChatColor.DARK_GRAY + "v" + plugin.getDescription().getVersion());

            commandSender.sendMessage(ChatColor.GOLD + "/unbreakableleashes " + ChatColor.DARK_GRAY + "-"
                    + ChatColor.RESET + " Plugin info");

            if (commandSender.hasPermission("unbreakableleashes.reload")) {
                commandSender.sendMessage(ChatColor.GOLD + "/unbreakableleashes reload " + ChatColor.DARK_GRAY + "-"
                        + ChatColor.RESET + " Reload plugin");
            }

            commandSender.sendMessage(ChatColor.DARK_GRAY + "Author: " + ChatColor.GOLD + "Ranull");
        } else if (args[0].equals("reload")) {
            if (commandSender.hasPermission("unbreakableleashes.reload")) {
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                commandSender.sendMessage(ChatColor.GOLD + "UnbreakableLeashes" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "Reloaded config file.");
            } else {
                commandSender.sendMessage(ChatColor.GOLD + "UnbreakableLeashes" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "No permission.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                      @NotNull String string, @NotNull String @NotNull [] args) {
        if (commandSender.hasPermission("unbreakableleashes.reload")) {
            return Collections.singletonList("reload");
        }

        return new ArrayList<>();
    }
}
