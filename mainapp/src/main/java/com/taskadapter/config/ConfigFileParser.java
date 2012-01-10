package com.taskadapter.config;

import com.google.gson.Gson;
import com.taskadapter.PluginManager;
import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Descriptor;

/**
 * @author Alexey Skorokhodov
 */
public class ConfigFileParser {
    private static final String LINE0_PREFIX = "ta.name=";
    private static final String LINE1_PREFIX = "ta.connector1.id=";
    private static final String LINE2_PREFIX = "ta.connector1.data=";
    private static final String LINE3_PREFIX = "ta.connector2.id=";
    private static final String LINE4_PREFIX = "ta.connector2.data=";

    private PluginManager pluginManager;

    public ConfigFileParser(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public TAConfig parse(String fileContents) {
        String lines[] = fileContents.split("\\r?\\n");

        String name = lines[0].substring(LINE0_PREFIX.length());
        String connector1ID = lines[1].substring(LINE1_PREFIX.length());
        String connector1DataString = lines[2].substring(LINE2_PREFIX.length());
        ConnectorConfig config1 = createConfig(connector1ID, connector1DataString);

        String connector2ID = lines[3].substring(LINE3_PREFIX.length());
        String connector2DataString = lines[4].substring(LINE4_PREFIX.length());
        ConnectorConfig config2 = createConfig(connector2ID, connector2DataString);

        TAConnectorDescriptor desc1 = new TAConnectorDescriptor(connector1ID,
                config1);
        TAConnectorDescriptor desc2 = new TAConnectorDescriptor(connector2ID,
                config2);

        return new TAConfig(name, desc1, desc2);
    }

    private ConnectorConfig createConfig(String pluginId, String dataString) {
        Descriptor descriptor = pluginManager.getDescriptor(pluginId);
        if (descriptor == null) {
            throw new RuntimeException("Connector with ID " + pluginId + " is not found.");
        }
        Class<ConnectorConfig> configClass = (Class<ConnectorConfig>) descriptor.getConfigClass();
        Gson gson = new Gson();
        return gson.fromJson(dataString, configClass);
    }

    public String convertToJSonString(TAConfig config) {
        Gson gson = new Gson();

        String line0 = LINE0_PREFIX + config.getName();

        String line1 = LINE1_PREFIX + config.getConnector1().getType();
        Object data1 = config.getConnector1().getData();
        String line2 = LINE2_PREFIX + gson.toJson(data1);

        String line3 = LINE3_PREFIX + config.getConnector2().getType();
        Object data2 = config.getConnector2().getData();
        String line4 = LINE4_PREFIX + gson.toJson(data2);

        return line0 + "\n" + line1 + "\n" + line2 + "\n" + line3
                + "\n" + line4;
    }

}