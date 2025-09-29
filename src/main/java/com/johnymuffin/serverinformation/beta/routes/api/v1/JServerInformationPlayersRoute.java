package com.johnymuffin.serverinformation.beta.routes.api.v1;

import com.johnymuffin.beta.fundamentals.Fundamentals;
import com.johnymuffin.beta.fundamentals.player.FundamentalsPlayer;
import com.johnymuffin.serverinformation.beta.routes.JServerInformationRoute;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JServerInformationPlayersRoute extends JServerInformationRoute {

    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        //Change to async
        final AsyncContext ctxt = request.startAsync();
        ctxt.start(() -> {
            //Change to Bukkit Synchronised Task
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.JServerInformation, () -> {
                try {
                    JSONObject responseObject = new JSONObject();
                    JSONArray playerList = new JSONArray();

                    boolean useFundamentals = Bukkit.getPluginManager().getPlugin("Fundamentals") != null;

                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        JSONObject playerJSON = new JSONObject();
                        playerJSON.put("name", player.getName());
                        playerJSON.put("uuid", player.getUniqueId().toString());
                        playerJSON.put("display_name", player.getDisplayName());

                        playerJSON.put("world", player.getWorld().getName());
                        playerJSON.put("world_uuid", player.getWorld().getUID().toString());
                        playerJSON.put("world_environment", player.getWorld().getEnvironment().toString());
                        playerJSON.put("x", player.getLocation().getX());
                        playerJSON.put("y", player.getLocation().getY());
                        playerJSON.put("z", player.getLocation().getZ());

                        if (useFundamentals) {
                            Fundamentals fundamentals = (Fundamentals) Bukkit.getPluginManager().getPlugin("Fundamentals");
                            FundamentalsPlayer fundamentalsPlayer = fundamentals.getPlayerMap().getPlayer(player.getUniqueId());
                            //If player is fakequit, don't add them to the list
                            if (fundamentalsPlayer.isFakeQuit()) {
                                continue;
                            }

                            //If player is vanished fake coordinates to 0,0,0
                            if (fundamentalsPlayer.isVanished()) {
                                playerJSON.put("x", 0);
                                playerJSON.put("y", 0);
                                playerJSON.put("z", 0);
                            }
                        }
                        playerList.add(playerJSON);
                    }

                    responseObject.put("error", false);
                    responseObject.put("players", playerList);
                    responseObject.put("player_count", playerList.size());
                    responseObject.put("max_players", Bukkit.getMaxPlayers());


                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseObject.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctxt.complete();
            });
        });
    }

}
