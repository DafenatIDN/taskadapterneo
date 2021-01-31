package com.taskadapter.webui;

import com.taskadapter.app.GoogleAnalyticsFactory;
import com.taskadapter.webui.pages.ConfigsListPage;
import com.taskadapter.webui.pages.SchedulesListPage;
import com.taskadapter.webui.pages.SupportPage;
import com.taskadapter.webui.pages.UserProfilePage;
import com.taskadapter.webui.results.ExportResultsListPage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.vaadin.googleanalytics.tracking.EnableGoogleAnalytics;
import org.vaadin.googleanalytics.tracking.TrackerConfiguration;
import org.vaadin.googleanalytics.tracking.TrackerConfigurator;

import java.util.ArrayList;
import java.util.List;

import static com.taskadapter.webui.Page.message;

/**
 * The main view is a top-level placeholder for other views.
 * <p>
 * RouterLayout and TrackerConfigurator are required for GoogleAnalytics.
 */
//@JsModule("./styles/shared-styles.js")
@Push // "push" is required to get reliable UI updates from background threads (like tasks loading)
@Theme(Lumo.class)
@CssImport("./styles/views/taskadapter-main.css")
@PWA(name = "My Project", shortName = "My Project", enableInstallPrompt = false)
@EnableGoogleAnalytics(value = GoogleAnalyticsFactory.GOOGLE_ANALYTICS_ID)
public class Layout extends AppLayout implements RouterLayout, TrackerConfigurator {

    private static Tab configsListTab = createTab(VaadinIcon.EDIT, "Configs List", ConfigsListPage.class);
    private static Tab schedulesListTab = createTab(VaadinIcon.CLOCK, "Schedules", SchedulesListPage.class);
    private static Tab configureTab = createTab(VaadinIcon.TOOLS, "Configure", ConfigureSystemPage.class);
    private static Tab resultsTab = createTab(VaadinIcon.LIST, message("headerMenu.results"), ExportResultsListPage.class);
    private static Tab supportTab = createTab(VaadinIcon.QUESTION, "Support", SupportPage.class);
    private static Tab profileTab = createTab(VaadinIcon.USER, "Profile", UserProfilePage.class);

    private Span viewTitle;
    private Tabs menuTabs;

    public Layout() {
        addToNavbar(true, createHeaderContent());
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setHeight("70px");
        layout.setId("header");
        layout.getThemeList().set("light", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        viewTitle = new Span("TaskAdapter");
        viewTitle.addClassName("taskAdapterLogo");
        layout.expand(viewTitle); // get white space after the logo, before the menu items

        menuTabs = createMenuTabs();
        layout.setFlexGrow(2, menuTabs);
        layout.add(viewTitle, menuTabs);

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        refreshTabsInMenu();
    }

    private static Tabs createMenuTabs() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        return tabs;
    }

    private void refreshTabsInMenu() {
        menuTabs.removeAll();
        menuTabs.add(getAvailableTabs());
    }

    private static Tab[] getAvailableTabs() {
        List<Tab> tabs = new ArrayList<>();
        if (SessionController.userIsLoggedIn()) {
            tabs.add(configsListTab);
            tabs.add(resultsTab);
            tabs.add(schedulesListTab);
            tabs.add(configureTab);
            tabs.add(supportTab);
            tabs.add(profileTab);

            // is there any need for this link really?
//            String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
//            Tab logoutTab = createTab(createLogoutLink(contextPath));
//            tabs.add(logoutTab);
        }
        return tabs.toArray(new Tab[0]);
    }

    // TODO 14 not needed?
    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    private static Tab createTab(VaadinIcon icon, String title, Class<? extends Component> viewClass) {
        return createTab(populateLink(new RouterLink(null, viewClass), icon, title));
    }

    private static Tab createTab(Component content) {
        final Tab tab = new Tab();
        tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab.add(content);
        return tab;
    }

    private static Anchor createLogoutLink(String contextPath) {
        final Anchor a = populateLink(new Anchor(), VaadinIcon.ARROW_RIGHT, "Logout");
        a.setHref(contextPath + "/logout");
        return a;
    }

    private static <T extends HasComponents> T populateLink(T a, VaadinIcon icon, String title) {
        a.add(icon.create());
        a.add(title);
        return a;
    }

    @Override
    public void configureTracker(TrackerConfiguration configuration) {
        // note - this google analytics vaadin library reports "page visited" events automatically,
        // for all "Page" classes
        configuration.setCreateField("allowAnchor", Boolean.FALSE);
        configuration.setInitialValue("transport", "beacon");
    }
/*

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }
*/

//    private String getCurrentPageTitle() {
//        return getContent().getClass().getAnnotation(PageTitle.class).value();
//    }
}
