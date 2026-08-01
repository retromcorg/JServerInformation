package com.johnymuffin.serverinformation.beta.commands;

import com.johnymuffin.beta.fundamentals.Fundamentals;
import com.johnymuffin.beta.fundamentals.player.FundamentalsPlayer;
import com.johnymuffin.serverinformation.beta.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandHideCoords implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!(sender.hasPermission("jserverinformation.hidecoords") || sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        Plugin fundamentalsPlugin = Bukkit.getPluginManager().getPlugin("Fundamentals");
        if (!(fundamentalsPlugin instanceof Fundamentals)) {
            sender.sendMessage(ChatColor.RED + "Fundamentals is not available.");
            return true;
        }

        Player player = (Player) sender;
        Fundamentals fundamentals = (Fundamentals) fundamentalsPlugin;
        FundamentalsPlayer fundamentalsPlayer = fundamentals.getPlayerMap().getPlayer(player);

        if (args.length > 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " [on|off|status]");
            return true;
        }

        boolean hideCoordinates = !Utility.isHidingCoordinates(fundamentalsPlayer);
        if (args.length == 1) {
            String option = args[0].toLowerCase();
            if (option.equals("status")) {
                sendStatus(sender, Utility.isHidingCoordinates(fundamentalsPlayer));
                return true;
            }
            if (option.equals("on")) {
                hideCoordinates = true;
            } else if (option.equals("off")) {
                hideCoordinates = false;
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " [on|off|status]");
                return true;
            }
        }

        fundamentalsPlayer.saveInformation(Utility.HIDE_COORDS_KEY, hideCoordinates);
        sendStatus(sender, hideCoordinates);
        return true;
    }

    private void sendStatus(CommandSender sender, boolean hideCoordinates) {
        if (hideCoordinates) {
            sender.sendMessage(ChatColor.GREEN + "Your coordinates are now hidden from the server information API.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Your coordinates are now visible in the server information API.");
        }
    }
}
