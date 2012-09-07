package com.taskadapter.connector.github;

import com.taskadapter.connector.common.AbstractTaskSaver;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.connector.definition.exceptions.UnsupportedConnectorOperation;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.List;

public class GithubTaskSaver extends AbstractTaskSaver<GithubConfig> {

    private IssueService issueService;

    private GithubToGTask taskConverter;
    private final UserService userService;

    public GithubTaskSaver(GithubConfig config) {
        super(config);
        ConnectionFactory ghConnector = new ConnectionFactory(config);
        issueService = ghConnector.getIssueService();
        userService = ghConnector.getUserService();
        taskConverter = new GithubToGTask();
    }


    @Override
    protected Issue convertToNativeTask(GTask task) throws ConnectorException {
        return new GTaskToGithub(userService).toIssue(task);
    }

    @Override
    protected GTask createTask(Object nativeTask) throws ConnectorException {
        Issue issue = (Issue) nativeTask;
        String userName = config.getServerInfo().getUserName();
        String repositoryName = config.getProjectKey();
        try {
            Issue createdIssue = issueService.createIssue(userName, repositoryName, issue);
            return taskConverter.toGtask(createdIssue);
        } catch (IOException e) {
            throw GithubUtils.convertException(e); 
        }
    }

    @Override
    protected void updateTask(String taskId, Object nativeTask) throws ConnectorException {
        Issue issue = (Issue) nativeTask;
        try {
            issueService.editIssue(config.getServerInfo().getUserName(), config.getProjectKey(), issue);
        } catch (IOException e) {
            throw GithubUtils.convertException(e); 
        }
    }

    @Override
    protected void saveRelations(List<GRelation> relations)
            throws UnsupportedConnectorOperation {
        throw new UnsupportedConnectorOperation(
                "saveRelations");
    }

}
