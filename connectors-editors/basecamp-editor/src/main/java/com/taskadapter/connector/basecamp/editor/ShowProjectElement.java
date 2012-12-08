package com.taskadapter.connector.basecamp.editor;

import com.taskadapter.connector.basecamp.BasecampConfig;
import com.taskadapter.connector.basecamp.BasecampUtils;
import com.taskadapter.connector.basecamp.transport.ObjectAPIFactory;
import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GProject;
import com.taskadapter.web.ExceptionFormatter;
import com.taskadapter.web.WindowProvider;
import com.taskadapter.web.configeditor.EditorUtil;

import static com.taskadapter.web.ui.MessageUtils.nvl;

public final class ShowProjectElement {
    static void loadProject(WindowProvider windowProvider, BasecampConfig config, ExceptionFormatter exceptionFormatter, ObjectAPIFactory factory) {
        try {
            GProject project = BasecampUtils.loadProject(factory, config);
            showProjectInfo(windowProvider, project);

        } catch (BadConfigException e) {
            String localizedMessage = exceptionFormatter.formatError(e);
            windowProvider.getWindow().showNotification(localizedMessage);
        } catch (ConnectorException e) {
            String localizedMessage = exceptionFormatter.formatError(e);
            windowProvider.getWindow().showNotification("Oops", localizedMessage);
        }
    }

    private static void showProjectInfo(WindowProvider windowProvider, GProject project) {
        String msg = "<BR>Key:  " + project.getKey() + "<BR>Name: "
                + project.getName() + "<BR>Description :"
                + nvl(project.getDescription());

        EditorUtil.show(windowProvider.getWindow(), "Project Info", msg);
    }
}