package com.taskadapter.webui;

import com.taskadapter.config.TAFile;
import com.taskadapter.webui.license.LicensePage;
import com.taskadapter.webui.service.Services;
import com.taskadapter.webui.service.UpdateManager;
import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Skorokhodov
 */
public class Navigator {
    public static final String TASKS = "tasks_list";
    public static final String HOME = "home";
    public static final String LICENSE_PAGE = "license";
    public static final String FEEDBACK_PAGE = "feedback";
    public static final String NEW_CONFIG_PAGE = "new_config_page";
    public static final String CONFIGURE_TASK_PAGE = "configure_task";

    private static final String TASK_DETAILS_PAGE = "task_details";
    private static final String LOGIN_PAGE = "login_page";
    private static final String DELETE_PAGE = "delete_task";

    private Map<String, Page> pages = new HashMap<String, Page>();
    private MenuLinkBuilder menuLinkBuilder;

    private HorizontalLayout navigationPanel;
    private HorizontalLayout currentComponentArea = new HorizontalLayout();
    private HorizontalLayout mainArea = new HorizontalLayout();
    private VerticalLayout layout;
    private Services services;
    private Label updateMessage;

    public Navigator(VerticalLayout layout, Services services) {
        this.layout = layout;
        this.services = services;
        menuLinkBuilder = new MenuLinkBuilder(this);
        registerPages();
        buildUI();
        checkLastAvailableVersion();
    }

    private void registerPages() {
        registerPage(TASKS, new TasksPage());
        registerPage(LOGIN_PAGE, new LoginPage());
        registerPage(HOME, new HomePage());
        registerPage(LICENSE_PAGE, new LicensePage());
        registerPage(FEEDBACK_PAGE, new SupportPage());
        registerPage(NEW_CONFIG_PAGE, new NewConfigPage());
        registerPage(CONFIGURE_TASK_PAGE, new ConfigureTaskPage());
        registerPage(TASK_DETAILS_PAGE, new TaskDetailsPage());
        registerPage(DELETE_PAGE, new DeletePage());
    }

    private void buildUI() {
        Header header = new Header();
        header.setHeight("50px");
        header.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        layout.addComponent(header);

        navigationPanel = new HorizontalLayout();
        navigationPanel.setHeight("30px");
        navigationPanel.setWidth("100%");
        navigationPanel.setSpacing(true);
        layout.addComponent(navigationPanel);

        updateMessage = new Label();
        layout.addComponent(updateMessage);
        layout.setComponentAlignment(updateMessage, Alignment.MIDDLE_CENTER);


        LeftMenu leftMenu = new LeftMenu(this);
        leftMenu.setWidth("120px");
        mainArea.addComponent(leftMenu);

        currentComponentArea.setSizeFull();

        mainArea.addComponent(currentComponentArea);
        mainArea.setExpandRatio(currentComponentArea, 1.0f);
        mainArea.setSizeFull();
        layout.addComponent(mainArea);
        layout.setComponentAlignment(mainArea, Alignment.TOP_LEFT);
        layout.setExpandRatio(mainArea, 1.0f);      // use all available space
    }

    public void registerPage(String id, Page page) {
        pages.put(id, page);
    }

    public void show(String pageId) {
        Page page = pages.get(pageId);
        if (page != null) {
            show(page);
        } else {
            showError("Internal error!", "Page \"" + pageId + "\" is not registered");
        }
    }

    public void show(Page page) {
        setServicesToPage(page);
        /* // TODO: uncomment for production
            if (!authenticator.isLoggedIn()) {
                show(pages.get(LOGIN_PAGE));
            } else {
                show(page);
            }
        */
        currentComponentArea.removeAllComponents();
        Component ui = page.getUI();
        currentComponentArea.addComponent(ui);
        currentComponentArea.setComponentAlignment(ui, Alignment.TOP_LEFT);

        navigationPanel.removeAllComponents();

        navigationPanel.addComponent(menuLinkBuilder.createButtonLink("Home", HOME));
        navigationPanel.addComponent(new Label(page.getPageTitle()));
    }

    private void setServicesToPage(Page page) {
        page.setNavigator(this);
        page.setServices(services);
    }

    // TODO these 3 showXX methods are not in line with the other show(). refactor!
    public void showConfigureTaskPage(TAFile file) {
        ConfigureTaskPage page = (ConfigureTaskPage) pages.get(CONFIGURE_TASK_PAGE);
        page.setFile(file);
        show(page);
    }

    public void showTaskDetailsPage(TAFile file) {
        TaskDetailsPage page = (TaskDetailsPage) pages.get(TASK_DETAILS_PAGE);
        page.setFile(file);
        show(page);
    }

    public void showDeleteFilePage(TAFile file) {
        DeletePage page = (DeletePage) pages.get(DELETE_PAGE);
        page.setFile(file);
        show(page);
    }

    private void checkLastAvailableVersion() {
        UpdateManager updateManager = new UpdateManager();

        if (updateManager.isCurrentVersionOutdated()) {
            updateMessage.setCaption("There's a newer version of Task Adapter available for download. Your version: "
                    + updateManager.getCurrentVersion() + ". Last available version: " + updateManager.getLatestAvailableVersion());
        }
    }

    public void showError(String caption, String message) {
        layout.getWindow().showNotification(caption, "<pre>" + message + "</pre>", Window.Notification.TYPE_ERROR_MESSAGE);
    }

    public void showNotification(String caption, String message) {
        layout.getWindow().showNotification(caption, "<pre>" + message + "</pre>", Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }

    public void addWindow(MessageDialog messageDialog) {
        layout.getApplication().addWindow(messageDialog);
    }

    public Application getApplication() {
        return layout.getApplication();
    }
}
