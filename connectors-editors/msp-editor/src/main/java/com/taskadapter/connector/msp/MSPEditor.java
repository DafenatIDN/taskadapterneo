package com.taskadapter.connector.msp;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.web.FileManager;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.taskadapter.web.configeditor.EditorUtil;
import com.taskadapter.web.configeditor.file.ServerModeFilePanel;
import com.taskadapter.web.service.Services;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * @author Alexey Skorokhodov
 */
public class MSPEditor extends ConfigEditor {

    private static final String XML_SUFFIX_LOWERCASE = ".xml";
//    private static final String MPP_SUFFIX_LOWERCASE = ".mpp";

    private static final String LABEL_FILE_NAME = "Input File name:";
    private static final String TOOLTIP_FILE_NAME = "Microsoft Project file name to load the data from (MPP or XML file)";

    private static final String LABEL_OUTPUT_FILE_NAME = "Output file name:";
    private static final String TOOLTIP_OUTPUT_FILE_NAME = "Microsoft Project file name to save the data to (only XML format is supported)";

    private TextField inputFileNameField;
    private TextField outputFileNameField;
    private TextField durationText;
    private TextField workText;

    public MSPEditor(ConnectorConfig config, Services services) {
        super(config, services);
        buildUI();
        addFieldsMappingPanel(MSPDescriptor.instance.getAvailableFieldsProvider());
        setData(config);
        setMSPDataToForm();
    }

    private void buildUI() {
        GridLayout internalStuffGroup = new GridLayout();
        internalStuffGroup.setCaption("MSP Text Fields to use for some internal stuff");
        internalStuffGroup.addStyleName("bordered-panel");
        internalStuffGroup.setMargin(true);
        internalStuffGroup.setSpacing(true);

        durationText = new TextField("Store 'Duration undefined' flag as:");
        durationText.addStyleName("msp-editor-textfield");
        durationText.setEnabled(false);
        internalStuffGroup.addComponent(durationText);

        workText = new TextField("Store 'Work undefined' flag as:");
        workText.addStyleName("msp-editor-textfield");
        workText.setEnabled(false);
        internalStuffGroup.addComponent(workText);

        addComponent(internalStuffGroup);
        createFilePanel();

        GridLayout fileNamesPanel = new GridLayout();
        fileNamesPanel.addStyleName("bordered-panel");
        fileNamesPanel.setMargin(true);
        fileNamesPanel.setSpacing(true);

        inputFileNameField = createFileName(LABEL_FILE_NAME, TOOLTIP_FILE_NAME);
        inputFileNameField.addListener(new FieldEvents.BlurListener() {
            public void blur(FieldEvents.BlurEvent event) {
                showXMLFieldIfNeeded();
            }
        });
        outputFileNameField = createFileName(LABEL_OUTPUT_FILE_NAME, TOOLTIP_OUTPUT_FILE_NAME);

        fileNamesPanel.addComponent(inputFileNameField);
        fileNamesPanel.addComponent(outputFileNameField);
        addComponent(fileNamesPanel);
    }

    private void createFilePanel() {
        addComponent(services.getSettingsManager().isLocal() ? createLocalModeFilePanel() : createRemoteModeFilePanel());
    }

    private TextField createFileName(String label, String tooltip) {
        final TextField field = new TextField(label);
        field.setDescription(tooltip);
        field.addStyleName("msp-file-name-textfield");
        field.addListener(new FieldEvents.BlurListener() {
            public void blur(FieldEvents.BlurEvent event) {
                addXMLExtensionIfNeeded(field);
            }
        });
        return field;
    }

    /**
     * Add ".xml" extension to the MSP file name if it's not there yet.
     */
    private void addXMLExtensionIfNeeded(TextField field) {
        String value = (String) field.getValue();
        if (!value.toLowerCase().endsWith(MSPFileReader.XML_SUFFIX_LOWERCASE)
                && (!value.toLowerCase().endsWith(MSPFileReader.MPP_SUFFIX_LOWERCASE))
                && (!value.isEmpty())) {
            field.setValue(value + XML_SUFFIX_LOWERCASE);
        }
    }

    private void showXMLFieldIfNeeded() {
        String outputFileNameString = (String) inputFileNameField.getValue();
        String fileNameLowercase = ((String) inputFileNameField.getValue()).toLowerCase();
        if (fileNameLowercase.endsWith(MSPFileReader.MPP_SUFFIX_LOWERCASE)) {
            outputFileNameString = createXMLFileNameForMPP((String) inputFileNameField.getValue());
        }
        outputFileNameField.setValue(outputFileNameString);
    }

    private String createXMLFileNameForMPP(String text) {
        String fileNameWithoutMPPExtension = text.substring(0, text.length() - MSPFileReader.MPP_SUFFIX_LOWERCASE.length());
        return fileNameWithoutMPPExtension + MSPFileReader.XML_SUFFIX_LOWERCASE;
    }

    private AbstractComponent createLocalModeFilePanel() {
        return new Label("Input and output file names (on your local machine)");
    }

    private AbstractComponent createRemoteModeFilePanel() {
        // TODO fix or delete
        return new ServerModeFilePanel(services.getAuthenticator());
        /*new UploadListener() {
            @Override
            public void fileUploaded(String file) {
                inputFileNameField.setValue(new FileManager().getFullFileNameOnServer(file));
            }
        }*/
    }

    private void setMSPDataToForm() {
        durationText.setValue(MSXMLFileWriter.FIELD_DURATION_UNDEFINED.toString());
        workText.setValue(MSXMLFileWriter.FIELD_WORK_UNDEFINED.toString());
        MSPConfig mspConfig = (MSPConfig) config;
        EditorUtil.setNullSafe(inputFileNameField, mspConfig.getInputFileName());
        EditorUtil.setNullSafe(outputFileNameField, mspConfig.getOutputFileName());
    }


    @Override
    public ConnectorConfig getPartialConfig() {
        MSPConfig mspConfig = new MSPConfig();
        mspConfig.setInputFileName(getFileName(inputFileNameField));
        mspConfig.setOutputFileName(getFileName(outputFileNameField));
        return mspConfig;
    }

    private String getFileName(TextField fileNameField) {
        String enteredFileName = (String) fileNameField.getValue();
        if (services.getSettingsManager().isLocal()) {
            return enteredFileName;
        } else {
            return new FileManager().getFullFileNameOnServer(enteredFileName);
        }
    }
}
