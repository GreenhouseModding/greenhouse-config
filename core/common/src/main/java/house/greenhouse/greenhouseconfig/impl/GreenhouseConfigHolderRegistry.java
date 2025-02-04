package house.greenhouse.greenhouseconfig.impl;

import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;

import java.util.HashMap;
import java.util.Map;

public class GreenhouseConfigHolderRegistry {
    public static final Map<String, GreenhouseConfigHolder<?>> SERVER_CONFIG_HOLDERS = new HashMap<>();
    public static final Map<String, GreenhouseConfigHolder<?>> CLIENT_CONFIG_HOLDERS = new HashMap<>();

    public static void registerServerConfig(String configName, GreenhouseConfigHolder<?> config) {
        if (SERVER_CONFIG_HOLDERS.containsKey(configName)) {
            throw new IllegalArgumentException("A config named '" + configName + "' has already been registered on the server.");
        }
        SERVER_CONFIG_HOLDERS.put(configName, config);
    }

    public static void registerClientConfig(String configName, GreenhouseConfigHolder<?> config) {
        if (CLIENT_CONFIG_HOLDERS.containsKey(configName)) {
            throw new IllegalArgumentException("A config for '" + configName + "' has already been registered on the client.");
        }
        CLIENT_CONFIG_HOLDERS.put(configName, config);
    }
}
