package com.taskadapter.webui.pageset;

import com.google.common.io.Files;
import com.taskadapter.Constants;
import com.taskadapter.license.LicenseManager;
import com.taskadapter.web.event.ApplicationActionEvent;
import com.taskadapter.web.event.ApplicationActionEventWithValue;
import com.taskadapter.web.event.ConfigCreateCompleted;
import com.taskadapter.web.event.EventBusImpl;
import com.taskadapter.web.event.PageShown;
import com.taskadapter.web.event.ShowAllExportResultsRequested;
import com.taskadapter.web.event.ShowConfigPageRequested;
import com.taskadapter.web.event.ShowConfigsListPageRequested;
import com.taskadapter.web.service.Sandbox;
import com.taskadapter.web.uiapi.ConfigId;
import com.taskadapter.web.uiapi.SetupId;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.ConfigCategory$;
import com.taskadapter.webui.ConfigureSystemPage;
import com.taskadapter.webui.EventTracker;
import com.taskadapter.webui.Header;
import com.taskadapter.webui.HeaderMenuBuilder;
import com.taskadapter.webui.Page;
import com.taskadapter.webui.SessionController;
import com.taskadapter.webui.Sizes;
import com.taskadapter.webui.TAPageLayout;
import com.taskadapter.webui.Tracker;
import com.taskadapter.webui.UserContext;
import com.taskadapter.webui.config.NewSetupPage;
import com.taskadapter.webui.config.SetupsListPage;
import com.taskadapter.webui.export.ExportResultsFragment;
import com.taskadapter.webui.license.LicenseFacade;
import com.taskadapter.webui.pages.AppUpdateNotificationComponent;
import com.taskadapter.webui.pages.ConfigPage;
import com.taskadapter.webui.pages.ConfigsListPage;
import com.taskadapter.webui.pages.DropInExportPage;
import com.taskadapter.webui.pages.LicenseAgreementPage;
import com.taskadapter.webui.pages.NewConfigPage;
import com.taskadapter.webui.pages.SchedulesListPage;
import com.taskadapter.webui.pages.SupportPage;
import com.taskadapter.webui.pages.UserProfilePage;
import com.taskadapter.webui.results.ExportResultFormat;
import com.taskadapter.webui.results.ExportResultsListPage;
import com.taskadapter.webui.service.Preservices;
//import com.vaadin.server.StreamVariable;
//import com.vaadin.server.VaadinSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.lang.scala.Subscriber;
import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.runtime.BoxedUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.function.Function;

import static com.taskadapter.webui.Page.message;

/**
 * Pageset for logged-in user.
 */
public class LoggedInPageset {
    private static final Logger log = LoggerFactory.getLogger(LoggedInPageset.class);

    /**
     * Global (app-wide) services.
     */
    private final Preservices services;

    /**
     * Context for current (logged-in) user.
     */
    private final UserContext context;

    /**
     * License facade.
     */
    private final LicenseFacade license;

    /**
     * Ui component.
     */
    private final Component ui;

    /**
     * Area for the current page.
     */
    private final HorizontalLayout currentComponentArea = new HorizontalLayout();

    private final ConfigsListPage configsListPage;

    // TODO 14 not used
    /**
     * @param services           used services.
     * @param ctx                context for active user.
     */
    private LoggedInPageset(Preservices services, UserContext ctx) {
        this.services = services;
        this.context = ctx;
        this.license = new LicenseFacade(services.licenseManager);

        final Component header = Header.render(() -> {}, createMenu(), license.isLicensed());

        currentComponentArea.setWidth(Sizes.mainWidth());
        this.ui = TAPageLayout.layoutPage(header, new AppUpdateNotificationComponent(), currentComponentArea);
        this.configsListPage = new ConfigsListPage();
        registerEventListeners();
    }

