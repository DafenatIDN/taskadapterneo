package com.taskadapter.webui;

import com.taskadapter.util.MyIOUtils;
import com.vaadin.data.Property;
import com.vaadin.ui.*;

import java.io.IOException;

public class LicenseAgreementPage extends Page {

    private static final String AGREEMENT_TITLE = "License Agreement";
    private static final String AGREEMENT_FILE_NAME = "license.html";
    private static final String AGREEMENT_FILE_NOT_FOUND = "License agreement not found.";
    private static final String ACCEPT_CHECKBOX = "I have read the license agreement and I accept it.";
    private static final String ACCEPT_BUTTON = "Accept";

    private static final String FORM_WIDTH = "700px";
    private static final String AGREEMENT_PANEL_HEIGHT = "500px";

    private Panel panel = new Panel(AGREEMENT_TITLE);
    private Button acceptButton;

    public LicenseAgreementPage() {
        buildUI();
    }

    private void buildUI() {
        final Panel agreementPanel = new Panel();
        agreementPanel.setWidth(FORM_WIDTH);
        agreementPanel.setHeight(AGREEMENT_PANEL_HEIGHT);

        Label agreementContent;
        try {
            String licenseAgreementText = MyIOUtils.convertStreamToString(MyIOUtils.getResourceAsStream(AGREEMENT_FILE_NAME));
            agreementContent = new Label(licenseAgreementText);
        } catch (IOException e) {
            agreementContent = new Label(AGREEMENT_FILE_NOT_FOUND);
        }
        agreementContent.setContentMode(Label.CONTENT_XHTML);
        agreementPanel.addComponent(agreementContent);
        panel.addComponent(agreementPanel);

        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setWidth(FORM_WIDTH);
        actionLayout.setMargin(true, false, false, false);

        CheckBox acceptCheckbox = new CheckBox(ACCEPT_CHECKBOX);
        acceptCheckbox.setValue(false);
        acceptCheckbox.setImmediate(true);
        acceptCheckbox.addListener(new CheckBox.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                acceptButton.setEnabled((Boolean) valueChangeEvent.getProperty().getValue());
            }
        });
        actionLayout.addComponent(acceptCheckbox);
        actionLayout.setComponentAlignment(acceptCheckbox, Alignment.MIDDLE_LEFT);

        acceptButton = new Button(ACCEPT_BUTTON);
        acceptButton.setEnabled(false);
        acceptButton.addListener(new Button.ClickListener(){

            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                services.getSettingsManager().markLicenseAgreementAsAccepted();
                navigator.show(Navigator.HOME);
            }
        });

        actionLayout.addComponent(acceptButton);
        actionLayout.setComponentAlignment(acceptButton, Alignment.MIDDLE_RIGHT);
        panel.addComponent(actionLayout);
    }


    @Override
    public String getPageGoogleAnalyticsID() {
        return "license_agreement";
    }

    @Override
    public Component getUI() {
        return panel;
    }
}