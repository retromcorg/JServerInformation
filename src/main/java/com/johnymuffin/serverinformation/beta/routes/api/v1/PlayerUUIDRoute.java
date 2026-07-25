package com.johnymuffin.serverinformation.beta.routes.api.v1;

import com.johnymuffin.serverinformation.beta.routes.JServerInformationRoute;
import com.projectposeidon.api.PoseidonUUID;
import com.projectposeidon.api.UUIDType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class PlayerUUIDRoute extends JServerInformationRoute {

    @Override
    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String username = request.getParameter("username");

        if (username == null || username.trim().isEmpty()) {
            this.returnErrorJSON(response, HttpServletResponse.SC_BAD_REQUEST, "username is required");
            return;
        }

        final AsyncContext ctxt = request.startAsync();
        ctxt.start(() -> Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.JServerInformation, () -> {
            try {
                UUID uuid = getUUIDFromUsername(username);

                if (uuid == null) {
                    this.returnErrorJSON(response, HttpServletResponse.SC_NOT_FOUND, "UUID not found for username");
                    ctxt.complete();
                    return;
                }

                JSONObject responseObject = new JSONObject();
                responseObject.put("error", false);
                responseObject.put("username", username);
                responseObject.put("uuid", uuid.toString());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(responseObject.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctxt.complete();
        }));
    }

    private static UUID getUUIDFromUsername(String name) {
        Player player = getPlayerFromString(name);
        if (player != null) {
            return player.getUniqueId();
        }

        UUIDType uuidType = PoseidonUUID.getPlayerUUIDCacheStatus(name);
        switch (uuidType) {
            case ONLINE:
                return PoseidonUUID.getPlayerUUIDFromCache(name, true);
            case OFFLINE:
                return PoseidonUUID.getPlayerUUIDFromCache(name, false);
            default:
                return null;
        }
    }

    private static Player getPlayerFromString(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            return player;
        }
        return Bukkit.getPlayer(name);
    }
}
