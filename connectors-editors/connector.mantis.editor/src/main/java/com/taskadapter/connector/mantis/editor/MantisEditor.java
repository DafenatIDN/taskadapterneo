package com.taskadapter.connector.mantis.editor;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.mantis.MantisDescriptor;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.vaadin.ui.CheckBox;

/**
 * @author Alexey Skorokhodov
 */
public class MantisEditor extends ConfigEditor {

    public MantisEditor(ConnectorConfig config) throws Exception {
        super(config);
        buildUI();
        setData(config);
    }

    private void buildUI() {
        addServerPanel();
        addProjectPanel(this, new MantisProjectProcessor(this));
        addFindUsersByNameElement();
        addSaveRelationSection();
        addFieldsMappingPanel(MantisDescriptor.instance.getAvailableFieldsProvider(), config.getFieldsMapping());
    }

    @Override
    public ConnectorConfig getPartialConfig() {
        return config;
    }

    private void addSaveRelationSection() {
        CheckBox saveRelations = new CheckBox("Save issue relations (follows/precedes)");
        addComponent(saveRelations);
    }

}
