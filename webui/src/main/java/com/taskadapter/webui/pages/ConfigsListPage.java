package com.taskadapter.webui.pages;

import com.taskadapter.web.uiapi.ConfigId;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.BasePage;
import com.taskadapter.webui.ConfigOperations;
import com.taskadapter.webui.EventTracker;
import com.taskadapter.webui.Layout;
import com.taskadapter.webui.Page;
import com.taskadapter.webui.SessionController;
import com.taskadapter.webui.service.Preservices;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = Navigator.CONFIGS_LIST, layout = Layout.class)
@RouteAlias(value = "", layout = Layout.class)
@CssImport(value = "./styles/views/mytheme.css")
public class ConfigsListPage extends BasePage {
    private final Logger log = LoggerFactory.getLogger(ConfigsListPage.class);

    private VerticalLayout configsLayout;
    private TextField filterField;

    private final List<UISyncConfig> configs = new ArrayList<>();

    public ConfigsListPage() {
        buildUI();
    }

    public void buildUI() {
        HorizontalLayout actionPanel = new HorizontalLayout();
        actionPanel.setWidth("100%");
        actionPanel.setSpacing(true);
        Button addButton = new Button(Page.message("configsPage.buttonNewConfig"),
                event -> Navigator.newConfig());
        actionPanel.add(addButton);

        HorizontalLayout filterPanel = new HorizontalLayout();
        this.filterField = new TextField();
        filterField.addKeyPressListener(e -> filterFields(filterField.getValue()));
        filterPanel.add(new Label(Page.message("configsPage.filter")));
        filterPanel.add(filterField);
        filterPanel.setSpacing(true);
        actionPanel.add(filterPanel);
        //  actionPanel.setComponentAlignment(filterPanel, Alignment.MIDDLE_RIGHT)

        //  configsTopLevelLayout.setComponentAlignment(configsLayout, Alignment.TOP_CENTER)

        HorizontalLayout configsTopLevelLayout = new HorizontalLayout();
        configsTopLevelLayout.setSpacing(true);
        configsTopLevelLayout.setSizeFull();

        configsLayout = new VerticalLayout();
        configsLayout.setWidth("100%");
        configsTopLevelLayout.add(configsLayout);

        setSpacing(true);

        add(
                actionPanel,
                configsTopLevelLayout);
    }

    @Override
    protected void beforeEnter() {
        EventTracker.trackPage("configs_list");
        refreshConfigs();
    }

    public void refreshConfigs() {
        configs.clear();
        ConfigOperations configOps = SessionController.buildConfigOperations();

        if (showAllConfigs()) {
            configs.addAll(configOps.getManageableConfigs());
        } else {
            configs.addAll(configOps.getOwnedConfigs());
        }
        Collections.sort(configs, Comparator.comparing(UISyncConfig::getOwnerName));

        setDisplayedConfigs(configs);
        filterFields(filterField.getValue());
    }

    private boolean showAllConfigs() {
        Preservices services = SessionController.getServices();
        return services.settingsManager.adminCanManageAllConfigs() &&
                SessionController.getUserContext().authorizedOps.canManagerPeerConfigs();
    }

    private void setDisplayedConfigs(List<UISyncConfig> dispConfigs) {
        configsLayout.removeAll();
        dispConfigs.forEach(config -> {
            configsLayout.add(createConfigComponent(config));
        });
    }


    private Component createConfigComponent(UISyncConfig config) {
        HorizontalLayout layout = new HorizontalLayout();
        String configLabel = config.label();
        Button link = new Button(configLabel,
                e -> showConfigPanel(config.configId()));
        layout.add(link);
        return layout;
    }

    private void filterFields(String filterStr) {
        String[] words;

        if (filterStr == null) {
            words = new String[0];
        } else {
            words = filterStr.toLowerCase().split(" +");
        }
        List<UISyncConfig> filteredConfigs = configs.stream().filter(config -> matches(config, words)).collect(Collectors.toList());
        setDisplayedConfigs(filteredConfigs);
    }

    private boolean matches(UISyncConfig config, String[] filters) {
        if (filters.length == 0) {
            return true;
        }

        DisplayMode displayMode = showAllConfigs() ? DisplayMode.ALL_CONFIGS : DisplayMode.OWNED_CONFIGS;

        for (String name : filters) {
            String confName = displayMode.nameOf(config);
            if (!confName.toLowerCase().contains(name)
                    && !config.getConnector1().getLabel().toLowerCase().contains(name)
                    && !config.getConnector2().getLabel().toLowerCase().contains(name)) {
                return false;
            }
        }
        return true;
    }

    private void showConfigPanel(ConfigId configId) {
        ConfigOperations configOps = SessionController.buildConfigOperations();

        Option<UISyncConfig> maybeConfig = configOps.getConfig(configId);
        if (maybeConfig.isEmpty()) {
            log.error("Cannot find config with id " + configId + "to show in the UI. It may have been deleted already");
            return;
        }
        getUI().ifPresent(ui -> ui.navigate("config/" + configId.id()));
    }

    /**
     * Comparator for configuration files.
     */
    private static final class ConfigComparator implements
            Comparator<UISyncConfig> {
        /**
         * Name of the current user.
         */
        private final String userName;

        ConfigComparator(String userName) {
            this.userName = userName;
        }

        @Override
        public int compare(UISyncConfig o1, UISyncConfig o2) {
            final boolean isMyConfig1 = userName.equals(o1.getOwnerName());
            final boolean isMyConfig2 = userName.equals(o2.getOwnerName());

            if (isMyConfig1 != isMyConfig2)
                return isMyConfig1 ? -1 : 1;

            final int ucomp = o1.getOwnerName().compareTo(o2.getOwnerName());
            if (ucomp != 0)
                return ucomp;

            return o1.getLabel().compareTo(o2.getLabel());
        }

    }

}