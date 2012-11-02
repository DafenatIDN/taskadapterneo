package com.taskadapter.webui.export;

import com.taskadapter.config.StorageException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GTask;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.ButtonBuilder;
import com.taskadapter.webui.ConfigsPage;
import com.taskadapter.webui.Page;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Action page. Always perform action from connector1 to connector2.
 */
public abstract class ActionPage extends Page {
    private final Logger logger = LoggerFactory.getLogger(ActionPage.class);

    protected final VerticalLayout mainPanel;

    protected ProgressIndicator loadProgress = new ProgressIndicator();
    protected ProgressIndicator saveProgress = new ProgressIndicator();
    protected List<GTask> loadedTasks;
    private ConfirmExportFragment confirmExportFragment;
    protected final UISyncConfig config;

    protected ActionPage(UISyncConfig config) {
        this.config = config;
        mainPanel = new VerticalLayout();
        mainPanel.setSpacing(true);
        mainPanel.setMargin(true);
        buildInitialPage();
    }

    protected abstract void saveData(List<GTask> tasks) throws ConnectorException;

    protected abstract void loadData() throws ConnectorException;

    protected abstract String getInitialText();

    protected abstract String getNoDataLoadedText();

    protected abstract VerticalLayout getDoneInfoPanel();

    private void buildInitialPage() {
        Label label = createLabel(getInitialText());
        label.setContentMode(Label.CONTENT_XHTML);
        mainPanel.addComponent(label);

        Button goButton = new Button(MESSAGES.get("button.go"));
        goButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                synchronized (navigator.getApplication()) {
                    buildLoadingPage();
                }
                new LoadWorker().start();
            }
        });
        mainPanel.addComponent(goButton);

        Button cancelButton = ButtonBuilder.createBackButton(navigator, MESSAGES.get("button.cancel"));
        mainPanel.addComponent(cancelButton);
    }

    protected void buildLoadingPage() {
        mainPanel.removeAllComponents();
        loadProgress = new ProgressIndicator();
        loadProgress.setIndeterminate(true);
        loadProgress.setPollingInterval(200);
        mainPanel.addComponent(loadProgress);
        String sourceDescription = config.getConnector1().getSourceLocation()
                + " (" + config.getConnector1().getLabel() + ")";
        String labelText = MESSAGES.format("action.loadingData", sourceDescription);
        mainPanel.addComponent(createLabel(labelText));
    }

    protected void showAfterDataLoaded() {
        loadProgress.setEnabled(false);
        if (loadedTasks == null || loadedTasks.isEmpty()) {
            mainPanel.addComponent(createLabel(getNoDataLoadedText()));
            mainPanel.addComponent(ButtonBuilder.createBackButton(navigator, MESSAGES.get("button.back")));
        } else {
            buildConfirmationUI();
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setWidth(800, Sizeable.UNITS_PIXELS);
        return label;
    }

    private class LoadWorker extends Thread {
        @Override
        public void run() {
            try {
                loadData();
            } catch (ConnectorException e) {
                e.printStackTrace();
                // TODO add "connector error" dialog
            } catch (Throwable t) {
                t.printStackTrace();
                // TODO Show "internal error" dialog
            }
            // must synchronize changes over application
            synchronized (navigator.getApplication()) {
                showAfterDataLoaded();
            }
        }
    }

    private class SaveWorker extends Thread {
        private List<GTask> tasks;

        private SaveWorker(List<GTask> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            try {
                saveData(tasks);
            } catch (ConnectorException e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            // must synchronize changes over application
            synchronized (navigator.getApplication()) {
                showAfterExport();
            }
        }
    }

    private void showAfterExport() {
        mainPanel.removeAllComponents();
        mainPanel.addComponent(getDoneInfoPanel());

        Button button = new Button("Back to home page");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigator.show(new ConfigsPage());
            }
        });

        mainPanel.addComponent(button);
    }

    private void saveConfigIfChanged() {
        if (confirmExportFragment.needToSaveConfig()) {
            String userLoginName = services.getAuthenticator().getUserName();
            try {
                services.getUIConfigStore().saveConfig(userLoginName, config);
            } catch (StorageException e) {
                logger.error("Can't save the updated config: " + e.toString(), e);
                // TODO !! report in the UI
            }
        }
    }

    protected void buildConfirmationUI() {
        confirmExportFragment = new ConfirmExportFragment(MESSAGES, navigator, loadedTasks,
                config, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveConfigIfChanged();
                startSaveTasksProcess();
            }
        });
        mainPanel.addComponent(confirmExportFragment);
        mainPanel.setExpandRatio(confirmExportFragment, 1f); // use all available space
    }

    protected void startSaveTasksProcess() {
        List<GTask> selectedRootLevelTasks = confirmExportFragment.getSelectedRootLevelTasks();

        if (!loadedTasks.isEmpty()) {
            saveProgress = new ProgressIndicator();
            saveProgress.setIndeterminate(false);
            saveProgress.setEnabled(true);
            saveProgress.setCaption(MESSAGES.format("action.saving", config.getConnector2().getDestinationLocation()));
            mainPanel.removeAllComponents();
            mainPanel.addComponent(saveProgress);

            new SaveWorker(selectedRootLevelTasks).start();
        } else {
            mainPanel.getWindow().showNotification(MESSAGES.get("action.pleaseSelectTasks"));
        }
    }

    @Override
    public Component getUI() {
        return mainPanel;
    }
}