    // TODO 14 not used
    private void registerEventListeners() {
        // temporary code to catch and re-throw "tracker" events
        Tracker tracker = SessionController.getTracker();
        EventBusImpl.observable(PageShown.class)
                .subscribe(new Subscriber<PageShown>() {
                    @Override
                    public void onNext(PageShown value) {
                        tracker.trackPage(value.pageName());
                    }
                });
        EventBusImpl.observable(ApplicationActionEvent.class)
                .subscribe(new Subscriber<ApplicationActionEvent>() {
                    @Override
                    public void onNext(ApplicationActionEvent value) {
                        tracker.trackEvent(value.category(), value.action(), value.label());
                    }
                });

        EventBusImpl.observable(ApplicationActionEventWithValue.class)
                .subscribe(new Subscriber<ApplicationActionEventWithValue>() {
                    @Override
                    public void onNext(ApplicationActionEventWithValue value) {
                        tracker.trackEvent(value.category(), value.action(), value.label(), value.value());
                    }
                });

        EventBusImpl.observable(ShowConfigsListPageRequested.class)
                .subscribe(new Subscriber<ShowConfigsListPageRequested>() {
                    @Override
                    public void onNext(ShowConfigsListPageRequested value) {
                        showConfigsList();
                    }
                });

        EventBusImpl.observable(ShowAllExportResultsRequested.class)
                .subscribe(new Subscriber<ShowAllExportResultsRequested>() {
                    @Override
                    public void onNext(ShowAllExportResultsRequested value) {
                        showExportResults(value.configId());
                    }
                });

        EventBusImpl.observable(ConfigCreateCompleted.class)
                .subscribe(new Subscriber<ConfigCreateCompleted>() {
                    @Override
                    public void onNext(ConfigCreateCompleted value) {
                        Option<UISyncConfig> maybeConfig = context.configOps.getConfig(value.configId());
                        if (maybeConfig.isEmpty()) {
                            throw new RuntimeException("The newly created config with id " + value.configId() +
                                    " cannot be found. This is weird.");
                        }
                        UISyncConfig config = maybeConfig.get();
                        EventTracker.trackEvent(ConfigCategory$.MODULE$, "created",
                                config.connector1().getConnectorTypeId() + " - " + config.connector2().getConnectorTypeId());
//                    showConfigsList();
                    }
                });
    }

    /**
     * Creates a self-management menu.
     */
    private Component createSelfManagementMenu() {
        HorizontalLayout layout = new HorizontalLayout(
                HeaderMenuBuilder.createButton(
                        message("headerMenu.userProfile"),
                        this::showUserProfilePage));
        layout.setSpacing(true);
        return layout;
    }

    private void showUserProfilePage() {
        applyUI(new UserProfilePage());
    }

    private Component createMenu() {
        final HorizontalLayout menu = new HorizontalLayout();
        menu.setSpacing(true);
//        menu.add(HeaderMenuBuilder.createButton(message("headerMenu.configs"),
//                this::showConfigsList));

        menu.add(HeaderMenuBuilder.createButton(message("headerMenu.schedules"),
                this::showSchedules));

        menu.add(HeaderMenuBuilder.createButton(message("headerMenu.results"),
                this::showAllResults));

        menu.add(HeaderMenuBuilder.createButton(message("headerMenu.configure"),
                this::showSystemConfiguration));
        menu.add(HeaderMenuBuilder.createButton(message("headerMenu.support"),
                this::showSupport));

        return menu;
    }

    private void showAllResults() {
        ExportResultsListPage page = new ExportResultsListPage(showExportResultsJava());
        Seq<ExportResultFormat> results = services.exportResultStorage.getSaveResults();
        page.showResults(JavaConverters.seqAsJavaList(results));
        EventTracker.trackPage("all_results");
        applyUI(page);
    }

    private void showSchedules() {
        applyUI(new SchedulesListPage());
    }

    /**
     * Shows a support page.
     */
    private void showSupport() {
        applyUI(new SupportPage());
    }

    /**
     * Shows a license agreement page.
     */
    private void showLicensePage() {
        EventTracker.trackPage("license_agreement");
//        applyUI(LicenseAgreementPage.render(services.settingsManager,
//                this::showHome));
    }

    private void showConfigsList() {
        configsListPage.refreshConfigs();
        EventTracker.trackPage("configs_list");
        applyUI(configsListPage);
    }

    private void showResult(ExportResultFormat result) {
        ExportResultsFragment fragment = new ExportResultsFragment(
                services.settingsManager.isTAWorkingOnLocalMachine());
        Component component = fragment.showExportResult(result);
        applyUI(component);
    }

    private void showExportResults(ConfigId configId) {
        ExportResultsListPage exportResultsListPage = new ExportResultsListPage(showExportResultsJava());
        Seq<ExportResultFormat> results = services.exportResultStorage.getSaveResults(configId);
        exportResultsListPage.showResults(JavaConverters.seqAsJavaList(results));
        applyUI(exportResultsListPage);
    }

    private Function<ExportResultFormat, Void> showExportResultsJava() {
        return (result) -> {
            showResult(result);
            return null;
        };
    }

