package com.johnymuffin.serverinformation.beta.routes.api.v1;

import com.johnymuffin.serverinformation.beta.routes.JServerInformationRoute;
import com.legacyminecraft.poseidon.Poseidon;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;

public class JServerInformationTPSRoute extends JServerInformationRoute {

    @Override
    protected void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // Initiate asynchronous context
        final AsyncContext ctxt = request.startAsync();

        // Execute the task asynchronously in the context of the Bukkit server
        ctxt.start(() -> {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.JServerInformation, () -> {
                try {
                    JSONObject responseObject = new JSONObject();
                    JSONArray tpsArray = new JSONArray();

                    // Get the last 15 minutes of TPS records
                    LinkedList<Double> tpsRecords = Poseidon.getTpsRecords();
                    // Ensure we don't exceed the size of the list
                    int recordsToFetch = Math.min(tpsRecords.size(), 900);

                    // Adding TPS records to the JSON array


                    responseObject.put("error", false);
                    responseObject.put("tps_records", tpsArray); // Include the TPS data for the last 15 minutes

                    // Provide last 5 seconds, 30 seconds, 1 minute, 5 minutes, 10 minutes, and 15 minutes TPS averages
                    responseObject.put("tps_5s", calculateAverage(tpsRecords, 5));
                    responseObject.put("tps_30s", calculateAverage(tpsRecords, 30));
                    responseObject.put("tps_1m", calculateAverage(tpsRecords, 60));
                    responseObject.put("tps_5m", calculateAverage(tpsRecords, 300));
                    responseObject.put("tps_10m", calculateAverage(tpsRecords, 600));
                    responseObject.put("tps_15m", calculateAverage(tpsRecords, 900));

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

    private double calculateAverage(LinkedList<Double> records, int seconds) {
        int size = Math.min(records.size(), seconds);
        if (size == 0) return 20.0;

        double total = 0;
        for (int i = 0; i < size; i++) {
            total += records.get(i);
        }
        return total / size;
    }
}