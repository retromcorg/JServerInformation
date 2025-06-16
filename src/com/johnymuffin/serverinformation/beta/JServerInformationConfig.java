package com.johnymuffin.serverinformation.beta;

import org.bukkit.util.config.Configuration;

import java.io.File;


public class JServerInformationConfig extends Configuration {

    public JServerInformationConfig(File settingsFile) {
        super(settingsFile);
        this.reload();
    }

    private String generateRandomKey() {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int randomChar = (int) (Math.random() * 26) + 97; // a-z
            key.append((char) randomChar);
        }
        return key.toString();
    }

    private void write() {
        //Main
        generateConfigOption("config-version", 1);

        //Execution Command Settings
        generateConfigOption("api.command.execute.info", "This setting is used to enable or disable the execution of commands via the API.");
        generateConfigOption("api.command.execute.enable", false);
        generateConfigOption("api.command.execute.key", generateRandomKey());


    }

    public void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }


    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }


    //Getters End


    public Long getConfigLongOption(String key) {
        if (this.getConfigOption(key) == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(this.getProperty(key)));
    }


    private boolean convertToNewAddress(String newKey, String oldKey) {
        if (this.getString(newKey) != null) {
            return false;
        }
        if (this.getString(oldKey) == null) {
            return false;
        }
        System.out.println("Converting Config: " + oldKey + " to " + newKey);
        Object value = this.getProperty(oldKey);
        this.setProperty(newKey, value);
        this.removeProperty(oldKey);
        return true;

    }


    private void reload() {
        this.load();
        this.write();
        this.save();
    }
}