    private void showHome() {
        showConfigsList();
    }

    private Sandbox createSandbox() {
        return new Sandbox(services.settingsManager.isTAWorkingOnLocalMachine(), context.configOps.syncSandbox());
    }

    private void showSystemConfiguration() {
        applyUI(new ConfigureSystemPage());
    }

/*
    private void dropIn(final UISyncConfig config, final Html5File file) {
        String fileExtension = Files.getFileExtension(file.getFileName());
        final File df = services.tempFileManager.nextFile(fileExtension);
        file.setStreamVariable(new StreamVariable() {
            @Override
            public void streamingStarted(StreamingStartEvent event) {
            }

            @Override
            public void streamingFinished(StreamingEndEvent event) {
                final VaadinSession ss = VaadinSession.getCurrent();
                ss.lock();
                try {
                    final int maxTasks = services.licenseManager
                            .isSomeValidLicenseInstalled() ? Constants.maxTasksToLoad()
                            : LicenseManager.TRIAL_TASKS_NUMBER_LIMIT;
                    EventTracker.trackPage("drop_in");
                    Component component = DropInExportPage.render(
                            services.exportResultStorage,
                            context.configOps, config,
                            maxTasks, services.settingsManager
                                    .isTAWorkingOnLocalMachine(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    df.delete();
                                    showHome();
                                }
                            }, df);
                    applyUI(component);
                } finally {
                    ss.unlock();
                }
            }

            @Override
            public void streamingFailed(StreamingErrorEvent event) {
                final VaadinSession ss = VaadinSession.getCurrent();
                ss.lock();
                try {
                    Notification.show(Page.message("uploadFailure", event.getException().toString()));
                } finally {
                    ss.unlock();
                }
                df.delete();
            }

            @Override
            public void onProgress(StreamingProgressEvent event) {
                log.debug("Safely ignoring 'progress' event. We don't need it.");
            }

            @Override
            public boolean listenProgress() {
                return false;
            }

            @Override
            public boolean isInterrupted() {
                return false;
            }

            @Override
            public OutputStream getOutputStream() {
                try {
                    return new FileOutputStream(df);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("File not found.", e);
                }
            }
        });
    }
*/

    // TODO TA3 file based connector - MSP
  /*  private void processFile(final UISyncConfig config,
            FileBasedConnector connectorTo) {
        if (!connectorTo.fileExists()) {
            exportCommon(config, exportDirection);
            return;
        }

        final String fileName = new File(
                connectorTo.getAbsoluteOutputFileName()).getName();
        final MessageDialog messageDialog = new MessageDialog(
                message("export.chooseOperation"),
                Page.message("export.fileAlreadyExists", fileName),
                Arrays.asList(message("export.update"),
                        message("export.overwrite"),
                        message("button.cancel")),
                new MessageDialog.Callback() {
                    public void onDialogResult(String answer) {
                        processSyncAction(config, answer);
                    }
                });
        messageDialog.setWidth(465, PIXELS);

        ui.getUI().addWindow(messageDialog);
    }

    private void processSyncAction(UISyncConfig config, String action) {
        if (action.equals(message("button.cancel"))) {
            return;
        }
        if (action.equals(message("export.update"))) {
            startUpdateFile(config);
        } else {
            exportCommon(config, exportDirection);
        }
    }
*/

/*
    private void startUpdateFile(UISyncConfig config) {
        tracker.trackPage("update_file");
        final int maxTasks = services.licenseManager
                .isSomeValidLicenseInstalled() ? MAX_TASKS_TO_LOAD
                : LicenseManager.TRIAL_TASKS_NUMBER_LIMIT;
        applyUI(UpdateFilePage.render(context.configOps, config, maxTasks,
                this::showHome));
    }
*/

    private void applyUI(Component ui) {
        currentComponentArea.removeAll();
        currentComponentArea.add(ui);
//        currentComponentArea.setComponentAlignment(ui, Alignment.TOP_CENTER);
    }

    /**
     * Creates a new pageset for logged-in user.
     *
     * @param services       global services.
     * @param ctx            Context for active user.
     * @return pageset UI.
     */
/*    public static Component createPageset(Preservices services, UserContext ctx) {
        final LoggedInPageset ps = new LoggedInPageset(services,
                ctx);
        if (services.settingsManager.isLicenseAgreementAccepted())
            ps.showHome();
        else
            ps.showLicensePage();
        return ps.ui;
    }*/
}
