package com.johnymuffin.serverinformation.beta.routes.api.v1;

import com.johnymuffin.serverinformation.beta.ChatMessage;
import com.johnymuffin.serverinformation.beta.routes.JServerInformationRoute;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JServerInformationChatRoute extends JServerInformationRoute {

    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws IOException {
        //Get StartUnixTime from request parameter if it exists
        long startUnixTime = 0;

        if (request.getParameter("startUnixTime") != null) {
            String startUnixTimeString = request.getParameter("startUnixTime");
            //Make sure startUnixTime is a long
            try {
                startUnixTime = Long.parseLong(startUnixTimeString);
            } catch (NumberFormatException e) {
                //Throw error
                this.returnErrorJSON(response, HttpServletResponse.SC_BAD_REQUEST, "startUnixTime must be a valid long");
                return;
            }
        }

        //Change to async
        final AsyncContext ctxt = request.startAsync();
        long finalStartUnixTime = startUnixTime;
        ctxt.start(() -> {
            //Change to Bukkit Synchronised Task
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.JServerInformation, () -> {
                try {
                    JSONObject responseJson = new JSONObject();

                    JSONArray chatMessages = new JSONArray();

                    //Loop through all recent chat messages
                    for (int i = 0; i < this.JServerInformation.getChatMessages().size(); i++) {
                        //Get chat message
                        ChatMessage chatMessage = this.JServerInformation.getChatMessages().get(i);

                        //Check if chat message is newer than startUnixTime
                        if (chatMessage.getTimestamp() > finalStartUnixTime) {
                            //Add chat message to chatMessages
                            chatMessages.add(i, chatMessage.toJSON());
                        }
                    }

                    responseJson.put("error", false);
                    responseJson.put("messages", chatMessages);
                    responseJson.put("unixTime", System.currentTimeMillis() / 1000L);
                    responseJson.put("startUnixTime", finalStartUnixTime);

                    //Send response
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().println(responseJson.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctxt.complete();
            });

        });
    }

    ;
}
