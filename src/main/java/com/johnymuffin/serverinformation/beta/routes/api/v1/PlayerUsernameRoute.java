package com.johnymuffin.serverinformation.beta.routes.api.v1;

import com.johnymuffin.serverinformation.beta.routes.JServerInformationRoute;
import com.projectposeidon.api.PoseidonUUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class PlayerUsernameRoute extends JServerInformationRoute {

    @Override
    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String uuidParameter = request.getParameter("uuid");

        if (uuidParameter == null || uuidParameter.trim().isEmpty()) {
            this.returnErrorJSON(response, HttpServletResponse.SC_BAD_REQUEST, "uuid is required");
            return;
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(uuidParameter);
        } catch (IllegalArgumentException e) {
            this.returnErrorJSON(response, HttpServletResponse.SC_BAD_REQUEST, "uuid must be a valid UUID");
            return;
        }

        final AsyncContext ctxt = request.startAsync();
        ctxt.start(() -> Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.JServerInformation, () -> {
            try {
                String username = getUsernameFromUUID(uuid);

                if (username == null) {
                    this.returnErrorJSON(response, HttpServletResponse.SC_NOT_FOUND, "Username not found for UUID");
                    ctxt.complete();
                    return;
                }

                JSONObject responseObject = new JSONObject();
                responseObject.put("error", false);
                responseObject.put("uuid", uuid.toString());
                responseObject.put("username", username);

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

    private static String getUsernameFromUUID(UUID uuid) {
        Player player = getPlayerFromUUID(uuid);
        if (player != null) {
            return player.getName();
        }
        return PoseidonUUID.getPlayerUsernameFromUUID(uuid);
    }

    private static Player getPlayerFromUUID(UUID uuid) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid)) {
                return player;
            }
        }
        return null;
    }
}
