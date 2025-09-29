package com.johnymuffin.serverinformation.beta.routes;

import com.johnymuffin.serverinformation.beta.JServerInformation;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletResponse;

public class JServerInformationRoute extends com.johnymuffin.beta.webapi.routes.NormalRoute {

    public com.johnymuffin.serverinformation.beta.JServerInformation JServerInformation = (JServerInformation) Bukkit.getPluginManager().getPlugin("JServerInformation");

    protected void returnErrorJSON(HttpServletResponse response, int errorCode, String errorMessage) {
        response.setStatus(errorCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            JSONObject errorJSON = new JSONObject();
            errorJSON.put("error", true);
            errorJSON.put("message", errorMessage);
            response.getWriter().write(errorJSON.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
