package com.taskadapter.connector.jira;

import com.taskadapter.connector.definition.PluginFactory;
import com.taskadapter.model.NamedKeyedObject;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.taskadapter.web.configeditor.LookupOperation;

import java.util.List;

public class LoadIssueTypesOperation extends LookupOperation {

    public LoadIssueTypesOperation(ConfigEditor editor, PluginFactory factory) {
        super(editor, factory);
    }

    @Override
    protected List<? extends NamedKeyedObject> loadData() throws Exception {
        JiraConnector jira = (JiraConnector) connector;
        return jira.getIssueTypes();
    }

}