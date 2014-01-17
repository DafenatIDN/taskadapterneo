package com.taskadapter.webui.pages;

import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.web.DroppingNotSupportedException;
import com.taskadapter.web.uiapi.UIConnectorConfig;
import com.taskadapter.web.uiapi.UISyncConfig;
import com.taskadapter.webui.ImageLoader;
import com.taskadapter.webui.Page;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.Transferable;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Notification;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * Controller for a single export.
 *
 */
final class UniConfigExport {

    private static final String DROP_ICON_TOOLTIP = "Drop an MSP file here to start the export process.";

    /**
     * Unified config export callback.
     */
    public static interface Callback {
        /**
         * Performs a regular export.
         */
        public void doExport();

        /**
         * Performs a drop-in export.
         *
         * @param file
         *            file to export.
         */
        public void dropInExport(Html5File file);
    }

    private static String getDropInValidationError(UISyncConfig syncConfig)
            throws DroppingNotSupportedException {
        final StringBuilder rb = new StringBuilder();
        try {
            syncConfig.getConnector1().validateForDropIn();
        } catch (BadConfigException e) {
            rb.append(Page.MESSAGES.format("configsPage.errorSource",
                    syncConfig.getConnector1().decodeException(e)));
        }
        try {
            syncConfig.getConnector2().validateForSave();
        } catch (BadConfigException e) {
            rb.append(Page.MESSAGES.format("configsPage.errorDestination",
                    syncConfig.getConnector2().decodeException(e)));
        }
        if (rb.length() == 0) {
            return null;
        }
        return Page.MESSAGES.format("configsPage.validationTemplate",
                rb.toString());
    }

    private static String getValidationError(UISyncConfig syncConfig) {
        final StringBuilder rb = new StringBuilder();
        try {
            syncConfig.getConnector1().validateForLoad();
        } catch (BadConfigException e) {
            rb.append(Page.MESSAGES.format("configsPage.errorSource",
                    syncConfig.getConnector1().decodeException(e)));
        }
        try {
            syncConfig.getConnector2().validateForSave();
        } catch (BadConfigException e) {
            rb.append(Page.MESSAGES.format("configsPage.errorDestination",
                    syncConfig.getConnector2().decodeException(e)));
        }
        if (rb.length() == 0) {
            return null;
        }
        return Page.MESSAGES.format("configsPage.validationTemplate",
                rb.toString());
    }

    private static Label createLabel(UIConnectorConfig connector) {
        final Label res = new Label(connector.getLabel());
        res.setWidth(100, PERCENTAGE);
        return res;
    }

    private static Component renderSimple(UISyncConfig config,
            final Callback callback) {
        final HorizontalLayout res = new HorizontalLayout();
        res.setWidth(274, PIXELS);

        final String validationFailure = getValidationError(config);
        final boolean isValid = validationFailure == null;

        res.addStyleName("uniExportPanel");
        res.addStyleName(isValid ? "valid" : "invalid");

        final UIConnectorConfig config1;
        final UIConnectorConfig config2;
        final String assetName;

        if (config.isReversed()) {
            config1 = config.getConnector2();
            config2 = config.getConnector1();
            assetName = "arrow_left.png";
        } else {
            config1 = config.getConnector1();
            config2 = config.getConnector2();
            assetName = "arrow_right.png";
        }

        final Label leftLabel = createLabel(config1);
        final Label rightLabel = createLabel(config2);
        final Embedded actionLabel = new Embedded(null, ImageLoader.getImage(assetName));

        leftLabel.addStyleName("left-label");
        rightLabel.addStyleName("right-label");

        res.addComponent(leftLabel);
        res.addComponent(actionLabel);
        res.addComponent(rightLabel);

        res.setExpandRatio(leftLabel, 1.0f);
        res.setExpandRatio(rightLabel, 1.0f);
        res.setSpacing(true);

        res.setComponentAlignment(leftLabel, Alignment.MIDDLE_RIGHT);
        res.setComponentAlignment(actionLabel, Alignment.MIDDLE_CENTER);
        res.setComponentAlignment(rightLabel, Alignment.MIDDLE_LEFT);

        if (isValid) {
            res.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutClickEvent event) {
                    callback.doExport();
                }
            });
        } else {
            leftLabel.setDescription(validationFailure);
            rightLabel.setDescription(validationFailure);
            actionLabel.setDescription(validationFailure);
            res.setDescription(validationFailure);
        }

        return res;
    }
    
    private static Component wrapDropArea(final Callback callback,
            final Component dropLabel) {
        final DragAndDropWrapper dadw = new DragAndDropWrapper(dropLabel);

        dadw.setDropHandler(new DropHandler() {
            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

            @Override
            public void drop(DragAndDropEvent event) {
                final Transferable t = event.getTransferable();
                if (!(t instanceof WrapperTransferable))
                    return;
                final WrapperTransferable wt = (WrapperTransferable) t;
                if (wt.getFiles().length == 0)
                    return;
                if (wt.getFiles().length > 1) {
                    Notification.show("Multidrops are not accepted");
                    return;
                }

                callback.dropInExport(wt.getFiles()[0]);
            }
        });
        return dadw;
    }

    /**
     * Renders the config box with connectors' names, arrow and possibly "drop" icon.
     *
     * @param config   config to render.
     * @param callback action to invoke when user clicks on the config.
     */
    public static Component render(UISyncConfig config, final Callback callback) {
        final HorizontalLayout layout = new HorizontalLayout();
        final Component regularExportBox = renderSimple(config, callback);
        layout.setWidth(274, PIXELS);
        try {
            final String validationFailure = getDropInValidationError(config);
            final boolean isValid = validationFailure == null;

            final Embedded dropLabel = new Embedded(null, ImageLoader.getImage("file_drop.gif"));
            dropLabel.setDescription(DROP_ICON_TOOLTIP);

            if (!isValid) {
                dropLabel.setDescription(validationFailure);
            }
            
            dropLabel.setWidth(32, PIXELS);
            regularExportBox.setWidth(274 - 32, PIXELS);
            
            if (config.isReversed()) {
                layout.addComponent(regularExportBox);
                layout.addComponent(dropLabel);
                layout.setComponentAlignment(dropLabel, Alignment.MIDDLE_RIGHT);
            } else {
                layout.addComponent(dropLabel);
                layout.addComponent(regularExportBox);
                layout.setComponentAlignment(dropLabel, Alignment.MIDDLE_LEFT);
            }
            layout.setExpandRatio(regularExportBox, 1f);
            layout.setExpandRatio(dropLabel, 0.0f);

            if (isValid) {
                return wrapDropArea(callback, layout);
            }
        } catch (DroppingNotSupportedException e) {
            regularExportBox.setWidth(274, PIXELS);
            layout.addComponent(regularExportBox);
        }
        return layout;
    }
}