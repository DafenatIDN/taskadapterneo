package com.taskadapter.web;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.taskadapter.web.service.Services;

/**
 * @author Alexey Skorokhodov
 */
public interface PluginEditorFactory {
    Descriptor getDescriptor();

    ConfigEditor createEditor(ConnectorConfig config, Services services);
    
    /**
     * Requests to format a plugin error. If error is not supported (not a 
     * custom error), this method may safelly return <code>null</code>.
     * @param e error to format.
     * @return formatted error.
     */
    public String formatError(Throwable e);
}
