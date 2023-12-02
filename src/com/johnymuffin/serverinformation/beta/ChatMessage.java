package com.johnymuffin.serverinformation.beta;

import org.bukkit.event.player.PlayerChatEvent;
import org.json.simple.JSONObject;

import java.util.UUID;

public class ChatMessage {
    private final UUID uuid;
    private final String username;
    private final String displayname;
    private final String message;
    private final Long timestamp;
    private final String channel;

    //Unique code for message
    private final String code = UUID.randomUUID().toString().substring(0, 8);

    public ChatMessage(UUID uuid, String username, String displayname, String message, Long timestamp, String channel) {
        this.uuid = uuid;
        this.username = username;
        this.displayname = displayname;
        this.message = message;
        this.timestamp = timestamp;
        this.channel = channel;
    }

    public ChatMessage(PlayerChatEvent event) {
        this.uuid = event.getPlayer().getUniqueId();
        this.username = event.getPlayer().getName();
        this.displayname = event.getPlayer().getDisplayName();
        this.message = event.getMessage();
        this.timestamp = System.currentTimeMillis() / 1000L;
        this.channel = "global";
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("uuid", uuid.toString());
        json.put("username", username);
        json.put("display_name", displayname);
        json.put("message", message);
        json.put("timestamp", timestamp);
        json.put("channel", channel);
        json.put("code", code);
        return json;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getMessage() {
        return message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getChannel() {
        return channel;
    }

    public String getCode() {
        return code;
    }
}
