package com.taskadapter.webui.license;

import com.taskadapter.license.License;
import com.taskadapter.license.LicenseManager;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class LicenseInfoPanel extends VerticalLayout {
    private Label registeredTo;
    private Label licenseCreatedOn;

    public LicenseInfoPanel() {
        buildUI();
    }

    private void buildUI() {
        registeredTo = new Label();
        addComponent(registeredTo);

        licenseCreatedOn = new Label();
        addComponent(licenseCreatedOn);

        Button clearLicenseButton = new Button("Remove license info");
        clearLicenseButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                clearLicenseInfo();
            }
        });
        addComponent(clearLicenseButton);
    }

    public void setLicense(License license) {
        registeredTo.setValue("Registered to: " + license.getCustomerName());
        licenseCreatedOn.setValue("License created on: " + license.getCreatedOn());
    }

    private void clearLicenseInfo() {
        LicenseManager.removeTaskAdapterLicenseFromThisComputer();
        getWindow().showNotification("Removed the license info");
    }
}