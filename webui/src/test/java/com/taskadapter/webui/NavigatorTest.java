package com.taskadapter.webui;

import com.taskadapter.FileManager;
import com.taskadapter.auth.BasicCredentialsManager;
import com.taskadapter.auth.CredentialsManager;
import com.taskadapter.auth.cred.CredentialsStore;
import com.taskadapter.auth.cred.FSCredentialStore;
import com.taskadapter.connector.testlib.FileBasedTest;
import com.taskadapter.license.LicenseManager;
import com.taskadapter.web.PluginEditorFactory;
import com.taskadapter.webui.service.EditableCurrentUserInfo;
import com.taskadapter.webui.service.EditorManager;
import com.taskadapter.webui.service.Services;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NavigatorTest extends FileBasedTest {
    @Test
    public void feedbackPageIsShownWithoutLogin() {
        Services services = getServices();
        Navigator navigator = getNavigator(services);
        navigator.show(new SupportPage("1.1", new LicenseManager(tempFolder)));
        assertEquals("support", navigator.getCurrentPage().getPageGoogleAnalyticsID());
    }

    @Test
    public void homePageRedirectsToLoginIfNotLoggedIn() {
        Navigator navigator = getNavigator();
        navigator.show(new ConfigsPage());
        assertEquals("login", navigator.getCurrentPage().getPageGoogleAnalyticsID());
    }

    private Navigator getNavigator() {
        return getNavigator(getServices());
    }

    private Navigator getNavigator(Services services) {
        Window window = new Window("Task Adapter");
        VerticalLayout layout = new VerticalLayout();
        window.setContent(layout);
        final CredentialsStore cs = new FSCredentialStore(services.getFileManager());
        final CredentialsManager cm = new BasicCredentialsManager(cs, 50);
        return new Navigator(layout, services, cm, new Authenticator(cm, new EditableCurrentUserInfo()));
    }

    private Services getServices() {
        final FileManager fm = new FileManager(tempFolder);
        final CredentialsStore cs = new FSCredentialStore(fm);
        final CredentialsManager cm = new BasicCredentialsManager(cs, 50);
        return new Services(fm, new EditorManager(Collections.<String,PluginEditorFactory<?>>emptyMap()), cm);
    }

}
