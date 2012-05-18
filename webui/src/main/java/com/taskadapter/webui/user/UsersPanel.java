package com.taskadapter.webui.user;

import com.taskadapter.config.User;
import com.taskadapter.license.License;
import com.taskadapter.license.LicenseChangeListener;
import com.taskadapter.web.InputDialog;
import com.taskadapter.web.MessageDialog;
import com.taskadapter.web.service.Services;
import com.taskadapter.web.service.UserManager;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

import java.io.IOException;
import java.util.*;

public class UsersPanel extends Panel implements LicenseChangeListener {
    private static final int COLUMNS_NUMBER = 3;

    private Services services;
    private GridLayout usersLayout;
    private static final String DELETE_BUTTON = "Delete";
    private Label errorLabel;

    public UsersPanel(Services services) {
        super("Users");
        this.services = services;
        addStyleName("panelexample");
        services.getLicenseManager().addLicenseChangeListener(this);
        refreshPage();
    }

    private void refreshPage() {
        removeAllComponents();
        addErrorLabel();
        addCreateUserSectionIfAllowedByLicense();
        addUsersListPanel();
        refreshUsers();
    }

    private void addErrorLabel() {
        errorLabel = new Label();
        errorLabel.addStyleName("errorMessage");
    }

    private void addUsersListPanel() {
        usersLayout = new GridLayout();
        usersLayout.setColumns(COLUMNS_NUMBER);
        usersLayout.setSpacing(true);
        addComponent(usersLayout);
    }

    private void refreshUsers() {
        usersLayout.removeAllComponents();
        List<User> users = new ArrayList<User>(services.getUserManager().getUsers());
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getLoginName().compareTo(o2.getLoginName());
            }
        });
        for (User user : users) {
            addUserToPanel(user);
        }
    }

    private void addUserToPanel(final User user) {
        Label userLoginLabel = new Label(user.getLoginName());
        userLoginLabel.addStyleName("userLoginLabelInUsersPanel");
        usersLayout.addComponent(userLoginLabel);

        Button setPasswordButton = new Button("Set password");
        setPasswordButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                startSetPasswordProcess(user);
            }
        });
        usersLayout.addComponent(setPasswordButton);

        addDeleteButtonUnlessUserIsAdmin(user);
    }

    private void addDeleteButtonUnlessUserIsAdmin(final User user) {
        if (!user.getLoginName().equals(UserManager.ADMIN_LOGIN_NAME)) {
            addDeleteButton(user);
        }
    }

    private void addDeleteButton(final User user) {
        Button deleteButton = new Button(DELETE_BUTTON);
        deleteButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                startDeleteProcess(user);
            }
        });
        usersLayout.addComponent(deleteButton);
    }

    private void startDeleteProcess(final User user) {
        MessageDialog messageDialog = new MessageDialog(
                "Please confirm", "Delete user " + user.getLoginName(),
                Arrays.asList(DELETE_BUTTON, MessageDialog.CANCEL_BUTTON_LABEL),
                new MessageDialog.Callback() {
                    public void onDialogResult(String answer) {
                        if (answer.equals(DELETE_BUTTON)) {
                            deleteUser(user);
                        }
                    }
                }
        );
        messageDialog.setWidth(200, UNITS_PIXELS);
        getApplication().getMainWindow().addWindow(messageDialog);
    }

    private void deleteUser(User user) {
        try {
            services.getUserManager().deleteUser(user.getLoginName());
        } catch (IOException e) {
            errorLabel.setValue("Can't delete user. " + e.getMessage());
        }
        refreshPage();
    }

    private void startSetPasswordProcess(final User user) {
        InputDialog inputDialog = new InputDialog("Set the new password", "New password: ",
                new InputDialog.Recipient() {
                    public void gotInput(String newPassword) {
                        setPassword(user.getLoginName(), newPassword);
                    }
                });
        inputDialog.setPasswordMode();
        getWindow().addWindow(inputDialog);
    }

    private void setPassword(String loginName, String newPassword) {
        services.getUserManager().saveUser(loginName, newPassword);
    }

    private void addCreateUserSectionIfAllowedByLicense() {
        License currentlyInstalledLicense = services.getLicenseManager().getLicense();
        if (currentlyInstalledLicense != null) {
            int maxUsersNumber = currentlyInstalledLicense.getUsersNumber();
            int currentUsersNumber = services.getUserManager().getUsers().size();
            if (currentUsersNumber < maxUsersNumber) {
                addCreateUserSection();
            } else {
                addComponent(new Label("Maximum users number allowed by your license is reached."));
            }
        } else {
            addComponent(new Label("Can't add users until a license is installed."));
        }
    }

    private void addCreateUserSection() {
        Button button = new Button("Add user");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                startCreateUserProcess();
            }
        });
        addComponent(button);
    }

    private void startCreateUserProcess() {
        InputDialog inputDialog = new InputDialog("Create a new user", "Login name: ",
                new InputDialog.Recipient() {
                    public void gotInput(String loginName) {
                        createUser(loginName);
                    }
                });
        getWindow().addWindow(inputDialog);
    }

    private void createUser(String loginName) {
        services.getUserManager().saveUser(loginName, "");
        refreshPage();
    }

    @Override
    public void licenseInfoUpdated() {
        refreshPage();
    }
}
