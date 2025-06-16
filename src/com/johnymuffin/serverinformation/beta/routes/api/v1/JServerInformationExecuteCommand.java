package com.johnymuffin.serverinformation.beta.routes.api.v1;

import com.johnymuffin.serverinformation.beta.CommandSender;
import com.johnymuffin.serverinformation.beta.routes.JServerInformationRoute;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class JServerInformationExecuteCommand extends JServerInformationRoute {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String apiKey = request.getHeader("Authorization");

        String VALID_API_KEY = this.JServerInformation.getConfig().getConfigString("api.command.execute.key");

        if (apiKey == null || !apiKey.equals(VALID_API_KEY)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": true, \"message\": \"Invalid API key\"}");
            return;
        }

        // Read body
        String body = new BufferedReader(request.getReader()).lines().collect(Collectors.joining("\n"));

        JSONObject responseObject = new JSONObject();
        JSONArray results = new JSONArray();

        try {
            JSONArray commands = (JSONArray) new JSONParser().parse(body);

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.JServerInformation, () -> {
                CommandSender console = this.JServerInformation.getCommandSender();

                for (Object commandObj : commands) {
                    if (commandObj instanceof String) {
                        String command = (String) commandObj;
                        boolean result = Bukkit.dispatchCommand(console, command);

                        JSONObject commandResult = new JSONObject();
                        commandResult.put("command", command);
                        commandResult.put("executed", result);
                        results.add(commandResult);
                    }
                }

                responseObject.put("error", false);
                responseObject.put("results", results);

                try {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(responseObject.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": true, \"message\": \"Invalid JSON payload\"}");
        }
    }
}
