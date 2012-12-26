package com.taskadapter.connector.redmine;

import com.taskadapter.connector.common.AbstractTaskSaver;
import com.taskadapter.connector.definition.Mappings;
import com.taskadapter.connector.definition.exceptions.CommunicationException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.GRelation;
import com.taskadapter.model.GTask;
import com.taskadapter.model.GTaskDescriptor.FIELD;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineProcessingException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedmineTaskSaver extends AbstractTaskSaver<RedmineConfig> {

    private RedmineManager mgr;
    private Project rmProject;
    private GTaskToRedmine converter;
    private RedmineToGTask toGTask;
    private Mappings mappings;

    public RedmineTaskSaver(RedmineConfig config, Mappings mappings) {
        super(config);
        this.mappings = mappings;
        toGTask = new RedmineToGTask(config);
    }

    @Override
    public void beforeSave() throws ConnectorException {
        this.mgr = RedmineManagerFactory.createRedmineManager(config
                .getServerInfo());
        try {
            rmProject = mgr.getProjectByKey(config.getProjectKey());
        } catch (RedmineException e) {
            throw RedmineExceptions.convertException(e);
        }
        converter = new GTaskToRedmine(config, mappings, loadPriorities());
        converter.setUsers(loadUsers());
        converter.setStatusList(loadStatusList());
    }

    private Map<String, Integer> loadPriorities() throws ConnectorException {
        if (!mappings.isFieldSelected(FIELD.PRIORITY)) {
            return new HashMap<String, Integer>();
        }
        final Map<String, Integer> res = new HashMap<String, Integer>();
        try {
            for (IssuePriority prio : mgr.getIssuePriorities()) {
                res.put(prio.getName(), prio.getId());
            }
        } catch (RedmineException e) {
            throw RedmineExceptions.convertException(e);
        }
        return res;
    }

    private List<User> loadUsers() {
        List<User> users;
        if (config.isFindUserByName()) {
            try {
                users = mgr.getUsers();
            } catch (RedmineException e) {
                throw new RuntimeException(e);
            }
        } else {
            users = new ArrayList<User>();
        }
        return users;
    }

    private List<IssueStatus> loadStatusList() throws ConnectorException {
        List<IssueStatus> statusList;

        try {
            statusList = mgr.getStatuses();
        } catch (RedmineException e) {
            throw RedmineExceptions.convertException(e);
        }

        return statusList;
    }

    @Override
    protected Issue convertToNativeTask(GTask task) {
        return converter.convertToRedmineIssue(rmProject, task);
    }

    @Override
    protected GTask createTask(Object nativeTask) throws ConnectorException {
        try {
            Issue newIssue = mgr.createIssue(rmProject.getIdentifier(),
                    (Issue) nativeTask);
            return toGTask.convertToGenericTask(newIssue);
        } catch (RedmineException e) {
            throw RedmineExceptions.convertException(e);
        }
    }

    @Override
    protected void updateTask(String taskId, Object nativeTask) throws ConnectorException {
        Issue rmIssue = (Issue) nativeTask;
        rmIssue.setId(Integer.parseInt(taskId));
        try {
            mgr.update(rmIssue);

            if (config.getSaveIssueRelations()) {
                mgr.deleteIssueRelationsByIssueId(rmIssue.getId());
            }
        } catch (RedmineException e) {
            throw RedmineExceptions.convertException(e);
        }
    }

    @Override
    protected void saveRelations(List<GRelation> relations) {
        try {
            for (GRelation gRelation : relations) {
                int taskKey = Integer.parseInt(gRelation.getTaskKey());
                int relatedTaskKey = Integer.parseInt(gRelation
                        .getRelatedTaskKey());
                mgr.createRelation(taskKey, relatedTaskKey, gRelation.getType().toString());
            }
        } catch (RedmineProcessingException e) {
            result.addGeneralError(new RelationCreationException(e));
        } catch (RedmineException e) {
            result.addGeneralError(new CommunicationException(e));
        }
    }

}
