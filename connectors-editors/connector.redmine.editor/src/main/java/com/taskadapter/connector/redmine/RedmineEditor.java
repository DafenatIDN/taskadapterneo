package com.taskadapter.connector.redmine;

import com.taskadapter.connector.definition.ConnectorConfig;
import com.taskadapter.connector.definition.WebServerInfo;
import com.taskadapter.web.configeditor.ConfigEditor;
import com.taskadapter.web.configeditor.EditorUtil;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import org.redmine.ta.RedmineManager;
import org.redmine.ta.beans.Project;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alexey Skorokhodov
 */
public class RedmineEditor extends ConfigEditor implements LoadProjectJobResultListener {

    private static final String USE_API = "Use API Access Key";
    private static final String USE_LOGIN = "Use Login and Password";
    private static final String DEFAULT_USE = USE_LOGIN;

    private RedmineConfig config;
    private TextField serverURL;
    private PasswordField redmineAPIKey;
    private TextField login;
    private PasswordField password;
    private TextField projectKey;
    private TextField defaultTaskType;
    private TextField queryId;

    private static final List<String> authOptions = Arrays.asList(new String[]{USE_API, USE_LOGIN});

    private OptionGroup authOptionsGroup = new OptionGroup("Authorization", authOptions);

    public RedmineEditor(ConnectorConfig config) {
        this.config = (RedmineConfig) config;

        buildUI();
        addSaveRelationSection();
        addFieldsMappingPanel(RedmineDescriptor.instance.getAvailableFieldsProvider(), config.getFieldsMapping());

        setData();
    }

    private void buildUI() {
        serverURL = new TextField("Redmine URL:");
        serverURL.setInputPrompt("http://myserver:3000/myredminelocation");
        addComponent(serverURL);

        authOptionsGroup.setNullSelectionAllowed(false);
        authOptionsGroup.setImmediate(true);
        authOptionsGroup.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                System.out.println("disable");
            }
        });
        authOptionsGroup.select(DEFAULT_USE);
        addComponent(authOptionsGroup);

        redmineAPIKey = new PasswordField("API access key:");
        addComponent(redmineAPIKey);

        login = new TextField();
        login.setCaption("Login");
        addComponent(login);

        password = new PasswordField("Password");
        addComponent(password);

        projectKey = new TextField("Project Key");
        addComponent(projectKey);

        queryId = new TextField("Query ID");
        addComponent(queryId);

        defaultTaskType = new TextField("Default task type");
        addComponent(defaultTaskType);

        addFindUsersByNameElement();
    }

    private void setData() {
        serverURL.setValue(config.getServerInfo().getHost());
        WebServerInfo serverInfo = config.getServerInfo();
        setIfNotNull(serverURL, serverInfo.getHost());
        setIfNotNull(redmineAPIKey, serverInfo.getApiKey());
        setIfNotNull(login, serverInfo.getUserName());
        setIfNotNull(password, serverInfo.getPassword());
        authOptionsGroup.select(serverInfo.isUseAPIKeyInsteadOfLoginPassword());
        authOptionsGroup.select(!serverInfo.isUseAPIKeyInsteadOfLoginPassword());

        setIfNotNull(projectKey, config.getProjectKey());
        setIfNotNull(defaultTaskType, config.getDefaultTaskType());
        setIfNotNull(findUserByName, config.getFindUserByName());
        if (config.getQueryId() != null) {
            queryId.setValue(String.valueOf(config.getQueryId()));
        }
    }

    @Override
    public ConnectorConfig getPartialConfig() {
        RedmineConfig rmConfig = new RedmineConfig();
        WebServerInfo serverInfo = new WebServerInfo((String) serverURL.getValue(),
                (String) login.getValue(), (String) password.getValue());
        serverInfo.setApiKey((String) redmineAPIKey.getValue());
        serverInfo.setUseAPIKeyInsteadOfLoginPassword(isAPIOptionSelected());
        rmConfig.setServerInfo(serverInfo);

        rmConfig.setProjectKey((String) projectKey.getValue());
        rmConfig.setDefaultTaskType((String) defaultTaskType.getValue());
        rmConfig.setFindUserByName((Boolean) findUserByName.getValue());
        if (!((String) queryId.getValue()).isEmpty()) {
            rmConfig.setQueryId(Integer.parseInt((String) queryId.getValue()));
        }
        return rmConfig;
    }

    private boolean isAPIOptionSelected() {
        return authOptionsGroup.getValue().equals(USE_API);
    }

    private void addSaveRelationSection() {
        CheckBox saveRelations = new CheckBox("Save issue relations (follows/precedes)");
        addComponent(saveRelations);
    }

    RedmineManager getRedmineManager() {
        RedmineManager mgr;
        if (isAPIOptionSelected()) {
            mgr = new RedmineManager((String) serverURL.getValue(), (String) redmineAPIKey.getValue());
        } else {
            mgr = new RedmineManager((String) serverURL.getValue(),
                    (String) login.getValue(), (String) password.getValue());
        }
        return mgr;
    }

    @Override
    public void notifyProjectLoaded(Project project) {
        String msg = "Key:  " + project.getIdentifier()
                + "\nName: " + project.getName()
                + "\nCreated: " + project.getCreatedOn()
                + "\nUpdated: " + project.getUpdatedOn();
        msg += addNullSafe("Homepage", project.getHomepage());
        msg += addNullSafe("Description", project.getDescription());
        EditorUtil.show(getWindow(), "Project Info", msg);
    }

    private String addNullSafe(String label, String fieldValue) {
        String msg = "\n" + label + ": ";
        if (fieldValue != null) {
            msg += fieldValue;
        }
        return msg;
    }
}