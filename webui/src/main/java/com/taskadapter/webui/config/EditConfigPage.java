package com.taskadapter.webui.config;

import com.taskadapter.config.StorageException;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.model.GTaskDescriptor;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.CloneDeletePanel;
import com.taskadapter.webui.ConfigsPage;
import com.taskadapter.webui.Page;
import com.taskadapter.webui.PageUtil;
import com.taskadapter.webui.data.ExceptionFormatter;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditConfigPage extends Page {
    private final Logger logger = LoggerFactory.getLogger(EditConfigPage.class);

    private VerticalLayout layout = new VerticalLayout();
    private UISyncConfig config;

    private Label errorMessageLabel = new Label("Test");
    private OnePageEditor editor;

    private void buildUI() {
        layout.removeAllComponents();
        layout.setSpacing(true);
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        Button saveButton = new Button("Save");
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                save();
            }
        });
        buttonsLayout.addComponent(saveButton);

        buttonsLayout.addComponent(PageUtil.createButton(navigator, "Cancel", new ConfigsPage()));

        errorMessageLabel.addStyleName("error-message-label");
        errorMessageLabel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        buttonsLayout.addComponent(errorMessageLabel);
        buttonsLayout.setExpandRatio(errorMessageLabel, 1.0f);

        CloneDeletePanel cloneDeletePanel = new CloneDeletePanel(services, navigator, config);
        buttonsLayout.addComponent(cloneDeletePanel);
        buttonsLayout.setComponentAlignment(cloneDeletePanel, Alignment.MIDDLE_RIGHT);
        layout.addComponent(buttonsLayout);

        HorizontalLayout descriptionLayout = new HorizontalLayout();
        descriptionLayout.setSpacing(true);
        layout.addComponent(descriptionLayout);
        descriptionLayout.addComponent(new Label("Description:"));
        TextField descriptionField = new TextField();
        descriptionField.addStyleName("configEditorDescriptionLabel");
        MethodProperty<String> label = new MethodProperty<String>(config, "label");
        descriptionField.setPropertyDataSource(label);
        descriptionLayout.addComponent(descriptionField);

        editor = new OnePageEditor(MESSAGES, services, config);
        layout.addComponent(editor);
    }

    public void setConfig(UISyncConfig config) {
        this.config = config.normalized();
    }

    private void save() {
        // TODO refactor: this method is long and ugly.
        try {
            editor.validate();
        } catch (FieldNotMappedException e) {
            String fieldDisplayName = GTaskDescriptor.getDisplayValue(e.getField());
            String s = MESSAGES.format("error.fieldSelectedForExportNotMapped", fieldDisplayName);
            errorMessageLabel.setValue(s);
            return;
        } catch (BadConfigException e) {
            String s = ExceptionFormatter.format(e);
            errorMessageLabel.setValue(s);
            return;
        }
        String userLoginName = services.getAuthenticator().getUserName();
        try {
            services.getUIConfigStore().saveConfig(userLoginName, config);
        } catch (StorageException e) {
            String message = "Can't save: " + e.getMessage();
            errorMessageLabel.setValue(message);
            logger.error(message, e);
            return;
        }
        navigator.showNotification("Saved", "All is saved OK");

        errorMessageLabel.setValue("");
        navigator.show(new ConfigsPage());
    }

    @Override
    public String getPageGoogleAnalyticsID() {
        return "edit_config";
    }

    @Override
    public Component getUI() {
        buildUI();
        return layout;
    }

    public void setErrorMessage(String errorMessage) {
        errorMessageLabel.setValue(errorMessage);
    }
}
