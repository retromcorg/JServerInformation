package com.johnymuffin.serverinformation.beta;

import com.johnymuffin.serverinformation.beta.commands.CommandHideCoords;
import com.johnymuffin.serverinformation.beta.routes.api.v1.ChatRoute;
import com.johnymuffin.serverinformation.beta.routes.api.v1.ExecuteCommand;
import com.johnymuffin.serverinformation.beta.routes.api.v1.PlayerUUIDRoute;
import com.johnymuffin.serverinformation.beta.routes.api.v1.PlayerUsernameRoute;
import com.johnymuffin.serverinformation.beta.routes.api.v1.PlayersRoute;
import com.johnymuffin.beta.webapi.JWebAPI;
import com.johnymuffin.beta.webapi.event.JWebAPIDisable;
import com.johnymuffin.serverinformation.beta.routes.api.v1.TPSRoute;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JServerInformation extends JavaPlugin implements Listener {

    private static JServerInformation plugin;
    private String pluginName;
    private Logger log;
    private PluginDescriptionFile pdf;

    private boolean errored = false;

    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();

    private boolean apiEnabled = false;

    private CommandSender commandSender;

    private Config config;

    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());

        //Load config
        config = new Config(new java.io.File(plugin.getDataFolder(), "config.yml"));

        //Check for JWebAPI
        if (Bukkit.getPluginManager().getPlugin("JWebAPI") == null) {
            logger(Level.SEVERE, "JWebAPI is not installed, disabling plugin.");
            errored = true;
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        commandSender = new CommandSender(Bukkit.getServer());
        getCommand("hidecoords").setExecutor(new CommandHideCoords());


        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if(!Bukkit.getPluginManager().isPluginEnabled("JWebAPI")) {
                    logger(Level.SEVERE, "JWebAPI is not enabled, disabling plugin.");
                    errored = true;
                    Bukkit.getPluginManager().disablePlugin(plugin);
                    return;
                }


                //Register API routes
                JWebAPI jWebAPI = (JWebAPI) Bukkit.getPluginManager().getPlugin("JWebAPI");
                jWebAPI.registerRoute(PlayersRoute.class, "/api/v1/server/players");
                jWebAPI.registerRoute(ChatRoute.class, "/api/v1/server/chat");
                jWebAPI.registerRoute(TPSRoute.class, "/api/v1/server/tps");
                jWebAPI.registerRoute(PlayerUUIDRoute.class, "/api/v1/server/player/uuid");
                jWebAPI.registerRoute(PlayerUsernameRoute.class, "/api/v1/server/player/username");
                // Only register the execute command route if enabled in the config
                if(config.getConfigBoolean("api.command.execute.enable")) {
                    jWebAPI.registerRoute(ExecuteCommand.class, "/api/v1/server/execute");
                }
                apiEnabled = true;
                logger(Level.INFO, "Registered API routes");
            }
        });

        Bukkit.getPluginManager().registerEvents(plugin, plugin);

        log.info("[" + pluginName + "] Has Loaded, Version: " + pdf.getVersion());
    }

    @Override
    public void onDisable() {
        log.info("[" + pluginName + "] Is Unloading, Version: " + pdf.getVersion());

        if (!errored) {
            removeAPIRoutes();
        }

        log.info("[" + pluginName + "] Has Unloaded, Version: " + pdf.getVersion());
    }

    public void removeAPIRoutes() {
        if (apiEnabled) {
            try {
                JWebAPI jWebAPI = (JWebAPI) Bukkit.getPluginManager().getPlugin("JWebAPI");
                jWebAPI.unregisterServlets(ChatRoute.class);
                jWebAPI.unregisterServlets(PlayersRoute.class);
                jWebAPI.unregisterServlets(TPSRoute.class);
                jWebAPI.unregisterServlets(PlayerUUIDRoute.class);
                jWebAPI.unregisterServlets(PlayerUsernameRoute.class);
                if(config.getConfigBoolean("api.command.execute.enable")) {
                    jWebAPI.unregisterServlets(ExecuteCommand.class);
                }
                logger(Level.INFO, "Unregistered API routes");
            } catch (Exception e) {
                logger(Level.WARNING, "Failed to unregister API routes");
                e.printStackTrace();
            }
            apiEnabled = false;
        }
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }

    public Config getConfig() {
        return config;
    }

    public void logger(Level level, String message) {
        Bukkit.getLogger().log(level, "[" + plugin.getDescription().getName() + "] " + message);
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Monitor)
    public void onPlayerChat(PlayerChatEvent event) {
        chatMessages.add(new ChatMessage(event));

        //Delete messages older than 5 minutes (300 seconds)
        chatMessages.removeIf(chatMessage -> chatMessage.getTimestamp() < (System.currentTimeMillis() / 1000L) - 300);
    }

    @EventHandler
    public void onCustomEvent(final Event customEvent) {
        if (!apiEnabled) return;
        if (!(customEvent instanceof JWebAPIDisable)) return;
        //Remove API routes if enabled and JStoreDisableEvent is called
        logger(Level.INFO, "JWebAPI disabled, removing API routes");
        removeAPIRoutes();
    }


    public ArrayList<ChatMessage> getChatMessages() {
        return chatMessages;
    }
}
