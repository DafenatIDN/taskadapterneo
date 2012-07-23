package com.taskadapter.web.configeditor.file;

import com.taskadapter.connector.msp.MSPConfig;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

public class LocalModeFilePanel extends Panel {
    private static final String LABEL_FILE_NAME = "Input file:";
    private static final String TOOLTIP_FILE_NAME = "Microsoft Project file name to load the data from (MPP or XML file)";

    private static final String LABEL_OUTPUT_FILE_NAME = "Output file:";
    private static final String TOOLTIP_OUTPUT_FILE_NAME = "Microsoft Project file name to save the data to (only XML format is supported)";
    
    /**
     * Used config.
     */
    private final MSPConfig config;

    public LocalModeFilePanel(MSPConfig config) {
    	super("Microsoft project files");
    	this.config = config;
        buildUI();
    }

    private TextField inputFileNameField;
    private TextField outputFileNameField;

    private void buildUI() {
        GridLayout layout = new GridLayout();
        addComponent(layout);
        layout.setColumns(2);
        layout.setSpacing(true);

        layout.addComponent(new Label(LABEL_FILE_NAME));
        inputFileNameField = createFileName(TOOLTIP_FILE_NAME);
		inputFileNameField.setPropertyDataSource(new MethodProperty<String>(
				config, "inputAbsoluteFilePath"));
        layout.addComponent(inputFileNameField);

        layout.addComponent(new Label(LABEL_OUTPUT_FILE_NAME));
        outputFileNameField = createFileName(TOOLTIP_OUTPUT_FILE_NAME);
		outputFileNameField.setPropertyDataSource(new MethodProperty<String>(
				config, "outputAbsoluteFilePath"));
        outputFileNameField.addListener(new FieldEvents.BlurListener() {
            @Override
            public void blur(FieldEvents.BlurEvent event) {
                String val = outputFileNameField.getValue().toString();
                if(!(val.endsWith(".xml") || val.endsWith(".XML"))) {
                    outputFileNameField.setValue(val + ".xml");
                }
            }
        });
        layout.addComponent(outputFileNameField);
    }


    private TextField createFileName(String tooltip) {
        final TextField field = new TextField();
        field.setDescription(tooltip);
        field.addStyleName("msp-file-name-textfield");
        return field;
    }
}
