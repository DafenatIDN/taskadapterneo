package com.taskadapter.connector.basecamp.editor;

import com.taskadapter.connector.basecamp.BasecampConfig;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.web.WindowProvider;
import com.taskadapter.web.configeditor.EditorUtil;
import com.taskadapter.web.configeditor.server.ServerPanelWithAPIKey;
import com.taskadapter.web.service.Services;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import java.util.List;

public class BasecampEditorFactory implements PluginEditorFactory<BasecampConfig> {
    @Override
    public ComponentContainer getMiniPanelContents(WindowProvider windowProvider, Services services, BasecampConfig config, List<BasecampConfig> relatedConfigs) {

        Panel panel = createServerPanel(config);
        Panel projectPanel = createProjectPanel(windowProvider, config);

        GridLayout grid = new GridLayout();
        grid.setColumns(2);
        grid.setMargin(true);
        grid.setSpacing(true);

        grid.addComponent(panel);
        grid.addComponent(projectPanel);
        return grid;
    }

    private Panel createServerPanel(BasecampConfig config) {
        Panel panel = new Panel("Server Info");
        ServerPanelWithAPIKey redmineServerPanel = new ServerPanelWithAPIKey(new MethodProperty<String>(config, "label"),
                new MethodProperty<String>(config, "serverUrl"),
                new MethodProperty<String>(config.getAuth(), "login"),
                new MethodProperty<String>(config.getAuth(), "password"),
                new MethodProperty<String>(config.getAuth(), "apiKey"),
                new MethodProperty<Boolean>(config.getAuth(), "useAPIKeyInsteadOfLoginPassword"));
        panel.addComponent(redmineServerPanel);
        return panel;
    }

    private Panel createProjectPanel(final WindowProvider windowProvider, final BasecampConfig config) {
        Panel projectPanel = new Panel("Project");

        GridLayout grid = new GridLayout();
        grid.setColumns(3);
        grid.setMargin(true);
        grid.setSpacing(true);

        projectPanel.addComponent(grid);

        Label accountIdLabel = new Label("Account Id:");
        grid.addComponent(accountIdLabel);
        TextField accountIdField = new TextField();
        accountIdField.setPropertyDataSource(new MethodProperty<String>(config, "accountId"));
        grid.addComponent(accountIdField);
        grid.addComponent(new Label(""));

        Label projectKeyLabel = new Label("Project key:");
        grid.addComponent(projectKeyLabel);
        TextField projectKeyField = new TextField();
        projectKeyField.setPropertyDataSource(new MethodProperty<String>(config, "projectKey"));
        grid.addComponent(projectKeyField);
        Button infoButton = EditorUtil.createButton("Info", "View the project info",
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        new ShowProjectElement(windowProvider, config, BasecampEditorFactory.this).loadProject();
                    }
                }
        );
        grid.addComponent(infoButton);
        grid.setComponentAlignment(infoButton, Alignment.MIDDLE_CENTER);

        Label todoListKey = new Label("Todo list key:");
        grid.addComponent(todoListKey);
        TextField todoListField = new TextField();
        todoListField.setPropertyDataSource(new MethodProperty<String>(config, "todoKey"));
        grid.addComponent(todoListField);
        grid.addComponent(new Label(""));
        return projectPanel;
    }


    @Override
    public void validateForSave(BasecampConfig config) throws BadConfigException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void validateForLoad(BasecampConfig config) throws BadConfigException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String describeSourceLocation(BasecampConfig config) {
        return "basecamp.com";
    }

    @Override
    public String describeDestinationLocation(BasecampConfig config) {
        return "basecamp.com";
    }

    @Override
    public String formatError(Throwable e) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
