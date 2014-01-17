package com.taskadapter.webui.pages;

import com.taskadapter.PluginManager;
import com.taskadapter.config.StorageException;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.ConfigOperations;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import java.util.Iterator;

public class NewConfigPage {
    private static final String DESCRIPTION_HINT = "(optional)";
    private static final String SYSTEM_1_TITLE = "System 1";
    private static final String SYSTEM_2_TITLE = "System 2";
    private static final String SELECT_CONNECTOR_1_MESSAGE = "Please select "
            + SYSTEM_1_TITLE;
    private static final String SELECT_CONNECTOR_2_MESSAGE = "Please select "
            + SYSTEM_2_TITLE;

    /**
     * Callback for config creation page.
     */
    public static interface Callback {
        /**
         * This method is called after new config was created.
         * 
         * @param config
         *            created config.
         */
        public void configCreated(UISyncConfig config);
    }

    private final TextField descriptionTextField;
    private final ListSelect connector1;
    private final ListSelect connector2;
    private final Panel panel;
    private final Label errorMessageLabel;
    private final ConfigOperations configOps;
    private final Callback callback;

    private NewConfigPage(PluginManager pluginManager, ConfigOperations ops,
            Callback callback) {

        this.configOps = ops;
        this.callback = callback;

        panel = new Panel("Create new config");
        panel.setWidth("600px");

        final GridLayout grid = new GridLayout(2, 4);
        grid.setSpacing(true);
        grid.setMargin(true);
        panel.setContent(grid);

        connector1 = createPluginSelector(SYSTEM_1_TITLE, pluginManager);
        grid.addComponent(connector1, 0, 0);

        connector2 = createPluginSelector(SYSTEM_2_TITLE, pluginManager);
        grid.addComponent(connector2, 1, 0);

        descriptionTextField = new TextField("Description");
        descriptionTextField.setInputPrompt(DESCRIPTION_HINT);
        descriptionTextField.setWidth("100%");
        grid.addComponent(descriptionTextField, 0, 1, 1, 1);
        grid.setComponentAlignment(descriptionTextField,
                Alignment.MIDDLE_CENTER);

        // empty label by default
        errorMessageLabel = new Label();
        errorMessageLabel.addStyleName("error-message-label");

        final Button saveButton = new Button("Create");
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveClicked();
            }
        });

        grid.addComponent(errorMessageLabel, 0, 2, 1, 2);
        grid.addComponent(saveButton, 1, 3);
        grid.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Creates a new plugin selector.
     * 
     * @param title
     *            plugin selector title.
     * @param plugins
     *            plugin manager.
     * @return plugin selector.
     */
    private static ListSelect createPluginSelector(String title,
            PluginManager plugins) {
        final ListSelect res = new ListSelect(SYSTEM_1_TITLE);
        res.setRequired(true);
        res.setNullSelectionAllowed(false);

        final Iterator<Descriptor> connectors = plugins.getPluginDescriptors();
        while (connectors.hasNext()) {
            final Descriptor connector = connectors.next();
            res.addItem(connector.getID());
            res.setItemCaption(connector.getID(), connector.getLabel());
        }

        res.setRows(res.size());
        return res;
    }

    private void saveClicked() {
        try {
            validate();
            errorMessageLabel.setValue("");
            callback.configCreated(save());
        } catch (ConnectorNotSelectedException e) {
            errorMessageLabel.setValue(e.getMessage());
        } catch (StorageException e) {
            errorMessageLabel
                    .setValue("Failed to save config in persistent store");
        }
    }

    private void validate() throws ConnectorNotSelectedException {
        if (connector1.getValue() == null) {
            connector1.setRequiredError(SELECT_CONNECTOR_1_MESSAGE);
            throw new ConnectorNotSelectedException(SELECT_CONNECTOR_1_MESSAGE);
        } else {
            connector1.setRequiredError("");
        }

        if (connector2.getValue() == null) {
            connector1.setRequiredError(SELECT_CONNECTOR_2_MESSAGE);
            throw new ConnectorNotSelectedException(SELECT_CONNECTOR_2_MESSAGE);
        } else {
            connector2.setRequiredError("");
        }
    }

    private UISyncConfig save() throws StorageException {

        final String descriptionString = descriptionTextField.getValue();
        final String id1 = (String) connector1.getValue();
        final String id2 = (String) connector2.getValue();

        return configOps.createNewConfig(descriptionString, id1, id2);
    }

    private class ConnectorNotSelectedException extends Exception {

        private static final long serialVersionUID = 1L;

        public ConnectorNotSelectedException(String string) {
            super(string);
        }
    }

    /**
     * null Renders a page for the new configs.
     * 
     * @param pluginManager
     *            plugin manager.
     * @param ops
     *            configuration operations.
     * @param callback
     *            callback for the item.
     * @return UI component.
     */
    public static Component render(PluginManager pluginManager,
            ConfigOperations ops, Callback callback) {
        return new NewConfigPage(pluginManager, ops, callback).panel;
    }
}